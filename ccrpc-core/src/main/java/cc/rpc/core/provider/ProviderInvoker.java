package cc.rpc.core.provider;

import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.meta.ProviderMeta;
import cc.rpc.core.util.TypeUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.springframework.util.MultiValueMap;

/**
 * @author nhsoft.lsd
 */
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(final ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    public RpcResponse invoke(final RpcRequest request) {

        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {

            ProviderMeta providerMeta = providerMetas.stream().filter(meta -> request.getMethodSign().equals(meta.getMethodSign())).findFirst().orElse(null);

            if (providerMeta == null) {
                return new RpcResponse(false, "method not found", null);
            }

            Method method = providerMeta.getMethod();
            Object bean = providerMeta.getService();

            //request.getArgs() 类型匹配
            Object result = method.invoke(bean, TypeUtil.requestCast(request.getArgs(), method));

            return new RpcResponse(true, result, null);

        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new RpcResponse(false, null, new RuntimeException(e.getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new RpcResponse(false, null, new RuntimeException(e.getMessage()));
        }
    }
}
