package cc.rpc.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

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
}
