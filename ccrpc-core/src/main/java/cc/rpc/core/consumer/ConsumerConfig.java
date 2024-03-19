package cc.rpc.core.consumer;

import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Data
public class ConsumerConfig {

    @Value("${ccrpc.provider:null}")
    private List<String> providers;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner createConsumerApplicationRunner(ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }
}
