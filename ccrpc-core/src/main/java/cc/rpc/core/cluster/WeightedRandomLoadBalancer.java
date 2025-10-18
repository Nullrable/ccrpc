package cc.rpc.core.cluster;

import cc.rpc.core.api.Invocation;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class WeightedRandomLoadBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers, Invocation invocation) {

        int totalWeight = providers.stream().map(provider -> provider.getWeight() == null ? 0 : provider.getWeight()).reduce(0, Integer::sum);

        //如果都没设置权重，则使用随机负载均衡策略
        if (totalWeight == 0) {
            log.warn("All providers have no weight, use random load balance strategy.");
            return providers.get(providers.size() - 1);
        }

        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;
        for (InstanceMeta provider : providers) {
            if (provider.getWeight() != null) {
                int weight = provider.getWeight();
                currentWeight += weight;
                if (randomWeight <= currentWeight) {
                    return provider;
                }
            }
        }

        return providers.get(providers.size() - 1);
    }
}
