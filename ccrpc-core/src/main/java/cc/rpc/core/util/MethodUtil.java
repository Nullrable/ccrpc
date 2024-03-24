package cc.rpc.core.util;

import cc.rpc.core.annotation.CcConsumer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author nhsoft.lsd
 */
public class MethodUtil {

    public static boolean checkLocalMethod(final Method method) {
        //本地方法不代理
        if ("toString".equals(method.getName()) ||
                "hashCode".equals(method.getName()) ||
                "notifyAll".equals(method.getName()) ||
                "equals".equals(method.getName()) ||
                "wait".equals(method.getName()) ||
                "getClass".equals(method.getName()) ||
                "notify".equals(method.getName())) {
            return true;
        }
        return false;
    }

    public static String methodSign(final Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );
        return sb.toString();
    }

    public static List<Field> findAnnotatedField(Class clazz, final Class<? extends Annotation> annotationClass) {

        List<Field> returnFieldList = new ArrayList<>();

        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                boolean isAnnotationPresent = field.isAnnotationPresent(annotationClass);
                if (isAnnotationPresent) {
                    returnFieldList.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return returnFieldList;
    }
}
