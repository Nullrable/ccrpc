package cc.rpc.core.registry.cc;

import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface RegisterServerLoadBalancer {

    String chooseOneFrom(List<String> servers);
}
