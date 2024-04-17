package cc.rpc.core.consumer;

import cc.rpc.core.api.CcRpcException;
import cc.rpc.core.api.Filter;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.consumer.http.OkHttpInvoker;
import cc.rpc.core.governance.SlidingTimeWindow;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.util.MethodUtil;
import cc.rpc.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;

/**
 * Consumer InvocationHandler.
 *
 * @author nhsoft.lsd
 */
@Slf4j
public class CcRpcInvocationHandler implements InvocationHandler {

    private final Class<?> service;

    private final RpcContext context;

    private final HttpInvoker httpInvoker;

    //活跃的服务实例
    private final List<InstanceMeta> providers;

    //隔离的服务实例
    private final List<InstanceMeta> isolatedProviders = new CopyOnWriteArrayList<>();

    //半开的服务实例，用于每隔一段时间进行探活，请求一次成功后，从隔离和半开的移除，加入到活跃的服务实例中
    private final List<InstanceMeta> halfOpenProviders = new CopyOnWriteArrayList<>();

    private final Map<String, SlidingTimeWindow> isolateController = new ConcurrentHashMap<>();

    public CcRpcInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;

        this.httpInvoker = new OkHttpInvoker(context.getConsumerProperties().getReadTimeout(),
                context.getConsumerProperties().getReadTimeout());

        ScheduledExecutorService singleService = new ScheduledThreadPoolExecutor(1);
        singleService.scheduleWithFixedDelay(this::halfOpen,
                context.getConsumerProperties().getHalfOpenInitialDelay(),
                context.getConsumerProperties().getHalfOpenDelay(),
                TimeUnit.SECONDS);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        log.debug(" ===> request: {}", JSON.toJSONString(args));

        RpcRequest request = new RpcRequest();
        request.setService(service.getName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);

        for (int i = 1; i <= context.getConsumerProperties().getRetries(); i++) {
            List<Filter> filters = context.getFilters();
            if (filters != null && !filters.isEmpty()) {
                for (Filter filter : filters) {
                    Object result = filter.preFilter(request);
                    if (result != null) {
                        return result;
                    }
                }
            }

            //根据是否存在半开服务实例来判断是否需要探活
            InstanceMeta instance;
            if (halfOpenProviders.isEmpty()) {
                List<InstanceMeta> nodes = context.getRouter().route(providers);
                instance = context.getLoadBalancer().choose(nodes);
            } else {
                instance = halfOpenProviders.remove(0);
            }

            ResponseBody responseBody;
            String url = instance.toUrl();

            try {
                log.debug(" ========> retries: {} invoker url: {}", i, instance.toUrl());
                responseBody = httpInvoker.post(url, request);
            } catch (Exception ex){
                log.error(ex.getMessage(), ex);

                synchronized (isolateController) {
                    SlidingTimeWindow faultWindow = isolateController.computeIfAbsent(url, k -> new SlidingTimeWindow());

                    faultWindow.record(System.currentTimeMillis());

                    int recordSum = faultWindow.getSum();
                    if (recordSum >= context.getConsumerProperties().getFaultLimit()) {
                        //依赖滑动窗口计算，一段时间内同一个实例失败次数大达到阈值，则放入到隔离服务实例中
                        isolated(instance);
                    }
                }

                //如果读取超时，则进行重试
                if (ex instanceof SocketTimeoutException) {
                    if (context.getConsumerProperties().getRetries() - 1 == i) {
                        throw new CcRpcException(CcRpcException.READ_TIMEOUT_EX);
                    }
                    continue;
                }

                throw ex;
            }


            if (responseBody == null) {
                throw new CcRpcException(CcRpcException.RESPONSE_NULL);
            }

            String resultJson = responseBody.string();
            log.debug(" ===> result: {}", resultJson);

            RpcResponse rpcResponse = JSON.parseObject(resultJson, RpcResponse.class);

            if (rpcResponse.isStatus()) {
                Object data = rpcResponse.getData();
                Object result = TypeUtil.resultCast(data, method);

                if (filters != null && !filters.isEmpty()) {
                    for (Filter filter : filters) {
                         Object resultAfterFilter = filter.postFilter(request, result);
                         if (resultAfterFilter != null) {
                             return resultAfterFilter;
                         }
                    }
                }

                //隔离的服务探活成功后，重新加入到活跃服务实例中
                recovered(instance);

                return result;
            } else {
                throw rpcResponse.getEx();
            }
        }
        throw new CcRpcException(CcRpcException.APP_RETRIES_MUST_GATHER_THAN_ZERO);
    }

    //隔离的服务探活成功后，重新加入到活跃服务实例中
    private void recovered(final InstanceMeta instance) {
        if (!providers.contains(instance)) {
            isolatedProviders.remove(instance);
            providers.add(instance);
            instance.setStatus(true);

            log.debug("instance {} is recovered, isolatedProviders={}, providers={}", instance, isolatedProviders, providers);
        }
    }

    //开启一个异步线程将隔离服务加入到半开服务中，用于进行探活
    private void halfOpen() {

        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);

        log.debug(" ====> halfOpenProviders: {}", halfOpenProviders);
    }


    private void isolated(final InstanceMeta instance) {
        log.debug(" ==> isolate instance: {}", instance);
        providers.remove(instance);
        log.debug(" ==> providers = {}", providers);
        isolatedProviders.add(instance);
        instance.setStatus(false);
        log.debug(" ==> isolatedProviders = {}", isolatedProviders);
    }
}
