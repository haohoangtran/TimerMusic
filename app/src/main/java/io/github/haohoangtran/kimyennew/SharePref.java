package io.github.haohoangtran.kimyennew;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePref {

    private static final String SHARE_PREFS_NAME = "SP";
    private static final String ONOFF_KEY = "ONOFF";
    private static final String PATH_MUSIC = "MUSIC";
    private SharedPreferences sharedPreferences;
    private static SharePref instance;

    public SharePref(Context context) {
        this.sharedPreferences = context.getSharedPreferences(
                SHARE_PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public static void init(Context context) {
        instance = new SharePref(context);
    }

    public static SharePref getInstance() {
        return instance;
    }

    public void saveOnOff(boolean isCheck) {
        sharedPreferences.edit().putString(ONOFF_KEY, isCheck ? "1" : "0").apply();
    }

    public void savePathRunning(String path) {
        sharedPreferences.edit().putString(PATH_MUSIC, path).apply();
    }

    public String getPathMusic() {
        return this.sharedPreferences.getString(PATH_MUSIC, "");
    }

    public boolean isOn() {
        return this.sharedPreferences.getString(ONOFF_KEY, "0").equals("1");
    }
}
