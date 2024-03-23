package cc.rpc.core.api;

import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface LoadBalancer {

    String choose(List<String> providers);

    LoadBalancer Default = providers -> {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        return providers.get(0);
    };
}
