package cc.rpc.core.consumer;

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
import java.util.List;
import java.util.stream.Collectors;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
public class CcRpcInvocationHandler implements InvocationHandler {

    private Class<?> service;

    private RpcContext context;

    private List<InstanceMeta> providers;

    private HttpInvoker httpInvoker = new OkHttpInvoker();;

    public CcRpcInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        System.out.println(" ===> request " + JSON.toJSONString(args));

        RpcRequest request = new RpcRequest();
        request.setService(service.getName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);

        List<String> urls = providers.stream().map(InstanceMeta::toUrl).collect(Collectors.toList());

        List<String> providers = context.getRouter().route(urls);
        String provider = context.getLoadBalancer().choose(providers);

        ResponseBody responseBody = httpInvoker.post(provider, request);
        if (responseBody == null) {
            throw new RuntimeException("okhttp response body is null");
        }

        RpcResponse rpcResponse = JSON.parseObject(responseBody.string(), RpcResponse.class);

        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtil.resultCast(data, method);
        } else {
            Exception ex = rpcResponse.getEx();
            throw new RuntimeException(ex);
        }
    }
}
