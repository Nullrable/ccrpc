package cc.rpc.core.api;

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

   private Map<String, String> parameters = new HashMap<>();
}
