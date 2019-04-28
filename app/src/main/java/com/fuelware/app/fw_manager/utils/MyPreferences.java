package com.fuelware.app.fw_manager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class MyPreferences {

    private MyPreferences() {}

    private static class SingletonHelper{
        private static final MyPreferences INSTANCE = new MyPreferences();
    }

    private static MyPreferences getInstance(){
        return SingletonHelper.INSTANCE;
    }


    private static SharedPreferences getSharedPrefs (Context context) {
        return context.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
    }

    public static String getStringValue (Context context, String key) {
        SharedPreferences prefs = getSharedPrefs(context);
        return prefs.getString(key,"");
    }

    public static int getIntValue (Context context, String key) {
        SharedPreferences prefs = getSharedPrefs(context);
        return prefs.getInt(key,0);
    }

    public static void setStringValue (Context context, String key, String value) {
        SharedPreferences prefs = getSharedPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setValues (Context context, List<String> keys, List<String> values) {
        SharedPreferences prefs = getSharedPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < keys.size(); i++) {
            editor.putString(keys.get(i), values.get(i));
        }
        editor.apply();
    }

    public static void setIntValue (Context context, String key, int value) {
        SharedPreferences prefs = getSharedPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    public static void setBoolValue (Context context, String key, boolean value) {
        SharedPreferences prefs = getSharedPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolValue (Context context, String key) {
        SharedPreferences prefs = getSharedPrefs(context);
        return prefs.getBoolean(key,false);
    }
}
