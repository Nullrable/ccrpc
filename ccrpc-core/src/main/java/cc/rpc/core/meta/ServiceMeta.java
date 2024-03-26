package cc.rpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
@Builder
public class ServiceMeta {

    private String app;

    private String namespace;

    private String env;

    private String service;

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, service);
    }
}
