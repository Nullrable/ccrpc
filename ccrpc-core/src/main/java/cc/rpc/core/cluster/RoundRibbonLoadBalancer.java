package cc.rpc.core.cluster;

import cc.rpc.core.api.LoadBalancer;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public class RoundRibbonLoadBalancer implements LoadBalancer {

    @Override
    public String choose(final List<String> providers) {
        return null;
    }
}
