package cc.rpc.core.cluster;

import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.List;
import java.util.Random;

/**
 * @author nhsoft.lsd
 */
public class RandomLoadBalancer implements LoadBalancer {

    Random random = new Random();

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        return providers.get(random.nextInt(providers.size()));
    }
}
