package cc.rpc.core.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.RpcContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
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

    @PostConstruct
    public void init() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {

            if (beanName.equals("createConsumerBootstrap")) {
                return;
            }

            Object bean = applicationContext.getBean(beanName);

            System.out.println(beanName + "====> " + bean.getClass().getCanonicalName());

            Class clazz = bean.getClass();

            if (beanName.equals("ccRpcDemoConsumerApplication")) {
                System.out.println("1231232");
            }

            while (clazz != null) {

                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    boolean isAnnotationPresent = field.isAnnotationPresent(CcConsumer.class);

                    if (isAnnotationPresent) {

                        Class<?> service = field.getType();
                        String serviceName = service.getCanonicalName();

                        RpcContext context = new RpcContext();
                        //TODO nhsoft.lsd

                        Object proxy = Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CcRpcInvocationHandler(service, context, List.of(consumerConfig.getProvider())));

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

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
