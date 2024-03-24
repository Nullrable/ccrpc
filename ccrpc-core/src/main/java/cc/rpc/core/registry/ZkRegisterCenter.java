package cc.rpc.core.registry;

import cc.rpc.core.api.RegisterCenter;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public class ZkRegisterCenter implements RegisterCenter {
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void register(final String service, final String url) {

    }

    @Override
    public void unregister(final String service, final String url) {

    }

    @Override
    public List<String> fetchAll(final String service) {
        return null;
    }

    @Override
    public void subscribe() {

    }
}
