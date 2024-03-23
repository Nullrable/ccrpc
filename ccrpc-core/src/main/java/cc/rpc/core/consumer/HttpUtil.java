package cc.rpc.core.consumer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
public class HttpUtil {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient okHttpClient = new OkHttpClient();

    static {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(1, TimeUnit.SECONDS)//设置读取超时时间
                .build();
    }

    public static ResponseBody post(final String url, final String json) {
        //TODO nhsoft.lsd 通行方式，序列化
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(url).post(body).build();

        try {
            return okHttpClient.newCall(request).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResponseBody get(final String url, final Map<String, String> params) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        HttpUrl.Builder builder = httpUrl.newBuilder();
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addQueryParameter(key, params.get(key));
            }
        }

        Request request = new Request.Builder()
                .url(builder.build())
                .build();

        try {
            return okHttpClient.newCall(request).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
