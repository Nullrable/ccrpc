package cc.rpc.core.registry.zk;

import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.registry.ChangedListener;
import cc.rpc.core.registry.Event;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @author nhsoft.lsd
 */
public class ZkRegisterCenter implements RegisterCenter {

    private CuratorFramework client;

    public ZkRegisterCenter(final String zkServer) {
        client = CuratorFrameworkFactory.
                builder().connectString(zkServer).retryPolicy(new
                        ExponentialBackoffRetry(1000,3)).
                namespace("ccrpc").build();
    }


    @Override
    public void start() {
        System.out.println("zookeeper register starting");
        client.start();
        System.out.println("zookeeper register started");
    }

    @Override
    public void stop() {
        System.out.println("zookeeper register stopping");
        client.close();
        System.out.println("zookeeper register stopped");
    }

    @Override
    @SneakyThrows
    public void register(final ServiceMeta service, final InstanceMeta instanceMeta) {

        String servicePath = "/" + service.toPath();

        if (client.checkExists().forPath(servicePath) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
        }

        String providerPath = servicePath + "/" + instanceMeta.toPath();
        client.create().withMode(CreateMode.EPHEMERAL).forPath(providerPath, "provider".getBytes());

        System.out.printf("register %s to zookeeper%n", providerPath);

    }

    @Override
    @SneakyThrows
    public void unregister(final ServiceMeta service, final InstanceMeta instanceMeta) {

        String servicePath = "/" + service.toPath();;

        System.out.printf("unregistering %s from zookeeper%n", servicePath);

        if (client.checkExists().forPath(servicePath) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
        }
        String providerPath = servicePath + "/" + instanceMeta.toPath();
        client.delete().quietly().forPath(providerPath);

        System.out.printf("unregister %s from zookeeper%n", providerPath);
    }

    @Override
    @SneakyThrows
    public List<InstanceMeta> fetchAll(final ServiceMeta service) {

        String servicePath = "/" + service.toPath();

        List<String> nodes = client.getChildren().forPath(servicePath);

        List<InstanceMeta> instanceMetas = new ArrayList<>();
        nodes.forEach(n -> {
            String[] node = n.split("_");
            InstanceMeta instance = InstanceMeta.http(node[0], Integer.parseInt(node[1]));
            instanceMetas.add(instance);
        });

        instanceMetas.forEach(System.out::println);

        return instanceMetas;
    }

    @Override
    @SneakyThrows
    public void subscribe(final ServiceMeta service, final ChangedListener listener) {

        System.out.println("subscribe");

        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    // 有任何节点变动这里会执行
                    System.out.println("zk subscribe event: " + event);
                    List<InstanceMeta> instances = fetchAll(service);
                    listener.fire(new Event(instances));
                }
        );

        cache.start();
    }
}
