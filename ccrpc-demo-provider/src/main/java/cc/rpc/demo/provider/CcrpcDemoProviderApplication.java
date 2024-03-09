package cc.rpc.demo.provider;

import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.api.RpcResponse;
import cc.rpc.core.provider.ProviderBootstrap;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class CcrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcrpcDemoProviderApplication.class, args);
    }

    @Resource
    ProviderBootstrap providerBootstrap;


    @GetMapping("/invoke")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

       return providerBootstrap.invoke(request);

    }

}
