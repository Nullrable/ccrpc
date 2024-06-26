package cc.rpc.core.consumer;

import cc.rpc.core.api.RpcRequest;
import java.io.IOException;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
public interface HttpInvoker {

    ResponseBody post(final String url, final RpcRequest request) throws IOException;
}
