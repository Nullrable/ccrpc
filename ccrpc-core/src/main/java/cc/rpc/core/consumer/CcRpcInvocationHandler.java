package cc.rpc.core.consumer;

import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.util.MethodUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
public class CcRpcInvocationHandler implements InvocationHandler {

    private Class<?> service;

    private RpcContext context;

    private List<String> providers;

    public CcRpcInvocationHandler(Class<?> clazz, RpcContext context, List<String> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        RpcRequest request = new RpcRequest();
        request.setClazz(service.getName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);

        ResponseBody responseBody = HttpUtil.post(providers.get(0), JSON.toJSONString(request));
        if (responseBody == null) {
            throw new RuntimeException("okhttp response body is null");
        }
        String respJson = responseBody.string();
        System.out.println(" ===> respJson = " + respJson);
        RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);

        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if (data instanceof JSONObject jsonObject) {
               return jsonObject.toJavaObject(method.getReturnType());
            } else {
                return data;
            }
        } else {
            Exception ex = rpcResponse.getEx();
            //ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
