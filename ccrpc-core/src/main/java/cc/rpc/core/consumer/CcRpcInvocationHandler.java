package cc.rpc.core.consumer;

import cc.rpc.core.api.CcRpcException;
import cc.rpc.core.api.Filter;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.consumer.http.OkHttpInvoker;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.util.MethodUtil;
import cc.rpc.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class CcRpcInvocationHandler implements InvocationHandler {

    private Class<?> service;

    private RpcContext context;

    private List<InstanceMeta> providers;

    private HttpInvoker httpInvoker;

    public CcRpcInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;

        int readTimeout = Integer.parseInt(context.getParameters().getOrDefault("app.read-timout", "0"));
        int connectTimeout = Integer.parseInt(context.getParameters().getOrDefault("app.connect-timout", "0"));

        this.httpInvoker = new OkHttpInvoker(connectTimeout, readTimeout);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        log.info(" ===> request: " + JSON.toJSONString(args));

        RpcRequest request = new RpcRequest();
        request.setService(service.getName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);

        int retries = Integer.parseInt(context.getParameters().getOrDefault("app.retries", "1"));

        for (int i = 0; i < retries; i++) {
            List<Filter> filters = context.getFilters();
            if (filters != null && !filters.isEmpty()) {
                for (Filter filter : filters) {
                    Object result = filter.preFilter(request);
                    if (result != null) {
                        return result;
                    }
                }
            }

            List<String> urls = providers.stream().map(InstanceMeta::toUrl).collect(Collectors.toList());

            List<String> providers = context.getRouter().route(urls);
            String provider = context.getLoadBalancer().choose(providers);

            ResponseBody responseBody;
            try {
                log.info(" ========> retries: " + i + " invoker url: " + provider);
                responseBody = httpInvoker.post(provider, request);
            } catch (Exception ex){
                log.error(ex.getMessage(), ex);
                //如果读取超时，则进行重试
                if (ex instanceof SocketTimeoutException) {
                    if (retries - 1 == i) {
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
            log.info(" ===> result: " + resultJson);

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
                return result;
            } else {
                Exception ex = rpcResponse.getEx();
                throw new CcRpcException(ex);
            }
        }
        throw new CcRpcException(CcRpcException.APP_RETRIES_MUST_GATHER_THAN_ZERO);
    }
}
