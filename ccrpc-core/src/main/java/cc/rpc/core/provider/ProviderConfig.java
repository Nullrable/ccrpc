package cc.rpc.core.provider;

import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.consumer.HttpInvoker;
import cc.rpc.core.provider.ProviderBootstrap;
import cc.rpc.core.provider.ProviderInvoker;
import cc.rpc.core.registry.zk.ZkRegisterCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author nhsoft.lsd
 */
@Configuration
public class ProviderConfig {

    @Bean
    public ProviderBootstrap createProviderBootstrap() {
        return new ProviderBootstrap();
    }


    @Value("${ccrpc.zkserver}")
    private String zkserver;

    @Bean
    public RegisterCenter proivderZkRegisterCenter() {
        return new ZkRegisterCenter(zkserver);
    }

    @Bean
    public ProviderInvoker providerInvoker(ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner createConsumerApplicationRunner(ProviderBootstrap providerBootstrap) {
        return x -> {
            providerBootstrap.start();
        };
    }
}
