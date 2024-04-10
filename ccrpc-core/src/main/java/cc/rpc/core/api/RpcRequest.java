package cc.rpc.core.api;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.ToString;

/**
 * @author nhsoft.lsd
 */
@Data
@ToString
public class RpcRequest {

    /**
     * service class.
     */
    private String service;

    /**
     * 方法签名.
     */
    private String methodSign; // 改为MethodSign。原因是相同方法名称，参数不同或者参数个数不同

    /**
     * 参数.
     */
    private Object[] args;  // 参数： 100e

    /**
     * 跨调用方需要传递的参数
     */
    private Map<String, String> parameters = new HashMap<>();
}
