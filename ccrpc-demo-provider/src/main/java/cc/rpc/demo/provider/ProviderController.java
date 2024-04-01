package cc.rpc.demo.provider;

import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.provider.ProviderBootstrap;
import cc.rpc.core.provider.ProviderInvoker;
import cc.rpc.demo.api.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nhsoft.lsd
 */
@RestController
public class ProviderController {

    @Resource
    private ProviderInvoker providerInvoker;

    @Resource
    UserService userService;


    @PostMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

        return providerInvoker.invoke(request);

    }

    @GetMapping("/setPorts")
    public RpcResponse setPorts(@RequestParam("ports") String ports) {
        userService.setPort(ports);
        RpcResponse response = new RpcResponse(true, ports, null);
        return response;

    }


}
