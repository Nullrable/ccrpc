package cc.rpc.core.cluster;

import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public class ShortestResponseLoadBalancer implements LoadBalancer {

    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers) {
        //TODO nhsoft.lsd 待实现
        return null;
    }
}
