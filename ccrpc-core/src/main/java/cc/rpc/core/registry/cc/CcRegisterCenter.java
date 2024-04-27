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

    private static OkHttpClient okHttpClient;

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private static final long CONNECT_TIMEOUT_MILLIS = 1000;

    private static final long READ_TIMEOUT_MILLIS = 5000;

    private Map<String, Long> VERSIONS = new HashMap<>();

    private List<String> servers;

    private LinkedMultiValueMap<InstanceMeta, ServiceMeta> map = new LinkedMultiValueMap<>();

    private ScheduledExecutorService heartBeatExecutor = new ScheduledThreadPoolExecutor(1);

    private ScheduledExecutorService subscribeExecutor = new ScheduledThreadPoolExecutor(1);


    public CcRegisterCenter(final CcRegistryProperties properties) {
        this.servers = properties.getServers();
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
            String leaderUrl = leader(servers);

            map.keySet().parallelStream().forEach(instance -> {


                List<ServiceMeta> serviceMetas = map.get(instance);
                String services = serviceMetas.stream().map(ServiceMeta::toPath).collect(Collectors.joining(","));

                RequestBody body = RequestBody.create(JSON_MEDIA, JSON.toJSONString(instance));
                Request req = new Request.Builder().url(leaderUrl + "/heartbeat?services=" + services).post(body).build();
                try (Response response = okHttpClient.newCall(req).execute()){

                    log.info("service = {}, heartbeat response: {}", services, response.body().string());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }, 5, 5, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private String leader(List<String> servers) {

        //TODO nhsoft.lsd servers.get(0) 集群模式下处理
        Request req = new Request.Builder().url(servers.get(0) + "/cluster").build();
        List<Server> serverInstants = new ArrayList<>();
        try (ResponseBody responseBody = okHttpClient.newCall(req).execute().body()) {
            assert responseBody != null;
            String json = responseBody.string();

            List<JSONObject> list = JSON.parseObject(json, List.class);
            list.forEach(s -> serverInstants.add(s.toJavaObject(Server.class)));
        }
        Server server =  null;
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
        Request req = new Request.Builder().url(leader(servers) + "/register?service=" + service.toPath()).post(body).build();
        okHttpClient.newCall(req).execute();

        map.add(instance, service);

        log.info("register service: {}", service.toPath());

    }

    @SneakyThrows
    @Override
    public void unregister(final ServiceMeta service, final InstanceMeta instance) {
        RequestBody body = RequestBody.create(JSON_MEDIA, JSON.toJSONString(instance));
        Request req = new Request.Builder().url(leader(servers) + "/unregister?service=" + service.toPath()).post(body).build();
        okHttpClient.newCall(req).execute();

        map.remove(instance, service);

        log.info("unregister service: {}", service.toPath());
    }

    @SneakyThrows
    @Override
    public List<InstanceMeta> fetchAll(final ServiceMeta service) {

        //TODO nhsoft.lsd 如果是集群模式应该随机选取
        Request req = new Request.Builder().url(servers.get(0) + "/fetchAll?service=" + service.toPath()).build();
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

        subscribeExecutor.scheduleWithFixedDelay(() -> {
            Long currentVersion = VERSIONS.getOrDefault(service.toPath(), -1L);

            //TODO nhsoft.lsd servers.get(0) 集群模式下处理
            Request req = new Request.Builder().url(servers.get(0) + "/version?service=" + service.toPath()).build();
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
        }, 1, 1, TimeUnit.SECONDS);



//        Request req = new Request.Builder().url(servers.get(0) + "/subscribe?service=" + service.toPath()).build();
//        try (Response response = okHttpClient.newCall(req).execute()) {
//            if (response.isSuccessful()) {
//                String json = response.body().string();
//                List<JSONObject> list = JSON.parseObject(json, List.class);
//                List<InstanceMeta> instanceMetas = new ArrayList<>();
//                list.forEach(s -> instanceMetas.add(s.toJavaObject(InstanceMeta.class)));
//                listener.fire(new Event(instanceMetas));
//
//                log.info("service changed: {}, {}", service.toPath(), instanceMetas);
//            }
//        } finally {
//            subscribe(service, listener);
//        }
    }
}
