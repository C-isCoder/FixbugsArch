package com.fixbugs.android.request;

import com.fixbugs.android.config.Configuration;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by iCong.
 * Time:2017年7月6日
 */
public class HttpsClient {

    private static HttpsClient INSTANCE;
    private static String BaseUrl = Configuration.get().getApiDefaultHost();
    private Retrofit retrofit;
    private Retrofit newUrlRetrofit;

    private HttpsClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //cache目录
        File cacheFile =
          new File(Configuration.get().getAppContext().getCacheDir(), "netWorkCache");
        builder.cache(new Cache(cacheFile, 1024 * 1024 * 50));//50MB
        //自定义请求拦截器
        builder.addInterceptor(new LoggerInterceptor());
        //设置超时
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        //ssl
        builder.sslSocketFactory(HttpsHelper.getSSLContext(), HttpsHelper.getTrustManager());
        //忽略错误
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        OkHttpClient client = builder.build();
        retrofit = new Retrofit.Builder().baseUrl(BaseUrl)
          .addConverterFactory(ConverterFactory.create())
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .client(client)
          .build();
    }

    private HttpsClient(String baseUrl) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //cache目录
        File cacheFile =
          new File(Configuration.get().getAppContext().getCacheDir(), "netWorkCache");
        builder.cache(new Cache(cacheFile, 1024 * 1024 * 50));//50MB
        //自定义请求拦截器
        builder.addInterceptor(new LoggerInterceptor());
        //设置超时
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        //ssl
        builder.sslSocketFactory(HttpsHelper.getSSLContext(), HttpsHelper.getTrustManager());
        //忽略错误
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        OkHttpClient client = builder.build();
        newUrlRetrofit = new Retrofit.Builder().baseUrl(baseUrl)
          .addConverterFactory(ConverterFactory.create())
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .client(client)
          .build();
    }

    /**
     * 默认的BaseUrl = APIConstant.BASE_URL
     */
    public static HttpsClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HttpsClient();
        }
        return INSTANCE;
    }

    /**
     *
     */
    public static HttpsClient getInstance(String baseUrl) {
        return new HttpsClient(baseUrl);
    }

    /**
     * 自定义Service
     *
     * @param service 传入自定义的Service
     */
    public <T> T create(Class<T> service) {
        return retrofit.create(service);
    }

    /**
     * 自定义Service
     *
     * @param service 传入自定义的Service
     */
    public <T> T createNewUrl(Class<T> service) {
        return newUrlRetrofit.create(service);
    }
}
