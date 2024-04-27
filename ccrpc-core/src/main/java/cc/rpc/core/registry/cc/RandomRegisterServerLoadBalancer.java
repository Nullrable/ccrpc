package cc.rpc.core.registry.cc;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author nhsoft.lsd
 */
public class RandomRegisterServerLoadBalancer implements RegisterServerLoadBalancer{

    @Override
    public String chooseOneFrom(final List<String> servers) {
        if (null == servers) {
            throw new IllegalArgumentException("arg is null");
        }
        if (servers.isEmpty()) {
            throw new IllegalArgumentException("arg is empty");
        }
        int index = ThreadLocalRandom.current().nextInt(servers.size());
        return servers.get(index);
    }
}
