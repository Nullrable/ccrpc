package cc.rpc.core.cluster;

import cc.rpc.core.api.LoadBalancer;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    @Override
    public String choose(final List<String> providers) {
        //TODO nhsoft.lsd 后续待实现
        return null;
    }
}
