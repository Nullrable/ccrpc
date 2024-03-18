package cc.rpc.core.api;

import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
public class RpcRequest {

    private String clazz;

    private String methodSign; // 改为MethodSign。原因是相同方法名称，参数不同或者参数个数不同

    private Object[] args;  // 参数： 100
}
