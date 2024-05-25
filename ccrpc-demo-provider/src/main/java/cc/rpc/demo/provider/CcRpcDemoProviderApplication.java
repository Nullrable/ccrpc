package cc.rpc.demo.provider;

import cc.rpc.core.config.ProviderConfig;
import io.cc.config.client.annotation.EnableCcConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ProviderConfig.class)
//@EnableApolloConfig
@EnableCcConfig
@Slf4j
public class CcRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcRpcDemoProviderApplication.class, args);
    }
}
