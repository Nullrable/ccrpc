package cc.rpc.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;

/**
 * @author nhsoft.lsd
 */
public class MockUtil {

    public static Object mock(Method method) {
        //
        if (method.getReturnType().isAssignableFrom(Map.class)) {

        } else if (method.getReturnType().isArray()) {

        } else if (method.getReturnType().isAssignableFrom(List.class)) {
            Type genericSuperclass = method.getGenericReturnType();
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                Type actureType = typeArguments[0];
                if (actureType instanceof Class<?>) {
                    Class<?> genericClass = (Class<?>) actureType;
                    // genericClass即为范型类的类型参数
                    //TODO
                } else if (actureType instanceof ParameterizedType parameterizedTypeInner) {
                    List<Object> returnList = new ArrayList<>();
//                    jsonArray.forEach(s -> {
//                        if (s instanceof Map<?, ?> itemMap) {
//                            Map resultMap = new HashMap();
//
//                            Class<?> keyType = (Class<?>)parameterizedTypeInner.getActualTypeArguments()[0];
//                            Class<?> valueType = (Class<?>)parameterizedTypeInner.getActualTypeArguments()[1];
//
//                            itemMap.entrySet().forEach(x -> {
//                                Object key = TypeUtil.cast(keyType, x.getKey());
//                                Object value = TypeUtil.cast(valueType, x.getValue());
//                                resultMap.put(key, value);
//                            });
//                            returnList.add(resultMap);
//                        } else {
//                            log.info("not match " + s);
//                        }
//                    });
                    return returnList;
                }
            } else {
                return null;
            }

        }

        return null;
    }


    //TODO nhsoft.lsd 缺少对泛型的支持
    public static Object mock(Class type) {
        if(type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if(type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 10000L;
        }
        if(Number.class.isAssignableFrom(type)) {
            return 1;
        }
        if(type.equals(String.class)) {
            return "this_is_a_mock_string";
        }

        return mockPojo(type);
    }

    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Class<?> fType = f.getType();
            Object fValue = mock(fType);
            f.set(result, fValue);
        }
        return result;
    }
}