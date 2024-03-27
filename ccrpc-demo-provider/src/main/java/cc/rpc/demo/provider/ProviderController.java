package cc.rpc.demo.provider;

import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.provider.ProviderBootstrap;
import cc.rpc.core.provider.ProviderInvoker;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nhsoft.lsd
 */
@RestController
public class ProviderController {

    @Resource
    private ProviderInvoker providerInvoker;


    @PostMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

        return providerInvoker.invoke(request);

    }
}
