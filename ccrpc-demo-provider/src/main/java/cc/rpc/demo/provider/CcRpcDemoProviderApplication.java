package cc.rpc.demo.provider;

import cc.rpc.core.config.ProviderConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ProviderConfig.class)
public class CcRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcRpcDemoProviderApplication.class, args);
    }

}
