package cc.rpc.core.provider;

import cc.rpc.core.annotation.CcProvider;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.meta.ProviderMeta;
import cc.rpc.core.util.MethodUtil;
import cc.rpc.core.util.TypeUtil;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.TypeUtils;

/**
 * @author nhsoft.lsd
 */
public class ProviderBootstrap implements ApplicationContextAware {

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(CcProvider.class);

        providers.values().forEach(this::genInterface);
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
            Object result = method.invoke(bean, cast(method, request.getArgs()));
            return new RpcResponse(true, result, null);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new RpcResponse(false, null, new RuntimeException(e.getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new RpcResponse(false, null, new RuntimeException(e.getMessage()));
        }
    }

    private Object[] cast(Method method, Object[] args) {
        if (args == null) {
            return null;
        }
        Object[] result = new Object[args.length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {

            Class parameterClass = method.getParameterTypes()[i];
            Object parameter = args[i];
            if (parameter instanceof List parameterList) {
              Type genericParameterTypes =  method.getGenericParameterTypes()[i];
              if (genericParameterTypes instanceof ParameterizedType parameterizedType) {

                  Type actureType = parameterizedType.getActualTypeArguments()[0];

                  if (actureType instanceof Class<?>) {
                      System.out.println("=======> list type  : " + actureType);
                      List<Object> returnList = new ArrayList<>();
                      parameterList.forEach(x -> {
                          returnList.add(TypeUtil.cast((Class<?>)actureType, x));
                      });
                      result[i] = returnList;
                  } else if(actureType instanceof ParameterizedType parameterizedTypeInner) {
                      List<Object> returnList = new ArrayList<>();
                      parameterList.forEach(s -> {
                          if ( s instanceof Map<?, ?> itemMap) {
                              Map resultMap = new HashMap();

                              Class<?> keyType = (Class<?>)parameterizedTypeInner.getActualTypeArguments()[0];
                              Class<?> valueType = (Class<?>)parameterizedTypeInner.getActualTypeArguments()[1];
                              System.out.println("=======> map keyType  : " + keyType);
                              System.out.println("=======> map valueType: " + valueType);

                              itemMap.entrySet().forEach(x -> {
                                  Object key = TypeUtil.cast(keyType, x.getKey());
                                  Object value = TypeUtil.cast(valueType, x.getValue());
                                  resultMap.put(key, value);
                              });
                              returnList.add(resultMap);
                          } else {
                              System.out.println("not match " + s);
                          }
                      });
                      result[i] = returnList;
                  } else {
                      System.out.println("=======>未找到泛型" + actureType);
                  }
              }
            } else if (parameter instanceof Map<?, ?> parameterMap) {
                Map resultMap = new HashMap();
                Type genericParameterTypes =  method.getGenericParameterTypes()[i];
                if (genericParameterTypes instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                    System.out.println("=======> map keyType  : " + keyType);
                    System.out.println("=======> map valueType: " + valueType);

                    parameterMap.entrySet().forEach(x -> {
                        Object key = TypeUtil.cast(keyType, x.getKey());
                        Object value = TypeUtil.cast(valueType, x.getValue());
                        resultMap.put(key, value);
                    });
                }
                result[i] = resultMap;
            } else {
                Object arg = TypeUtil.cast(parameterClass, parameter);
                result[i] = arg;
            }
        }
        return result;
    }
}
