package cc.rpc.core.api;

import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface Router {

    List<String> route(List<String> providers);

    Router Default = providers -> providers;

}
