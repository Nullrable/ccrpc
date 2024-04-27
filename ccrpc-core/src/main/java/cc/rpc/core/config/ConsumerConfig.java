package cc.rpc.core.config;

import cc.rpc.core.api.Filter;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.api.RpcContext;
import cc.rpc.core.cluster.GrayRouter;
import cc.rpc.core.cluster.RoundRibbonLoadBalancer;
import cc.rpc.core.consumer.ConsumerBootstrap;
import cc.rpc.core.filter.ContextParameterFilter;
import cc.rpc.core.registry.cc.CcRegisterCenter;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Data
@Slf4j
@Import({CcRegistryProperties.class, ConsumerProperties.class, AppProperties.class, ZkProperties.class})
public class ConsumerConfig {

    @Resource
    private ConsumerProperties consumerProperties;

    @Resource
    private AppProperties appProperties;

    @Resource
    private ZkProperties zkProperties;

    @Resource
    private CcRegistryProperties ccregistryProperties;

    @Bean
    @Primary
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundRibbonLoadBalancer();
    }
    @Bean
    public Router router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }
    @Bean
    @ConditionalOnMissingBean
    public RegisterCenter registerRegistryCenter() {
        return new CcRegisterCenter(ccregistryProperties);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }

    @Bean
    public Filter filter() {
        return new ContextParameterFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    ApolloConfigRefresher provider_apolloChangedListener() {
        return new ApolloConfigRefresher();
    }

    @Bean
    public RpcContext rpcContext(@Autowired LoadBalancer loadBalancer,
                                 @Autowired Router router,
                                 @Autowired List<Filter> filters) {

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.setConsumerProperties(consumerProperties);
        context.setAppProperties(appProperties);
        log.debug(" =========> consumer rpc context: {}", context);

        return context;
    }

}
