package cc.rpc.core.cluster;

import cc.rpc.core.api.Invocation;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author nhsoft.lsd
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    // 虚拟节点数量（每个真实节点对应的虚拟节点数）
    private static final int VIRTUAL_NODE_COUNT = 150;

    private final Map<String, TreeMap<Long, InstanceMeta>> hashRingCache = new ConcurrentHashMap<>();

    private final MessageDigest md5;

    public ConsistentHashLoadBalancer() {

        try {
            this.md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers, final Invocation invocation) {

        TreeMap<Long, InstanceMeta> hasRing = getHashRing(providers);

        Map.Entry<Long,InstanceMeta> entry = hasRing.ceilingEntry(hash(invocation.getConsistentHashArg()));

        if (entry == null) {
            entry = hasRing.firstEntry();
        }

        return entry.getValue();
    }



    private TreeMap<Long, InstanceMeta> getHashRing(List<InstanceMeta> providers) {

       String key = providers.stream().map(this::hashKey).collect(Collectors.joining("#"));

        TreeMap<Long, InstanceMeta> hashRing =  hashRingCache.get(key);

        if (hashRing == null) {
            hashRing =  buildVirtualNodes(providers);
            hashRingCache.put(key, hashRing);
        }
        return hashRing;
    }

    private String hashKey(InstanceMeta provider) {
        return provider.getHost() + "#" + provider.getPort() + "#" + provider.getWeight();
    }

    private TreeMap<Long, InstanceMeta> buildVirtualNodes(final List<InstanceMeta> providers) {

        TreeMap<Long, InstanceMeta> hashRing = new TreeMap<>();

        for (InstanceMeta provider : providers) {
            // 构建虚拟节点
            int size = VIRTUAL_NODE_COUNT * Optional.ofNullable(provider.getWeight()).orElse(1);

            for (int i = 0; i < size; i++) {

                String key = provider.toPath() + "#" + i;
                long hash = hash(key);

                hashRing.put(hash, provider);
            }
        }

        return hashRing;

    }

    private long hash(String key) {

        md5.reset();
        byte[] digest = md5.digest(key.getBytes(StandardCharsets.UTF_8));

        // 取 MD5 结果的前 8 个字节转换为 long
        long hash = 0;
        for (int i = 0; i < 8; i++) {
            hash = (hash << 8) | (digest[i] & 0xFF);
        }

        return hash;
    }
}
