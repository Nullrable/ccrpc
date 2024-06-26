package cc.rpc.core.consumer.http;

import cc.rpc.core.api.RpcRequest;
import cc.rpc.core.consumer.HttpInvoker;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author nhsoft.lsd
 */
public class OkHttpInvoker implements HttpInvoker {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient okHttpClient;


    public OkHttpInvoker(int connectTimeout, int readTimeout) {
         okHttpClient = new OkHttpClient.Builder().connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)//设置连接超时时间
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)//设置读取超时时间
                .build();
    }

    @Override
    public ResponseBody post(final String url, final RpcRequest request) throws IOException {
        RequestBody body = RequestBody.create(JSON, com.alibaba.fastjson.JSON.toJSONString(request));
        Request req = new Request.Builder().url(url).post(body).build();
        return okHttpClient.newCall(req).execute().body();
    }
}
