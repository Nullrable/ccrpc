package cc.rpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
@AllArgsConstructor
public class RpcResponse<T> {

    private boolean status;
    private T data;
    private Exception ex;
}
