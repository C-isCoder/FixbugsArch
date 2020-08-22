package com.fixbugs.android.cache;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import com.tencent.mmkv.MMKV;

public class MMapCache {
    private static final String TAG = MMapCache.class.getSimpleName();

    private static MMKV sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (MMapCache.class) {
                if (sInstance == null) {
                    String rootDir = MMKV.initialize(context);
                    Log.i(TAG, "mmkv root: " + rootDir);
                    sInstance = MMKV.defaultMMKV();
                }
            }
        }
    }

    public static void put(String key, String value) {
        sInstance.encode(key, value);
    }

    public static void put(String key, int value) {
        sInstance.encode(key, value);
    }

    public static void put(String key, boolean value) {
        sInstance.encode(key, value);
    }

    public static void put(String key, double value) {
        sInstance.encode(key, value);
    }

    public static void put(String key, float value) {
        sInstance.encode(key, value);
    }

    public static void put(String key, long value) {
        sInstance.encode(key, value);
    }

    public static void put(String key, byte[] bytes) {
        sInstance.encode(key, bytes);
    }

    public static void put(String key, Parcelable parcelable) {
        sInstance.encode(key, parcelable);
    }

    public static String getString(String key) {
        return sInstance.decodeString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return sInstance.decodeString(key, defaultValue);
    }

    public static boolean getBoolen(String key) {
        return sInstance.decodeBool(key, false);
    }

    public static boolean getBoolen(String key, boolean defaultValue) {
        return sInstance.decodeBool(key, defaultValue);
    }

    public static int getInt(String key) {
        return sInstance.decodeInt(key, 0);
    }

    public static int getIntID(String key) {
        return sInstance.decodeInt(key, -10010);
    }

    public static int getInt(String key, int defaultValue) {
        return sInstance.decodeInt(key, defaultValue);
    }

    public static double getDouble(String key) {
        return sInstance.decodeDouble(key, 0);
    }

    public static double getDouble(String key, double defaultValue) {
        return sInstance.decodeDouble(key, defaultValue);
    }

    public static float getFloat(String key) {
        return sInstance.decodeFloat(key, 0);
    }

    public static float getFloat(String key, float defaultValue) {
        return sInstance.decodeFloat(key, defaultValue);
    }

    public static byte[] getBytes(String key) {
        return sInstance.decodeBytes(key);
    }

    public static <T extends Parcelable> T getParce(String key, Class<T> clazz) {
        return sInstance.decodeParcelable(key, clazz);
    }

    public static void remove(String key) {
        sInstance.removeValueForKey(key);
    }

    public static void remove(String[] keys) {
        sInstance.removeValuesForKeys(keys);
    }

    public static String[] getKeys() {
        return sInstance.allKeys();
    }
}
