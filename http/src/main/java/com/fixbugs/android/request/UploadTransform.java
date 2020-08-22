package com.fixbugs.android.request;

import android.text.TextUtils;
import java.io.File;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by iCong.
 * Retrofit文件上传
 */

public class UploadTransform {
    private static final String FILE_NOT_NULL = "文件不能为空";
    private static final String FILE_TYPE_ERROR = "文件类型错误，Map<String, String> or Map<String, File>";
    private static final String FILE_PATH_NOT_NULL = "文件路径不能为空";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private static final String DEFAULT_FORM_DATA_NAME = "file";

    public static MultipartBody.Part getPart(String path) {
        if (TextUtils.isEmpty(path)) throw new NullPointerException(FILE_PATH_NOT_NULL);
        File file = new File(path);
        return createMultipartBody(DEFAULT_FORM_DATA_NAME, file);
    }

    public static MultipartBody.Part getPart(String name /*FormData Key name*/,
      String path) {
        if (TextUtils.isEmpty(path)) throw new NullPointerException(FILE_PATH_NOT_NULL);
        File file = new File(path);
        return createMultipartBody(name, file);
    }

    public static MultipartBody.Part getPart(File file) {
        return createMultipartBody(DEFAULT_FORM_DATA_NAME, file);
    }

    public static MultipartBody.Part getPart(String name /*FormData Key name*/,
      File file) {
        return createMultipartBody(name, file);
    }

    private static MultipartBody.Part createMultipartBody(String name, File file) {
        if (file.exists()) {
            RequestBody requestFile = RequestBody.create(MEDIA_TYPE, file);
            return MultipartBody.Part.createFormData(name, file.getName(),
              requestFile);
        } else {
            throw new NullPointerException(FILE_NOT_NULL);
        }
    }

    public static <V> List<MultipartBody.Part> getParts(Map<String, V> fileMap) {
        if (fileMap == null || fileMap.isEmpty()) throw new NullPointerException(FILE_NOT_NULL);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (String formDataName : fileMap.keySet()) {
            V v = fileMap.get(formDataName);
            File file;
            if (v instanceof String) {
                file = new File(((String) v));
            } else if (v instanceof File) {
                file = (File) v;
            } else {
                throw new IllegalArgumentException(FILE_TYPE_ERROR);
            }
            if (file.exists()) {
                RequestBody requestFile = RequestBody.create(MEDIA_TYPE, file);
                builder.addFormDataPart(formDataName, file.getName(), requestFile);
            } else {
                throw new NullPointerException(FILE_NOT_NULL);
            }
        }
        return builder.build().parts();
    }

    public static RequestBody getBody(String content) {
        return RequestBody.create(
          MediaType.parse("multipart/form-data"), content);
    }
}
