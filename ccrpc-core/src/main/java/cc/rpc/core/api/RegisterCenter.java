package cc.rpc.core.api;

import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface RegisterCenter {

    void start();

    void stop();

    void register(String service, String url);

    void unregister(String service, String url);

    List<String> fetchAll(String service);

    void subscribe();
}
