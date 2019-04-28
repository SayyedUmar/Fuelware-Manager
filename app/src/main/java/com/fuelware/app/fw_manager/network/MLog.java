package com.fuelware.app.fw_manager.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fuelware.app.fw_manager.BuildConfig;

public class MLog {

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void debugToast(Context context, String msg) {
        if (BuildConfig.IS_LOG_ENABLED)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void debugLongToast(Context context, String msg) {
        if (BuildConfig.IS_LOG_ENABLED)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void d(String key, String val) {
        if (BuildConfig.IS_LOG_ENABLED)
            Log.d(key, val);
    }

    public static void e(String key, String val) {
        if (BuildConfig.IS_LOG_ENABLED)
            Log.e(key, val);
    }

    public static void w(String key, String val) {
        if (BuildConfig.IS_LOG_ENABLED)
            Log.w(key, val);
    }


}
