package io.github.haohoangtran.music.sharepref;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    private SharedPreferences sharedPreferences;
    private static final String SHARE_PREFS_NAME = "SP";
    private static final String USER_KEY = "username";
    private static final String PASS_KEY = "password";
    private static SharedPrefs instance;

    public SharedPrefs(Context context) {
        this.sharedPreferences = context.getSharedPreferences(
                SHARE_PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public static void init(Context context) {
        SharedPrefs.instance = new SharedPrefs(context);
    }

    public void putEmail(String username) {
        sharedPreferences.edit().putString(USER_KEY, username).apply();
    }
    public void putPassword(String pass) {
        sharedPreferences.edit().putString(PASS_KEY, pass).apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
    public String getEmail(){
        return this.sharedPreferences.getString(USER_KEY, "");
    }
    public String getPass(){
        return this.sharedPreferences.getString(PASS_KEY, "");
    }
    public static SharedPrefs getInstance() {
        return instance;
    }
}
