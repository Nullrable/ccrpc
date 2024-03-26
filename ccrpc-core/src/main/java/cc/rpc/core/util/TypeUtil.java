package cc.rpc.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nhsoft.lsd
 */
public class TypeUtil {

    public static Object cast(Class<?> origin, Object arg) {

        if (arg == null) {
            return null;
        }

        if (arg.getClass().isAssignableFrom(origin)) {
            return arg;
        }

       if (origin.isArray()) {
           if (arg instanceof List<?> argList) {
               Object array =  Array.newInstance(origin.getComponentType(), argList.size());
               for (int i = 0; i < argList.size(); i++) {
                   Array.set(array, i, argList.get(i));
               }
               return array;
           }
       }

       if (arg instanceof Map map) {
           return new JSONObject(map).toJavaObject(origin);
       }

        if (arg instanceof JSONObject jsonObject) {
            return jsonObject.toJavaObject(origin);
        }

        if (origin.equals(Double.class)  || origin.equals(double.class)) {
            return Double.valueOf(arg.toString());
        }else if (origin.equals(Float.class)  || origin.equals(float.class)) {
            return Float.valueOf(arg.toString());
        }else if (origin.equals(Short.class)  || origin.equals(short.class)) {
            return Short.valueOf(arg.toString());
        }else if (origin.equals(Byte.class)  || origin.equals(byte.class)) {
            return Byte.valueOf(arg.toString());
        }else if (origin.equals(Character.class)  || origin.equals(char.class)) {
            return Character.valueOf(arg.toString().charAt(0));
        }else if (origin.equals(Boolean.class)  || origin.equals(boolean.class)) {
            return Boolean.valueOf(arg.toString());
        }else if (origin.equals(Long.class)  || origin.equals(long.class)) {
            return Long.valueOf(arg.toString());
        }else if (origin.equals(Integer.class)  || origin.equals(int.class)) {
            return Integer.valueOf(arg.toString());
        }
       return arg;
    }

    public static Object[] requestCast(final Object[] args, final Method method) {
        if (args == null) {
            return null;
        }
        Object[] result = new Object[args.length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {

            Class parameterClass = method.getParameterTypes()[i];
            Object parameter = args[i];

            if (parameterClass.isArray()) {
                result[i] = TypeUtil.cast(parameterClass, parameter);
            } else {
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
                            System.out.println("=======>未找到泛型1" + actureType);
                        }
                    } else {
                        System.out.println("=======>未找到泛型2" + parameterClass);
                    }
                } else if (parameter instanceof Map<?, ?> parameterMap) {

                    Type genericParameterTypes =  method.getGenericParameterTypes()[i];
                    if (genericParameterTypes instanceof ParameterizedType parameterizedType) {

                        Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                        Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                        System.out.println("=======> map keyType  : " + keyType);
                        System.out.println("=======> map valueType: " + valueType);

                        Map resultMap = new HashMap();
                        parameterMap.entrySet().forEach(x -> {
                            Object key = TypeUtil.cast(keyType, x.getKey());
                            Object value = TypeUtil.cast(valueType, x.getValue());
                            resultMap.put(key, value);
                        });
                        result[i] = resultMap;
                    } else {
                        result[i] = TypeUtil.cast(parameterClass, parameter);
                    }
                } else {
                    Object arg = TypeUtil.cast(parameterClass, parameter);
                    result[i] = arg;
                }
            }
        }
        return result;
    }

    public static Object resultCast(final Object data, final Method method) {
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

                    Type actureType = typeArguments[0];
                    System.out.println("=======> actureType: " + actureType);
                    if (actureType instanceof Class<?>) {
                        Class<?> genericClass = (Class<?>) actureType;
                        // genericClass即为范型类的类型参数
                        return jsonArray.toJavaList(genericClass);
                    } else if (actureType instanceof ParameterizedType parameterizedTypeInner) {
                        List<Object> returnList = new ArrayList<>();
                        jsonArray.forEach(s -> {
                            if (s instanceof Map<?, ?> itemMap) {
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
                        return returnList;
                    }
                } else {
                    return jsonArray.toArray();
                }
            }
            return data;
        } else {
            return TypeUtil.cast(method.getReturnType(), data);
        }
    }
}
