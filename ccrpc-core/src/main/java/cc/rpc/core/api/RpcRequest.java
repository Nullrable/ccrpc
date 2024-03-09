package cc.rpc.core.api;

import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
public class RpcRequest {

    private String clazz;

    private String method;

    private Object[] args;  // 参数： 100
}
