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

    private final int trafficLimit;

    public ProviderInvoker(final ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
        trafficLimit = Integer.parseInt(providerBootstrap.getProviderProperties().getMetas().getOrDefault("trafficLimit", "20"));
    }

    public RpcResponse<?> invoke(final RpcRequest request) {

        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {

            ProviderMeta providerMeta = providerMetas.stream().filter(meta -> request.getMethodSign().equals(meta.getMethodSign())).findFirst().orElse(null);

            if (providerMeta == null) {
                return new RpcResponse<>(false, "method not found", null);
            }

            String service = request.getService();

            synchronized (trafficController) {
                SlidingTimeWindow trafficWindow = trafficController.computeIfAbsent(service, k -> new SlidingTimeWindow(1));

                if (trafficWindow.calcSum() > trafficLimit) {
                    log.debug(" ========> traffic limit {} invoker url: {}", trafficLimit, service);
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
        }
    }
}
