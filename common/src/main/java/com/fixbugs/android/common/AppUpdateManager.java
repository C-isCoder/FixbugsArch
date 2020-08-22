package com.fixbugs.android.common;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.fixbugs.android.config.Configuration;
import java.io.File;

/**
 * Created by iscod.
 * Time:2016/11/10-16:37.
 */

public class AppUpdateManager {
    private static final String TAG = AppUpdateManager.class.getSimpleName();

    private Context mContext;
    private File mFilePath;
    File file;
    //返回的安装包url
    private String apkUrl = "";
    private DownloadManager mManager;
    private long id;
    private boolean isDownloadSuccess = false;
    private AppDownloadReceiver receiver;

    private AppUpdateManager(Context context) {
        mContext = context;
        mFilePath = mContext.getExternalFilesDir("Pictures");
    }

    public static AppUpdateManager getInstance(Context context) {
        return new AppUpdateManager(context);
    }

    public void update(String apkUrl) {
        Log.e("appInstall", "开始下载......");
        this.apkUrl = apkUrl;
        registerReceiver();
        downloadApk();
    }

    private void registerReceiver() {
        receiver = new AppDownloadReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        mContext.registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(receiver);
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        if (mManager == null) {
            mManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        Uri downloadUri = Uri.parse(Configuration.get().getApiDownload() + apkUrl);
        DownloadManager.Request rq = new DownloadManager.Request(downloadUri);
        rq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        rq.setMimeType("application/vnd.android.package-archive");
        file = new File(mFilePath, "update.apk");
        if (file.exists()) {
            boolean b = file.delete();
        }
        Uri uri = Uri.fromFile(file);
        rq.setDestinationUri(uri);
        PackageManager manager = mContext.getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(mContext.getPackageName(), 0);
            rq.setTitle(manager.getApplicationLabel(info) + "更新···");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        id = mManager.enqueue(rq);
    }

    /**
     * 安装apk
     */
    private void installApk() {
        try {
            if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
                Uri apkUri =
                  FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".FileProvider",
                    new File(mFilePath, "update.apk"));
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                mContext.startActivity(install);
            } else {
                Intent install = new Intent(Intent.ACTION_VIEW);
                Uri uri = mManager.getUriForDownloadedFile(id);
                install.setDataAndType(uri, "application/vnd.android.package-archive");
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(install);
            }
        } catch (Exception e) {
            Log.e(TAG, "startUpdate error: " + e.toString());
        } finally {
            unregisterReceiver();
        }

        //判读版本是否在7.0以上
        //if (Build.VERSION.SDK_INT >= 24) {
        //
        //    //在AndroidManifest中的android:authorities的值， BuildConfig.APPLICATION_ID = com.test
        //    if (file != null) {
        //        Uri apkUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", file);
        //        Intent install = new Intent(Intent.ACTION_VIEW);
        //        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        //        mContext.startActivity(install);
        //    } else {
        //        Log.e("appInstall---", "文件为空");
        //    }
        //} else {
        //    Log.e("appInstall---", "android版本低于7.0");
        //    Intent install = new Intent(Intent.ACTION_VIEW);
        //    install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        //    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //    mContext.startActivity(install);
        //}
    }

    /**
     * 下载完成广播 and 通知点击广播
     */
    public class AppDownloadReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            final String ACTION_COMPLETE = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
            final String ACTION_CLICKED = DownloadManager.ACTION_NOTIFICATION_CLICKED;
            String action = intent.getAction();
            if (TextUtils.equals(action, ACTION_COMPLETE)) {
                isDownloadSuccess = true;
                showTips();
                installApk();
            } else if (isDownloadSuccess && TextUtils.equals(action, ACTION_CLICKED)) {
                installApk();
            }
        }
    }

    private void showTips() {
        Toast.makeText(mContext, "下载完成，若没有自动安装，请点击通知栏，进行安装", Toast.LENGTH_LONG).show();
    }
}
