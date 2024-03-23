package cc.rpc.core.consumer;

import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.util.MethodUtil;
import cc.rpc.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        System.out.println(" ===> request " + JSON.toJSONString(args));

        RpcRequest request = new RpcRequest();
        request.setService(service.getName());
        request.setMethodSign(MethodUtil.methodSign(method));
        request.setArgs(args);

        List<String> routeProviders = context.getRouter().route(providers);
        String provider = context.getLoadBalancer().choose(routeProviders);

        ResponseBody responseBody = HttpUtil.post(provider, JSON.toJSONString(request));
        if (responseBody == null) {
            throw new RuntimeException("okhttp response body is null");
        }

        String respJson = responseBody.string();
        System.out.println(" ===> respJson = " + respJson);
        RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);

        if (rpcResponse.isStatus()) {

            Object data = rpcResponse.getData();
            if (data instanceof JSONObject jsonObject) {
                if (method.getReturnType().isAssignableFrom(Map.class)) {
                    Map resultMap = new HashMap();
                    Type genericReturnType = method.getGenericReturnType();
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                        Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                        System.out.println("keyType  : " + keyType);
                        System.out.println("valueType: " + valueType);
                        jsonObject.entrySet().stream().forEach(
                                e -> {
                                    Object key = TypeUtil.cast(keyType, e.getKey());
                                    Object value = TypeUtil.cast(valueType, e.getValue());
                                    resultMap.put(key, value);
                                }
                        );
                    }
                    return resultMap;
                }
                return jsonObject.toJavaObject(method.getReturnType());
            } else if (data instanceof JSONArray jsonArray) {
                if (method.getReturnType().isArray()) {
                    Object array = Array.newInstance(method.getReturnType().getComponentType(), jsonArray.size());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object o = jsonArray.get(i);
                        Array.set(array, i, TypeUtil.cast(method.getReturnType().getComponentType(), o));
                    }
                    return array;
                } else if (method.getReturnType().isAssignableFrom(List.class)) {
                    Type genericSuperclass = method.getGenericReturnType();
                    if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        Class<?> genericClass = (Class<?>) typeArguments[0];
                        // genericClass即为范型类的类型参数
                        return jsonArray.toJavaList(genericClass);
                    } else {
                        return jsonArray.toArray();
                    }
                }
                return data;
            } else {
                return TypeUtil.cast(method.getReturnType(), data);
            }
        } else {
            Exception ex = rpcResponse.getEx();
            //ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
