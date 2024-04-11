package cc.rpc.core.cluster;

import cc.rpc.core.api.Router;
import cc.rpc.core.meta.InstanceMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class GrayRouter implements Router {

    private Integer grayRatio;

    public GrayRouter(final Integer grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(final List<InstanceMeta> providers) {

        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(p -> {

            log.debug(" ======> gray router instance meta {} ", p);

            if("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        if (normalNodes.isEmpty() || grayNodes.isEmpty()) {
            return providers;
        }

        if (grayRatio <= 0) {
            //返回非灰度节点
           return normalNodes;
        } else if (grayRatio >= 100) {
            //返回灰度节点
            return grayNodes;
        } else {
            Random random = new Random();
            if (random.nextInt(100) <= grayRatio) {
                //返回灰度节点
                return grayNodes;
            } else {
                //返回非灰度节点
                return normalNodes;
            }
        }
    }
}
