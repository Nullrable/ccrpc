package cc.rpc.core.registry.zk;

import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.config.ZkProperties;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.registry.ChangedListener;
import cc.rpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class ZkRegisterCenter implements RegisterCenter {

    private ZkProperties zkProperties;

    private CuratorFramework client;

    public ZkRegisterCenter(ZkProperties zkProperties) {

        this.zkProperties = zkProperties;

        client = CuratorFrameworkFactory.
                builder().connectString(zkProperties.getServer()).retryPolicy(new
                        ExponentialBackoffRetry(1000,3)).
                namespace(zkProperties.getRoot()).build();
    }

    @Override
    public void start() {
        log.info("zookeeper register starting, server: {}, namespace: {}", zkProperties.getServer(), zkProperties.getRoot());
        client.start();
        log.info("zookeeper register started, server: {}, namespace: {}", zkProperties.getServer(), zkProperties.getRoot());
    }

    @Override
    public void stop() {
        log.info("zookeeper register stopping, server: {}, namespace: {}", zkProperties.getServer(), zkProperties.getRoot());
        client.close();
        log.info("zookeeper register stopped, server: {}, namespace: {}", zkProperties.getServer(), zkProperties.getRoot());
    }

    @Override
    public void heartbeat() {

    }

    @Override
    @SneakyThrows
    public void register(final ServiceMeta service, final InstanceMeta instanceMeta) {

        String servicePath = "/" + service.toPath();

        if (client.checkExists().forPath(servicePath) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
        }

        log.debug(String.format("register service path %s to zookeeper", servicePath));

        String providerPath = servicePath + "/" + instanceMeta.toPath();

        if (client.checkExists().forPath(providerPath) != null) {
            client.delete().quietly().forPath(providerPath);
        }

        client.create().withMode(CreateMode.EPHEMERAL).forPath(providerPath, instanceMeta.toMetas().getBytes());

        log.debug(String.format("register provider path %s to zookeeper", providerPath));

    }

    @Override
    @SneakyThrows
    public void unregister(final ServiceMeta service, final InstanceMeta instanceMeta) {

        String servicePath = "/" + service.toPath();;

        log.debug(String.format("unregistering service path %s from zookeeper", servicePath));

        if (client.checkExists().forPath(servicePath) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
        }
        String providerPath = servicePath + "/" + instanceMeta.toPath();
        client.delete().quietly().forPath(providerPath);

        log.debug(String.format("unregister provider path %s from zookeeper", providerPath));
    }

    @Override
    @SneakyThrows
    public List<InstanceMeta> fetchAll(final ServiceMeta service) {

        String servicePath = "/" + service.toPath();

        List<String> nodes = client.getChildren().forPath(servicePath);

        List<InstanceMeta> instanceMetas = new ArrayList<>();
        nodes.forEach(n -> {

            System.out.println(" node: " + n);

            String[] node = n.split("_");
            InstanceMeta instance = InstanceMeta.http(node[0], Integer.parseInt(node[1]));

            System.out.println(" instance: " + instance.toUrl());
            String nodePath = servicePath + "/" + instance.toPath();
            byte[] bytes;
            try {
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String,Object> params = JSON.parseObject(new String(bytes));
            params.forEach((k,v) -> {
                System.out.println(k + " -> " +v);
                instance.getParameters().put(k,v==null?null:v.toString());
            });

            instanceMetas.add(instance);
        });

        instanceMetas.forEach(instanceMeta -> log.info(instanceMeta.toPath()));

        return instanceMetas;
    }

    @Override
    @SneakyThrows
    public void subscribe(final ServiceMeta service, final ChangedListener listener) {

        log.debug("subscribe to zookeeper {}", service);

        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener(
                (curator, event) -> {
                    // 有任何节点变动这里会执行
                    log.debug("zookeeper subscribe event: {}", event);
                    List<InstanceMeta> instances = fetchAll(service);
                    listener.fire(new Event(instances));
                }
        );

        cache.start();
    }
}
