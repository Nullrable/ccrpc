package cc.rpc.core.api;

import cc.rpc.core.config.AppProperties;
import cc.rpc.core.config.ConsumerProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

/**
 * @author nhsoft.lsd
 */
@Data
@ToString
public class RpcContext {

   private LoadBalancer loadBalancer;

   private Router router;

   private List<Filter> filters;

   private ConsumerProperties consumerProperties;

   private AppProperties appProperties;

   private static final ThreadLocal<Map<String, String>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

   public static String get(String key) {
      return CONTEXT.get().get(key);
   }

   public static void put(String key, String value) {
      CONTEXT.get().put(key, value);
   }

   public static void clear() {
      CONTEXT.get().clear();
   }

   public static Map<String, String> getContext() {
      return CONTEXT.get();
   }
}
