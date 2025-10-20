package cc.rpc.core.cluster;

import cc.rpc.core.api.Invocation;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nhsoft.lsd
 */
public class LeastActiveLoadBalancer implements LoadBalancer {

    private final Map<String, ServerNode> serverNodes = new ConcurrentHashMap<>();

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers, Invocation invocation) {

        //这个有性能问题，serverNodes 每次调用都要判断，性能较差
        addNodes(providers);

        Map<ServerNode, Double> scoreMap = new HashMap<>();

        double totalScore = 0L;
        Collection<ServerNode> nodes = serverNodes.values();
        for (ServerNode node : nodes) {
            long score = node.getActiveConnections().get();
            double revertedScore = 1.0 / (score + 1);
            scoreMap.put(node, revertedScore);

            totalScore += revertedScore;
        }

        double randomScore = ThreadLocalRandom.current().nextDouble(totalScore);;

        double currentScore = 0L;
        for (ServerNode node : nodes) {
            double score = scoreMap.get(node);
            currentScore += score;

            if (randomScore <= currentScore) {
                node.incrementConnections();
                return node.getInstance();
            }
        }

        return providers.get(0);
    }

    private void addNodes(final List<InstanceMeta> providers) {
        Set<String> currentPaths = providers.stream()
                .map(InstanceMeta::toPath)
                .collect(Collectors.toSet());

        // 移除下线节点
        serverNodes.keySet().removeIf(k -> !currentPaths.contains(k));

        // 增加新节点
        for (InstanceMeta provider : providers) {
            serverNodes.computeIfAbsent(provider.toPath(), k -> new ServerNode(provider))
                    .setInstance(provider);
        }
    }

    public void decrementConnections(InstanceMeta instance) {
        LeastActiveLoadBalancer.ServerNode serverNode = serverNodes.get(instance.toPath());
        if (serverNode == null) {
            return;
        }
        serverNode.decrementConnections();
    }

    @Getter
    @Setter
    private static class ServerNode {
        private InstanceMeta instance;
        private final AtomicLong activeConnections;   // 活跃连接数

        public ServerNode(InstanceMeta instance) {
            this.instance = instance;
            this.activeConnections = new AtomicLong(0);
        }

        // 增加活跃连接
        public void incrementConnections() {
            activeConnections.incrementAndGet();
        }

        // 减少活跃连接
        public void decrementConnections() {
            activeConnections.updateAndGet(v -> Math.max(0, v - 1));
        }
    }
}
