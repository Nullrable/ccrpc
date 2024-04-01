package cc.rpc.core.api;

import cc.rpc.core.meta.InstanceMeta;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface Router {

    List<InstanceMeta> route(List<InstanceMeta> providers);

    Router Default = providers -> providers;

}
