package com.fixbugs.android.request;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by iCong.
 *
 * Date:2017年7月6日
 */

public class HttpObserver<T> extends DisposableObserver<T> {
    private static final String TAG = "HttpLogger";
    private HttpListener<T> mResultListener;

    public HttpObserver(HttpListener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("HttpListener not null");
        }
        mResultListener = listener;
    }

    @Override public void onError(Throwable e) {
        mResultListener.onComplete();
        mResultListener.error(e);
    }

    @Override protected void onStart() {
        super.onStart();
        mResultListener.onStart();
    }

    @Override public void onComplete() {
        mResultListener.onComplete();
    }

    @Override public void onNext(T t) {
        mResultListener.success(t);
    }
}
