package cc.rpc.core.filter;

import cc.rpc.core.api.Filter;
import cc.rpc.core.api.RpcRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nhsoft.lsd
 */
public class CacheFilter implements Filter {
    Map<String, Object> cache = new ConcurrentHashMap<>();
    @Override
    public Object preFilter(final RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postFilter(final RpcRequest request, final Object data) {
        return cache.putIfAbsent(request.toString(), data);
    }
}
