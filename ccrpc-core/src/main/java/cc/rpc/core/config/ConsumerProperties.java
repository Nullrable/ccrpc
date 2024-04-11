package cc.rpc.core.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nhsoft.lsd
 */

@ConfigurationProperties(prefix = "ccrpc.consumer")
@Data
@ToString
public class ConsumerProperties {

    private int  retries = 1;

    private int grayRatio = 0;

    private int connectTimeout = 5000;

    private int readTimeout = 1000;

    private int halfOpenInitialDelay = 10;

    private int halfOpenDelay = 60;

    private int faultLimit = 10;

    private int qps = 100;
}
