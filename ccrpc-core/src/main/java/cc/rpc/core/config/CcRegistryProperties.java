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
}
