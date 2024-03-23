package cc.rpc.core.api;

import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
public class RpcContext {

   private LoadBalancer loadBalancer;

   private Router router;
}
