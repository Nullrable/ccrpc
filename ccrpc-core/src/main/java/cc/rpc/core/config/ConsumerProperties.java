package cc.rpc.core.config;

import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

/**
 * @author nhsoft.lsd
 */

@ConfigurationProperties(prefix = "ccrpc.consumer")
@ToString
@Setter
public class ConsumerProperties {

    @Resource
    private Environment environment;

    private int  retries = 1;

    private int grayRatio = 0;

    private int connectTimeout = 5000;

    private int readTimeout = 180_000;

    private int halfOpenInitialDelay = 10;

    private int halfOpenDelay = 60;

    private int faultLimit = 10;

    public int getRetries() {
        return environment.getProperty("ccrpc.consumer.retries", Integer.class, 1);
    }

    public int getGrayRatio() {
        Integer value = environment.getProperty("ccrpc.consumer.grayRatio", Integer.class);
        if (value != null) {
            return value;
        }
        return environment.getProperty("ccrpc.consumer.gray-ratio", Integer.class, 0);
    }

    public int getConnectTimeout() {
        Integer value = environment.getProperty("ccrpc.consumer.connectTimeout", Integer.class);
        if (value != null) {
            return value;
        }
        return environment.getProperty("ccrpc.consumer.connect-timeout", Integer.class, 5000);
    }

    public int getReadTimeout() {
        Integer value = environment.getProperty("ccrpc.consumer.readTimeout", Integer.class);
        if (value != null) {
            return value;
        }
        return environment.getProperty("ccrpc.consumer.read-timeout", Integer.class, 180_000);
    }

    public int getHalfOpenInitialDelay() {
        Integer value = environment.getProperty("ccrpc.consumer.halfOpenInitialDelay", Integer.class);
        if (value != null) {
            return value;
        }
        return environment.getProperty("ccrpc.consumer.half-open-initial-delay", Integer.class, 10);
    }

    public int getHalfOpenDelay() {
        Integer value = environment.getProperty("ccrpc.consumer.halfOpenDelay", Integer.class);
        if (value != null) {
            return value;
        }

        return environment.getProperty("ccrpc.consumer.half-open-delay", Integer.class, 60);
    }

    public int getFaultLimit() {
        Integer value = environment.getProperty("ccrpc.consumer.faultLimit", Integer.class);
        if (value != null) {
            return value;
        }
        return environment.getProperty("ccrpc.consumer.fault-limit", Integer.class, 10);
    }
}
