package cc.rpc.core.api;

import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.registry.ChangedListener;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nhsoft.lsd
 */
public interface RegisterCenter {

    void start();

    void stop();

    void heartbeat();

    void register(ServiceMeta service, InstanceMeta instance);

    void unregister(ServiceMeta service, InstanceMeta instance);

    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangedListener listener);

    @Slf4j
    class StaticRegisterCenter implements RegisterCenter {

        private List<String> providers;

        public StaticRegisterCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
            log.info("static register center start");

        }

        @Override
        public void stop() {
            log.info("static register center stop");
        }

        @Override
        public void heartbeat() {

        }

        @Override
        public void register(final ServiceMeta service, final InstanceMeta instance) {

        }

        @Override
        public void unregister(final ServiceMeta service, final InstanceMeta instance) {

        }

        @Override
        public List<InstanceMeta> fetchAll(final ServiceMeta service) {

            return null;
        }

        @Override
        public void subscribe(final ServiceMeta service, final ChangedListener listener) {

        }
    }
}
