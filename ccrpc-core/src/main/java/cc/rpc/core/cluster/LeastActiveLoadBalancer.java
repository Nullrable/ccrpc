package cc.rpc.core.cluster;

import cc.rpc.core.api.Invocation;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.meta.InstanceMeta;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public class LeastActiveLoadBalancer implements LoadBalancer {
    @Override
    public InstanceMeta choose(final List<InstanceMeta> providers, Invocation invocation) {
        //TODO nhsoft.lsd 后续待实现
        return null;
    }
}
