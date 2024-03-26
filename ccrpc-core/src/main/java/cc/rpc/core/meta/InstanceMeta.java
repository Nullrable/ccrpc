package cc.rpc.core.meta;

import java.util.Map;
import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
public class InstanceMeta {

    private String scheme;

    private String host;

    private int port;

    private String context;

    private boolean status; // online or offline
    private Map<String, String> parameters;  // idc  A B C

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "");
    }
}
