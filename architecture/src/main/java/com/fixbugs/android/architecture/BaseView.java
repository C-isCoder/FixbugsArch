package com.fixbugs.android.architecture;

public interface BaseView<T> {
    void setPresenter(T presenter);

    void showMessage(String message);

    void showMessage(int messageId);

    void tipsSuccess(String message);

    void tipsError(String error);

    void showProgress();

    void hideProgress();
}
