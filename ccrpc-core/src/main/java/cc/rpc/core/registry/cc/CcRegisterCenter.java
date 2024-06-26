package cc.rpc.core.registry.cc;

import cc.rpc.core.api.RegisterCenter;
import cc.rpc.core.config.CcRegistryProperties;
import cc.rpc.core.meta.InstanceMeta;
import cc.rpc.core.meta.ServiceMeta;
import cc.rpc.core.registry.ChangedListener;
import cc.rpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.util.LinkedMultiValueMap;

/**
 * @author nhsoft.lsd
 */
@Slf4j
public class CcRegisterCenter implements RegisterCenter {

    private static final long LONG_POLLING_READ_TIMEOUT = 90_000;

    private static OkHttpClient okHttpClient;

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private static final long CONNECT_TIMEOUT_MILLIS = 1000;

    private static final long READ_TIMEOUT_MILLIS = 50_000;

    private Map<String, Long> VERSIONS = new HashMap<>();

    private List<String> servers;

    private long refreshInterval;

    private LinkedMultiValueMap<InstanceMeta, ServiceMeta> serviceMetaMap = new LinkedMultiValueMap<>();

    private ScheduledExecutorService heartBeatExecutor = new ScheduledThreadPoolExecutor(1);

    private ScheduledExecutorService subscribeExecutor = new ScheduledThreadPoolExecutor(1);

    private RandomRegisterServerLoadBalancer registerServerLoadBalancer;

    public CcRegisterCenter(final CcRegistryProperties properties) {
        this.servers = properties.getServers();
        this.refreshInterval = properties.getRefreshInterval();
        this.registerServerLoadBalancer = new RandomRegisterServerLoadBalancer();
    }

    @Override
    public void start() {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)//设置连接超时时间
                .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)//设置读取超时时间
                .build();
    }

    @Override
    public void stop() {
        heartBeatExecutor.shutdown();
    }

    public void heartbeat() {
        //这个是个provider用的，consumer 不需要的
        heartBeatExecutor.scheduleWithFixedDelay(() -> {
            String leaderUrl = leader();

            serviceMetaMap.keySet().parallelStream().forEach(instance -> {


                List<ServiceMeta> serviceMetas = serviceMetaMap.get(instance);
                String services = serviceMetas.stream().map(ServiceMeta::toPath).collect(Collectors.joining(","));

                RequestBody body = RequestBody.create(JSON_MEDIA, JSON.toJSONString(instance));
                Request req = new Request.Builder().url(leaderUrl + "/heartbeat?services=" + services).post(body).build();
                try (Response response = okHttpClient.newCall(req).execute()) {
                    log.info(" ====>>>> heartbeat success service = {}, response: {}", services, response.body().string());
                } catch (IOException e) {
                    log.error(" ====>>>> heartbeat failed service = {}", services);
                }
            });

        }, 5, 5, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private String leader() {

        Request req = new Request.Builder().url(registerServerLoadBalancer.chooseOneFrom(servers) + "/cluster").build();
        List<Server> serverInstants = new ArrayList<>();
        try (ResponseBody responseBody = okHttpClient.newCall(req).execute().body()) {
            assert responseBody != null;
            String json = responseBody.string();

            List<JSONObject> list = JSON.parseObject(json, List.class);
            list.forEach(s -> serverInstants.add(s.toJavaObject(Server.class)));
        }
        Server server = null;
        while (server == null) {
            server = serverInstants.stream().filter(Server::isLeader).findFirst().orElse(null);
            Thread.sleep(1000);
        }
        return server.getUrl();
    }

    @SneakyThrows
    @Override
    public void register(final ServiceMeta service, final InstanceMeta instance) {
        RequestBody body = RequestBody.create(JSON_MEDIA, JSON.toJSONString(instance));
        Request req = new Request.Builder().url(leader() + "/register?service=" + service.toPath()).post(body).build();
        okHttpClient.newCall(req).execute();

        serviceMetaMap.add(instance, service);

        log.info("register service: {}", service.toPath());

    }

    @SneakyThrows
    @Override
    public void unregister(final ServiceMeta service, final InstanceMeta instance) {
        RequestBody body = RequestBody.create(JSON_MEDIA, JSON.toJSONString(instance));
        Request req = new Request.Builder().url(leader() + "/unregister?service=" + service.toPath()).post(body).build();
        okHttpClient.newCall(req).execute();

        serviceMetaMap.remove(instance, service);

        log.info("unregister service: {}", service.toPath());
    }

    @SneakyThrows
    @Override
    public List<InstanceMeta> fetchAll(final ServiceMeta service) {

        Request req = new Request.Builder().url(registerServerLoadBalancer.chooseOneFrom(servers) + "/fetchAll?service=" + service.toPath()).build();
        String json;
        try (ResponseBody responseBody = okHttpClient.newCall(req).execute().body()) {
            assert responseBody != null;
            json = responseBody.string();
        }
        List<JSONObject> list = JSON.parseObject(json, List.class);
        List<InstanceMeta> instanceMetas = new ArrayList<>();
        list.forEach(s -> instanceMetas.add(s.toJavaObject(InstanceMeta.class)));

        log.info("fetchAll service: {} {}", service.toPath(), instanceMetas);


        return instanceMetas;
    }

    @SneakyThrows
    @Override
    public void subscribe(final ServiceMeta service, final ChangedListener listener) {
        if (refreshInterval < 0) {
            longPulling(service, listener);
        } else {
            scheduleWithFixedDelay(service, listener);
        }
    }

    private void longPulling(final ServiceMeta service, final ChangedListener listener) {
        log.info(" ====>>>> long pulling subscribe server: {}", service.toPath());
        AtomicReference<String> lastServerUrl = new AtomicReference<>();
        subscribeExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                long startTime = System.currentTimeMillis();
                if (lastServerUrl.get() == null) {
                    lastServerUrl.set(registerServerLoadBalancer.chooseOneFrom(servers));
                }

                //服务端设置long pulling timeout 为 60s.
                Request req = new Request.Builder()
                        .url(lastServerUrl.get() + "/subscribe?service=" + service.toPath())
                        // 设置请求的读取超时时间
                        .tag(OkHttpClient.class, okHttpClient.newBuilder().readTimeout(LONG_POLLING_READ_TIMEOUT, TimeUnit.MILLISECONDS).build())
                        .build();

                try (Response response = okHttpClient.newCall(req).execute()) {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        List<JSONObject> list = JSON.parseObject(json, List.class);
                        List<InstanceMeta> instanceMetas = new ArrayList<>();
                        list.forEach(s -> instanceMetas.add(s.toJavaObject(InstanceMeta.class)));
                        listener.fire(new Event(instanceMetas));

                        log.info(" ====>>>> long pulling service changed: {}, {}", service.toPath(), instanceMetas);
                    } else {
                        log.info(" ====>>>> long pulling response failed: {}", response.body().string());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                long diffTime = System.currentTimeMillis() - startTime;
                if (diffTime < 1000) {
                    log.info(" ====>>>> long pulling waiting: {}", service.toPath());
                    try {
                        Thread.sleep(1000 - diffTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
    }

    private void scheduleWithFixedDelay(final ServiceMeta service, final ChangedListener listener) {
        AtomicReference<String> lastServerUrl = new AtomicReference<>();
        subscribeExecutor.scheduleWithFixedDelay(() -> {
            Long currentVersion = VERSIONS.getOrDefault(service.toPath(), -1L);

            if (lastServerUrl.get() == null) {
                lastServerUrl.set(registerServerLoadBalancer.chooseOneFrom(servers));
            }

            Request req = new Request.Builder().url(lastServerUrl.get() + "/version?service=" + service.toPath()).build();
            try (Response response = okHttpClient.newCall(req).execute()) {
                Long lastVersion = Long.parseLong(response.body().string());

                log.info("current version: {}, last version {}", currentVersion, lastVersion);

                if (currentVersion < lastVersion) {
                    List<InstanceMeta> instanceMetas = fetchAll(service);
                    listener.fire(new Event(instanceMetas));
                    log.info("service changed: {}, {}", service.toPath(), instanceMetas);
                    VERSIONS.put(service.toPath(), lastVersion);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }, 1, refreshInterval, TimeUnit.MILLISECONDS);
    }
}
