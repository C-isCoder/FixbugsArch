package com.fixbugs.android.config;

import android.content.Context;

/**
 * Created by iscod.
 * Time:2016/12/7-15:40.
 */

public interface IConfiguration {

    String getApiDefaultHost();

    String getApiWebView();

    Context getAppContext();

    String getApiUploadImage();

    String getApiLoadImage();

    String getToken();

    String getApiDownload();

    String getApiUpload();

    void refreshToken();

    boolean isDebug();
}
