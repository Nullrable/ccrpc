package cc.rpc.core.provider;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.config.AppProperties;
import cc.rpc.core.config.ProviderProperties;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ProviderMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.util.MethodUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author nhsoft.lsd
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private RegisterCenter registerCenter;

    private InstanceMeta instance;

    private int serverPort;
    private AppProperties appProperties;
    private ProviderProperties providerProperties;

    public ProviderBootstrap(final int serverPort, final AppProperties appProperties, final ProviderProperties providerProperties) {
        this.serverPort = serverPort;
        this.appProperties = appProperties;
        this.providerProperties = providerProperties;
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        log.info("provider bootstrap initialling");
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CcProvider.class);
        providers.keySet().forEach(log::info);
        providers.values().forEach(this::genInterface);

        registerCenter = applicationContext.getBean(RegisterCenter.class);

        log.info("provider bootstrap initialled");

    }

    public void start() {
        log.info("provider bootstrap starting");

        instance = createInstance();
        registerCenter.start();
        registerCenter.heartbeat();
        skeleton.keySet().forEach(this::registerService);

        log.info("provider bootstrap started");
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder().app(appProperties.getId()).namespace(appProperties.getNamespace()).env(appProperties.getEnv()).service(service).build();
        registerCenter.register(serviceMeta, instance);
    }

    @PreDestroy
    public void stop() {
        log.info("provider bootstrap stopping");

        //取消注册服务
        skeleton.keySet().forEach(this::unregisterService);
        registerCenter.stop();

        log.info("provider bootstrap stopped");
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder().app(appProperties.getId()).namespace(appProperties.getNamespace()).env(appProperties.getEnv()).service(service).build();
        registerCenter.unregister(serviceMeta, instance);
    }

    private void genInterface(final Object service) {
        Class<?>[] itfers = service.getClass().getInterfaces();

        Arrays.stream(itfers).forEach(itfer -> {
            for (Method method : itfer.getMethods()) {
                if (MethodUtil.checkLocalMethod(method)) {
                    continue;
                }
                ProviderMeta meta = createProviderMeta(method, service);
                skeleton.add(itfer.getCanonicalName(), meta);
            }
        });
    }

    private ProviderMeta createProviderMeta(final Method method, Object service) {

        ProviderMeta meta = new ProviderMeta();
        meta.setService(service);
        meta.setMethod(method);
        meta.setMethodSign(MethodUtil.methodSign(method));

        log.debug(" ========> create provider meta: {}", meta);

        return meta;
    }

    @SneakyThrows
    private InstanceMeta createInstance() {
        //注册服务
        String ip = InetAddress.getLocalHost().getHostAddress();
        InstanceMeta instanceMeta = InstanceMeta.http(ip, serverPort);
        instanceMeta.setParameters(providerProperties.getMetas());
        return instanceMeta;
    }
}
