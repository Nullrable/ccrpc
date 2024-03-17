package cc.rpc.demo.provider;

import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.provider.ProviderBootstrap;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ComponentScan("cc.rpc")
public class CcRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcRpcDemoProviderApplication.class, args);
    }

    @Resource
    private ProviderBootstrap providerBootstrap;


    @PostMapping("/invoke")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

       return providerBootstrap.invoke(request);

    }

}
