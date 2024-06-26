package cc.rpc.core.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nhsoft.lsd
 */
@ConfigurationProperties(prefix = "ccregistry")
@Data
public class CcRegistryProperties {

    private List<String> servers;

    /**
     * 刷新注册中心的时间间隔
     */
    private long refreshInterval = -1L;
}
