package cc.rpc.core.config;

import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.provider.ProviderBootstrap;
import cc.rpc.core.provider.ProviderInvoker;
import cc.rpc.core.registry.cc.CcRegisterCenter;
import cc.rpc.core.transport.SpringBootTransport;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Import({CcRegistryProperties.class, ProviderProperties.class, AppProperties.class, ZkProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port}")
    private int port;

    @Resource
    private ProviderProperties providerProperties;

    @Resource
    private AppProperties appProperties;

    @Resource
    private ZkProperties zkProperties;

    @Resource
    private CcRegistryProperties ccregistryProperties;

    @Bean
    public ProviderBootstrap createProviderBootstrap() {
        return new ProviderBootstrap(port, appProperties, providerProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegisterCenter proivderRegisterCenter() {
        return new CcRegisterCenter(ccregistryProperties);
    }

    @Bean
    public ProviderInvoker providerInvoker(ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @ConditionalOnMissingBean
    ApolloConfigRefresher provider_apolloChangedListener() {
        return new ApolloConfigRefresher();
    }


    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner createConsumerApplicationRunner(ProviderBootstrap providerBootstrap) {
        return x -> {
            providerBootstrap.start();
        };
    }
}
