package cc.rpc.core.filter;

import cc.rpc.core.api.Filter;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.util.MethodUtil;
import cc.rpc.core.util.MockUtil;
import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.SneakyThrows;

/**
 * @author nhsoft.lsd
 */
public class MockFilter implements Filter {
    @Override
    @SneakyThrows
    public Object preFilter(final RpcRequest request) {

        Class<?> service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());

        return MockUtil.mock(method.getReturnType());
    }

    private Method findMethod(final Class service, final String methodSign) {
        return Arrays.stream(service.getMethods()).filter(method -> MethodUtil.methodSign(method).equals(methodSign)).findFirst().orElse(null);
    }

    @Override
    public Object postFilter(final RpcRequest request, final Object data) {
        return null;
    }
}
