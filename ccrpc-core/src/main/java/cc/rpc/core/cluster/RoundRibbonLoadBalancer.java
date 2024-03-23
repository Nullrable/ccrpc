package cc.rpc.core.cluster;

import cc.rpc.core.api.LoadBalancer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author nhsoft.lsd
 */
public class RoundRibbonLoadBalancer implements LoadBalancer {

    AtomicInteger index = new AtomicInteger(0);

    @Override
    public String choose(final List<String> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
    
        return providers.get(index.getAndIncrement() % providers.size());
    }
}
