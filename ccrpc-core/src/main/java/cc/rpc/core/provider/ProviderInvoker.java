package cc.rpc.core.provider;

import cc.rpc.core.api.CcRpcException;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.config.ProviderProperties;
import cc.rpc.core.governance.SlidingTimeWindow;
import cc.rpc.core.meta.ProviderMeta;
import cc.rpc.core.util.TypeUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    private final Map<String, SlidingTimeWindow> trafficController = new ConcurrentHashMap<>();

//    private final int tc;

    private ProviderProperties providerProperties;

    public ProviderInvoker(final ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
        providerProperties = providerBootstrap.getProviderProperties();
    }

    public RpcResponse<?> invoke(final RpcRequest request) {

        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {

            if (RpcContext.getContext() != null) {
                request.getParameters().forEach(RpcContext::put);
            }

            ProviderMeta providerMeta = providerMetas.stream().filter(meta -> request.getMethodSign().equals(meta.getMethodSign())).findFirst().orElse(null);

            if (providerMeta == null) {
                return new RpcResponse<>(false, "method not found", null);
            }

            String service = request.getService();

            int tc =  Integer.parseInt(providerProperties.getMetas().getOrDefault("tc", "20"));

            synchronized (trafficController) {
                SlidingTimeWindow trafficWindow = trafficController.computeIfAbsent(service, k -> new SlidingTimeWindow(30));
                if (trafficWindow.calcSum() > tc) {
                    log.debug(" ========> invoker url: {} traffic limit {} ", tc, service);
                    throw new CcRpcException(CcRpcException.TRAFFIC_LIMIT);
                }
                trafficWindow.record(System.currentTimeMillis());
            }


            Method method = providerMeta.getMethod();
            Object bean = providerMeta.getService();

            //request.getArgs() 类型匹配
            Object result = method.invoke(bean, TypeUtil.requestCast(request.getArgs(), method));

            return new RpcResponse<>(true, result, null);

        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
            return new RpcResponse<>(false, null, new CcRpcException(e.getMessage()));
        } catch (CcRpcException e) {
            log.error(e.getMessage(), e);
            return new RpcResponse<>(false, null, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new RpcResponse<>(false, null, new CcRpcException(CcRpcException.UNKNOWN));
        } finally {
            RpcContext.clear();
        }
    }
}
