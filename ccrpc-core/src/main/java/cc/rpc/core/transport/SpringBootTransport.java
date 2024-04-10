package cc.rpc.core.transport;

import cc.rpc.core.api.RpcContext;
import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.provider.ProviderInvoker;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nhsoft.lsd
 */
@RestController
public class SpringBootTransport {

    @Resource
    private ProviderInvoker providerInvoker;

    @PostMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

        if (RpcContext.getContext() != null) {
            request.getParameters().forEach(RpcContext::put);
        }

        RpcResponse<?> response = providerInvoker.invoke(request);

        RpcContext.clear();

        return response;
    }
}
