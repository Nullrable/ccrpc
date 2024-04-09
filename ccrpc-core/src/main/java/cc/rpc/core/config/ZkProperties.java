package cc.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nhsoft.lsd
 */
@Data
@ConfigurationProperties(prefix = "ccrpc.zk")
public class ZkProperties {

    private String root = "ccrpc";

    private String server = "localhost:2181";
}

