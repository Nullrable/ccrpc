package cc.rpc.core.registry;

import cc.rpc.core.meta.InstanceMeta;
import java.util.List;
import lombok.Data;

/**
 * @author nhsoft.lsd
 */
@Data
public class Event {

    private List<InstanceMeta> data;

    public Event(final List<InstanceMeta> data) {
        this.data = data;
    }
}
