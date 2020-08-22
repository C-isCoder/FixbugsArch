package com.fixbugs.android.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.fixbugs.android.config.Configuration;

/**
 * Created by iCong.
 * Time:2016/12/5-11:31.
 */

class NetWorkTool {
    static boolean isNetworkConnected() {
        final String s = Context.CONNECTIVITY_SERVICE;
        Context c = Configuration.get().getAppContext();
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(s);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
}
