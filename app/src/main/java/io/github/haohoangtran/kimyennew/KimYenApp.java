package io.github.haohoangtran.kimyennew;

import android.app.Application;

import io.realm.Realm;

public class KimYenApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DbContext.init(getApplicationContext());
        SharePref.init(getApplicationContext());
    }
}
