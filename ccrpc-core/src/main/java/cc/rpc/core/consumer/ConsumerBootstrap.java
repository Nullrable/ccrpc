package cc.rpc.core.consumer;

import cc.rpc.core.annotation.CcConsumer;
import cc.rpc.core.api.CcRpcException;
import cc.rpc.core.api.Filter;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.config.AppProperties;
import cc.rpc.core.config.ConsumerProperties;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.registry.ChangedListener;
import cc.rpc.core.registry.Event;
import cc.rpc.core.util.MethodUtil;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware {

    private Map<String, Object> stub = new HashMap<>();

    private ApplicationContext applicationContext;

    private RegisterCenter rc;

    private AppProperties appProperties;

    private ConsumerProperties consumerProperties;

    public ConsumerBootstrap(final AppProperties appProperties, final ConsumerProperties consumerProperties) {
        this.appProperties = appProperties;
        this.consumerProperties = consumerProperties;
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PreDestroy
    public void stop() {
        log.info("consumer bootstrap stopped");
        rc.stop();
    }

    public void start() {

        log.info("consumer bootstrap starting");

        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        Router router = applicationContext.getBean(Router.class);
        Map<String, Filter> filterMap = applicationContext.getBeansOfType(Filter.class);
        List<Filter> filters = filterMap.values().stream().sorted().collect(Collectors.toList());

        rc = applicationContext.getBean(RegisterCenter.class);
        rc.start();

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.setConsumerProperties(consumerProperties);
        log.info(" =========> rpc context: " + context);

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {

            Object bean = applicationContext.getBean(beanName);

            Class clazz = bean.getClass();

            List<Field> fields = MethodUtil.findAnnotatedField(clazz, CcConsumer.class);

            fields.forEach(field -> {
                Class<?> service = field.getType();
                String serviceName = service.getCanonicalName();

                Object proxy = stub.get(serviceName);

                if (proxy == null) {
                    proxy = createConsumerFromRegister(service, context, rc);
                    stub.put(serviceName, proxy);
                }
                try {
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                    throw new CcRpcException(CcRpcException.ILLEGAL_ACCESS_EX, e);
                }
            });
        }

        log.info("consumer bootstrap started");
    }

    private Object createConsumerFromRegister(Class<?> service, RpcContext context, RegisterCenter rc) {
        ServiceMeta serviceMeta = ServiceMeta.builder().app(appProperties.getId()).namespace(appProperties.getNamespace()).env(appProperties.getEnv()).service(service.getCanonicalName()).build();
        List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        Object proxy = createConsumer(service, context, providers);
        rc.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });
        return proxy;
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<InstanceMeta> providers) {

        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new CcRpcInvocationHandler(service, context, providers));
    }
}
