package com.fixbugs.android.request;

import android.text.TextUtils;
import com.fixbugs.android.config.Configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by iCong
 * <p>
 * Date:2017年7月6日
 */
public class ConverterFactory extends Converter.Factory {
    private static final String TAG = ConverterFactory.class.getSimpleName();
    private final Gson mGson;

    private ConverterFactory() {
        mGson = new GsonBuilder().registerTypeAdapterFactory(ResponseTypeAdapter.FACTORY).create();
        //        mGson = new GsonBuilder().create();
    }

    public static ConverterFactory create() {
        return new ConverterFactory();
    }

    /**
     * 服务器相应处理
     * 根据具体Result API 自定义处理逻辑
     *
     * @return 返回Data相应的实体
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type mType,
      Annotation[] annotations, Retrofit retrofit) {
        return new ResponseBodyConverter<>(mType);//响应
    }

    /**
     * 请求处理
     * request body 我们无需特殊处理，直接返回 GsonConverterFactory 创建的 converter。
     *
     * @return 返回 GsonConverterFactory 创建的 converter
     */
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type mType,
      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return GsonConverterFactory.create()
          .requestBodyConverter(mType, parameterAnnotations, methodAnnotations, retrofit);
    }

    @SuppressWarnings("unchecked")
    private static class ResponseTypeAdapter extends TypeAdapter<HttpResponse> {
        static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == HttpResponse.class) {
                    return (TypeAdapter<T>) new ResponseTypeAdapter(gson);
                }
                return null;
            }
        };

        private final Gson gson;

        ResponseTypeAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, HttpResponse value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.beginObject();
            out.name(value.getCodeName());
            gson.getAdapter(Long.class).write(out, (long) value.getCode());
            out.name(value.getMsgName());
            gson.getAdapter(String.class).write(out, value.getMessage());
            out.name(value.getDataName());
            gson.getAdapter(Object.class).write(out, value.getData());
            out.endObject();
        }

        @Override
        public HttpResponse read(JsonReader in) throws IOException {
            HttpResponse data = new HttpResponse();
            Map<String, Object> dataMap = (Map<String, Object>) readInternal(in);
            if (dataMap == null) return null;
            data.setCode((long) dataMap.get(data.getCodeName()));
            data.setMessage((String) dataMap.get(data.getMsgName()));
            data.setData(dataMap.get(data.getDataName()));
            return data;
        }

        private Object readInternal(JsonReader in) throws IOException {
            if (in == null) return null;
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(readInternal(in));
                    }
                    in.endArray();
                    return list;
                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), readInternal(in));
                    }
                    in.endObject();
                    return map;
                case STRING:
                    return in.nextString();
                case NUMBER:
                    String numberStr = in.nextString();
                    if (numberStr.contains(".") || numberStr.contains("e")
                      || numberStr.contains("E")) {
                        return Double.parseDouble(numberStr);
                    }
                    return Long.parseLong(numberStr);
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * 自定义的result Api处理逻辑
     *
     * @param <T> 泛型
     */
    private class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        // state
        private static final int SUCCESS = 0;
        private static final int TOKEN_ERROR = 100;
        private Type mType;//泛型，当服务器返回的数据为数组的时候回用到

        private ResponseBodyConverter(Type mType) {
            this.mType = mType;
        }

        /**
         * 自定义转换器-处理服务器返回数据
         *
         * @return 返回data的实体or列表
         */
        @Override
        public T convert(ResponseBody response) {
            try {
                String strResponse = response.string();
                if (TextUtils.isEmpty(strResponse)) {
                    throw new HttpException(HttpException.SERVICE_ERROR);
                }
                HttpResponse res = mGson.fromJson(strResponse, HttpResponse.class);
                long code = res.getCode();
                // 服务器状态
                switch ((int) code) {
                    case SUCCESS:
                        return mGson.fromJson(mGson.toJson(res.getData()), mType);
                    case TOKEN_ERROR:
                        Configuration.get().refreshToken();
                        throw new HttpException(res.getMessage());
                    default:
                        throw new HttpException(res.getMessage());
                }
            } catch (IOException e) {
                throw new HttpException(HttpException.SERVICE_ERROR);
            } finally {
                response.close();
            }
        }
    }
}


