package cc.rpc.core.api;

import java.util.List;
import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
public class RpcContext {

   private LoadBalancer loadBalancer;

   private Router router;

   private List<Filter> filters;
}
