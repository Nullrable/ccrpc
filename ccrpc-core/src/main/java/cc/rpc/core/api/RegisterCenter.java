package cc.rpc.core.api;

import java.util.List;

/**
 * @author nhsoft.lsd
 */
public interface RegisterCenter {

    void start();

    void stop();

    void register(String service, String url);

    void unregister(String service, String url);

    List<String> fetchAll(String service);

    void subscribe();

    class StaticRegisterCenter implements RegisterCenter {

        private List<String> providers;

        public StaticRegisterCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

            System.out.println("register center start");

        }

        @Override
        public void stop() {
            System.out.println("register center stop");
        }

        @Override
        public void register(final String service, final String url) {

        }

        @Override
        public void unregister(final String service, final String url) {

        }

        @Override
        public List<String> fetchAll(final String service) {
            System.out.println(service + " fetch providers from register center");
            return providers;
        }

        @Override
        public void subscribe() {

        }
    }
}
