package cc.rpc.core.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.Router;
import cc.rpc.core.api.RpcContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author nhsoft.lsd
 */
public class ConsumerBootstrap implements ApplicationContextAware {

    private Map<String, Object> stub = new HashMap<>();

    private ApplicationContext applicationContext;

    @Resource
    private ConsumerConfig consumerConfig;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void start() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {

//            //循环应用，这里需要改为更好的方式. 改为延后启动，不能在spring本身管理实例化的过程中
//            if (beanName.equals("createConsumerBootstrap")) {
//                return;
//            }
            LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
            Router router = applicationContext.getBean(Router.class);
            RpcContext context = new RpcContext();
            context.setRouter(router);
            context.setLoadBalancer(loadBalancer);

            Object bean = applicationContext.getBean(beanName);

            System.out.println(beanName + "====> " + bean.getClass().getCanonicalName());

            Class clazz = bean.getClass();

            while (clazz != null) {

                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    boolean isAnnotationPresent = field.isAnnotationPresent(CcConsumer.class);

                    if (isAnnotationPresent) {

                        Class<?> service = field.getType();
                        String serviceName = service.getCanonicalName();

                        Object proxy = Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CcRpcInvocationHandler(service, context, List.of(consumerConfig.getProviders().split(","))));

                        if (!stub.containsKey(serviceName)) {
                            stub.put(serviceName, proxy);
                        }

                        field.setAccessible(true);
                        try {
                            field.set(bean, proxy);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
    }
}
