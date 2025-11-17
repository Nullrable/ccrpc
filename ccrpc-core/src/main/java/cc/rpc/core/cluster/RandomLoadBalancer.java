package cc.rpc.core.cluster;

import cc.rpc.core.api.Invocation;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author nhsoft.lsd
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers, Invocation invocation) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        return providers.get(ThreadLocalRandom.current().nextInt(providers.size()));
    }
}
