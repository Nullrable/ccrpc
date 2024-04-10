package cc.rpc.core.config;

import cc.rpc.core.api.Filter;
import cc.rpc.core.api.LoadBalancer;
import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.api.Router;
import cc.rpc.core.cluster.GrayRouter;
import cc.rpc.core.cluster.RoundRibbonLoadBalancer;
import cc.rpc.core.consumer.ConsumerBootstrap;
import cc.rpc.core.filter.ContextParameterFilter;
import cc.rpc.core.registry.zk.ZkRegisterCenter;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Import({ConsumerProperties.class, AppProperties.class, ZkProperties.class})
public class ConsumerConfig {

    @Resource
    private ConsumerProperties consumerProperties;

    @Resource
    private AppProperties appProperties;

    @Resource
    private ZkProperties zkProperties;

    @Bean
    @Primary
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap(appProperties, consumerProperties);
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
    public RegisterCenter registerCenter() {
        return new ZkRegisterCenter(zkProperties);
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

}
