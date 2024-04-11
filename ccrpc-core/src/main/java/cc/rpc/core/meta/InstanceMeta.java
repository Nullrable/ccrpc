package cc.rpc.core.meta;

import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author nhsoft.lsd
 */
@Data
public class InstanceMeta {

    private String scheme;

    private String host;

    private int port;

    private String context;

    private boolean status = true; // online or offline

    private Map<String, String> parameters = new HashMap<>();  // idc  A B C

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
        return new InstanceMeta("http", host, port, "ccrpc");
    }

    public String toMetas() {
       return JSON.toJSONString(this.getParameters());
    }

}
