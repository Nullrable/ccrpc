package cc.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nhsoft.lsd
 */
@Data
@ConfigurationProperties(prefix = "ccrpc.app")
public class AppProperties {

    // for app instance
    private String id = "app1";

    private String namespace = "public";

    private String env = "dev";

}
