package cc.rpc.core.provider;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author nhsoft.lsd
 */
public class ProviderBootstrap implements ApplicationContextAware {

    private Map<String, Object> skeleton = new HashMap<>();

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CcProvider.class);

        providers.values().forEach(
                x -> genInterface(x)
        );
    }

    private void genInterface(final Object x) {
        Class<?> itfer = x.getClass().getInterfaces()[0];
        skeleton.put(itfer.getCanonicalName(), x);
    }

    public RpcResponse invoke(final RpcRequest request) {


        Object bean = skeleton.get(request.getClazz());
        try {

            Method method =  Arrays.stream(bean.getClass().getMethods()).filter(m -> m.getName().equals(request.getMethod())).findFirst().orElse(null);
            if (method == null) {
                return new RpcResponse(false, "method not found");
            }
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
