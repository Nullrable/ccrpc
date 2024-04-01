package cc.rpc.core.api;

import cc.rpc.core.meta.InstanceMeta;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface LoadBalancer {

    InstanceMeta choose(List<InstanceMeta> providers);

    LoadBalancer Default = providers -> {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        return providers.get(0);
    };
}
