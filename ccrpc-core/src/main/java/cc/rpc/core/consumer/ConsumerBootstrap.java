package cc.rpc.core.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.util.MethodUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author nhsoft.lsd
 */
public class ConsumerBootstrap implements ApplicationContextAware {

    private Map<String, Object> stub = new HashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void start() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        Router router = applicationContext.getBean(Router.class);
        RegisterCenter rc = applicationContext.getBean(RegisterCenter.class);

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);

        for (String beanName : beanNames) {

            Object bean = applicationContext.getBean(beanName);

            Class clazz = bean.getClass();

            List<Field> fields =  MethodUtil.findAnnotatedField(clazz, CcConsumer.class);

            fields.forEach(field -> {
                Class<?> service = field.getType();
                String serviceName = service.getCanonicalName();

                Object proxy = stub.get(serviceName);
                if (proxy == null) {
                    proxy = createConsumer(service, context, rc.fetchAll(serviceName));
                    stub.put(serviceName, proxy);
                }
                try {
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CcRpcInvocationHandler(service, context, providers));
    }
}
