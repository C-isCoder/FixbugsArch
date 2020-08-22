package com.fixbugs.android.request;

import android.text.TextUtils;
import android.util.Log;
import com.fixbugs.android.config.Configuration;
import com.fixbugs.android.library.logger.Logger;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 自定义请求拦截器，处理请求的token，加密，打印日志等
 * Created by iCong on 2016/9/17.
 */
public class LoggerInterceptor implements Interceptor {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final String TAG = "HttpLogger";

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = doRequest(chain.request());
        long startNs = System.nanoTime();
        if (NetWorkTool.isNetworkConnected()) {
            try {
                return doResponse(startNs, chain.proceed(request));
            } catch (SocketTimeoutException e) {
                throw new HttpException(HttpException.NET_CONNECT_ERROR);
            }
        } else {
            throw new HttpException(HttpException.NETWORK_ERROR);
        }
    }

    // 处理请求体
    private Request doRequest(Request request) {
        RequestBody requestBody = request.body();
        String parameter = "";
        try {
            if (requestBody != null && requestBody.contentLength() != 0) {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                parameter = buffer.readString(UTF8);
                buffer.flush();
                buffer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String token = Configuration.get().getToken();
        Logger.i(TAG, "REQUEST\n" +
          "param ->[T_T]     ：" + (TextUtils.isEmpty(parameter) ? "空空如也" : parameter) + "\n" +
          "url   ->[Q_Q]     ：" + request.url() + "\n" +
          "host  ->[@_@]     ：" + request.url().host() + "\n" +
          "token:->[*_*]     ：" + token + "\n" +
          "method->[^_^]     ：" + request.method()
        );
        Request.Builder requestBuilder = request.newBuilder();
        if (!TextUtils.isEmpty(token)) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        return requestBuilder.build();
    }

    // 处理响应体
    private Response doResponse(long startNs, Response response) {
        if (!Configuration.get().isDebug()) {
            return response;
        } else {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            ResponseBody responseBody = response.body();
            assert responseBody != null;
            BufferedSource source = responseBody.source();
            try {
                source.request(Long.MAX_VALUE); // Buffer the entire body.
            } catch (IOException e) {
                Log.e(TAG, "doResponse: ", e);
                e.printStackTrace();
            }
            Buffer responseBuffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    return response;
                }
            }
            if (!isPlaintext(responseBuffer)) {
                return response;
            }
            if (charset != null) {
                Logger.d(TAG, "RESPONSE\n"
                  + "url:" + response.request().url() + "\n"
                  + "timer:" + tookMs + "ms\n"
                  + responseBuffer.clone().readString(charset)
                );
            }
            return response;
        }
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}

