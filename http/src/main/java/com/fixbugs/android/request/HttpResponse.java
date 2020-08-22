package com.fixbugs.android.request;

/**
 * Created by iCong.
 * Time:2016/6/21-14:46.
 */
public class HttpResponse<T> {

    private T data;
    private long code;
    private String message;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getDataName() {
        return "data";
    }

    public String getCodeName() {
        return "code";
    }

    public String getMsgName() {
        return "message";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "服务器返回信息:"
          + "\nmessage:"
          + message
          + "\ndata:"
          + data.toString()
          + "\ncode:"
          + code;
    }
}
