package cc.rpc.core.config;

import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.consumer.HttpInvoker;
import cc.rpc.core.provider.ProviderBootstrap;
import cc.rpc.core.provider.ProviderInvoker;
import cc.rpc.core.registry.zk.ZkRegisterCenter;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Import({ProviderProperties.class, AppProperties.class, ZkProperties.class})
public class ProviderConfig {

    @Value("${server.port}")
    private int port;

    @Resource
    private ProviderProperties providerProperties;

    @Resource
    private AppProperties appProperties;

    @Resource
    private ZkProperties zkProperties;

    @Bean
    public ProviderBootstrap createProviderBootstrap() {
        return new ProviderBootstrap(port, appProperties, providerProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterCenter proivderZkRegisterCenter() {
        return new ZkRegisterCenter(zkProperties);
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
