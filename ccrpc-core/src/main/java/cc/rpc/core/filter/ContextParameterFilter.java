package cc.rpc.core.filter;

import cc.rpc.core.api.Filter;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import java.util.Map;

/**
 * @author nhsoft.lsd
 */
public class ContextParameterFilter implements Filter {
    @Override
    public Object preFilter(final RpcRequest request) {
        Map<String, String> params = RpcContext.getContext();
        if (params != null) {
            request.setParameters(params);
        }
        return null;
    }

    @Override
    public Object postFilter(final RpcRequest request, final Object data) {
        RpcContext.clear();
        return null;
    }
}
