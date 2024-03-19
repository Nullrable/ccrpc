package cc.rpc.core.meta;

import java.lang.reflect.Method;
import lombok.Data;
import lombok.ToString;

/**
 * @author nhsoft.lsd
 */
@Data
@ToString
public class ProviderMeta {

    private String methodSign;

    private Method method;

    private Object service;
}
