package com.fixbugs.android.request;

import android.text.TextUtils;

/**
 * Created by iCong.
 * Time:2016/6/29-17:23.
 */
public class HttpException extends RuntimeException {

    public static final String SERVICE_ERROR = "请求服务器异常";
    public static final String NETWORK_ERROR = "网络链接异常，请检查网络连接是否畅通。";
    public static final String NET_REQUEST_TIME_OUT = "请求超时";
    public static final String NET_CONNECT_ERROR = "请求超时，连接服务器异常";
    private static final long serialVersionUID = -2199603193956026137L;
    HttpException(String detailMessage) {
        super(detailMessage);
    }

    @Override
    public String getMessage() {
        if (TextUtils.isEmpty(super.getMessage())) {
            return SERVICE_ERROR;
        } else {
            return super.getMessage();
        }
    }
}
