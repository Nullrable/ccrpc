package cc.rpc.core.provider;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ProviderMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.util.MethodUtil;
import cc.rpc.core.util.TypeUtil;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.TypeUtils;

/**
 * @author nhsoft.lsd
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private RegisterCenter registerCenter;

    private InstanceMeta instance;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;


    public void start() {
        System.out.println("ProviderBootstrap init");
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CcProvider.class);
        registerCenter = applicationContext.getBean(RegisterCenter.class);
        providers.keySet().forEach(System.out::println);
        providers.values().forEach(this::genInterface);

        System.out.println("ProviderBootstrap start");

        registerCenter.start();


        instance = createInstance();

        skeleton.keySet().forEach(service -> {
            ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).service(service).build();
            registerCenter.register(serviceMeta, instance);
        });

    }

    private InstanceMeta createInstance() {
        //注册服务
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String port = System.getProperty("server.port");

        return new InstanceMeta("http", ip, Integer.parseInt(port), null);
    }

    public void stop() {
        System.out.println("ProviderBootstrap stop");
        //取消注册服务
        skeleton.keySet().forEach(service -> {
            ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).service(service).build();
            registerCenter.unregister(serviceMeta, instance);
        });
        registerCenter.stop();
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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

        System.out.println("注册的方法：" + meta);

        return meta;
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
