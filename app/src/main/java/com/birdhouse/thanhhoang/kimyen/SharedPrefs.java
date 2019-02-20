package com.birdhouse.thanhhoang.kimyen;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    private SharedPreferences sharedPreferences;
    private static final String SHARE_PREFS_NAME = "SP";
    private final String USER_KEY = "username";
    private final String PASS_KEY = "password";
    private final String AUTO_KEY = "auto";
    private final String MAIL_KEY = "mail";
    private final String LAST_TIME_KEY = "login_time";
    private static SharedPrefs instance;

    private SharedPrefs(Context context) {
        this.sharedPreferences = context.getSharedPreferences(
                SHARE_PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public static void init(Context context) {
        SharedPrefs.instance = new SharedPrefs(context);
    }

    public void putLastTime(Long login_time) {
        sharedPreferences.edit().putLong(LAST_TIME_KEY, login_time).apply();
    }

    public void putEmail(String username) {
        sharedPreferences.edit().putString(MAIL_KEY, username).apply();
    }

    public void putPassword(String pass) {
        sharedPreferences.edit().putString(PASS_KEY, pass).apply();
    }

    public void putAuto(boolean auto) {
        sharedPreferences.edit().putString(AUTO_KEY, auto ? "1" : "0").apply();
    }

    public boolean isAuto() {
        return this.sharedPreferences.getString(AUTO_KEY, "").equals("1");
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    public void putUsername(String user) {
        sharedPreferences.edit().putString(USER_KEY, user).apply();
    }

    public String getUsername() {
        return this.sharedPreferences.getString(USER_KEY, "");
    }

    public String getEmail() {
        return this.sharedPreferences.getString(MAIL_KEY, "");
    }

    public String getPass() {
        return this.sharedPreferences.getString(PASS_KEY, "");
    }

    public Long getLastTime() {
        return this.sharedPreferences.getLong(LAST_TIME_KEY, 0);
    }

    public static SharedPrefs getInstance() {
        return instance;
    }
}
