package cc.rpc.core.consumer;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nhsoft.lsd
 */
@Configuration
@Data
public class ConsumerConfig {

    @Value("${ccrpc.provider:null}")
    private String provider;

    @Bean
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }
}
