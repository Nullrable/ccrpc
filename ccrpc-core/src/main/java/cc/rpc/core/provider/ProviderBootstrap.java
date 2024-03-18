package cc.rpc.core.provider;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.util.MethodUtil;
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

        providers.values().forEach(this::genInterface);
    }

    private void genInterface(final Object x) {
        //TODO nhsoft.lsd 这里可能实现多个接口，需要改造
        Class<?> itfer = x.getClass().getInterfaces()[0];
        skeleton.put(itfer.getCanonicalName(), x);
    }

    public RpcResponse invoke(final RpcRequest request) {
        Object bean = skeleton.get(request.getClazz());
        try {

            //TODO nhsoft.lsd 这里需要缓存，反射获取影响性能
            Method method = Arrays.stream(bean.getClass().getDeclaredMethods()).filter(m -> {
                //过滤本地Object方法和方法前面
                return !MethodUtil.checkLocalMethod(m) && MethodUtil.methodSign(m).equals(request.getMethodSign());
            }).findFirst().orElse(null);
            if (method == null) {
                return new RpcResponse(false, "method not found", null);
            }

            //TODO nhsoft.lsd  request.getArgs() 类型匹配
            Object result = method.invoke(bean, request.getArgs());
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
