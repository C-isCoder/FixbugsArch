package com.fixbugs.android.request;

import io.reactivex.observers.DisposableObserver;

public class HttpResult<T> extends DisposableObserver<T> {
    private static final String TAG = "HttpLogger";
    private Listener<T> mResultListener;

    public HttpResult(Listener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("HttpListener not null");
        }
        mResultListener = listener;
    }

    @Override public void onError(Throwable e) {
        mResultListener.result(false, null, e);
    }

    @Override protected void onStart() {
        super.onStart();
        mResultListener.result(true, null, null);
    }

    @Override public void onComplete() {
        mResultListener.result(false, null, null);
    }

    @Override public void onNext(T t) {
        mResultListener.result(false, t, null);
    }

    public interface Listener<T> {
        void result(boolean isStart, T t, Throwable e);
    }
}
