package cc.rpc.core.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nhsoft.lsd
 */

@ConfigurationProperties(prefix = "ccrpc.provider")
@Data
public class ProviderProperties {

    private Map<String, String> metas = new HashMap<>();
}
