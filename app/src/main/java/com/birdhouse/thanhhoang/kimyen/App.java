package com.birdhouse.thanhhoang.kimyen;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Environment;

import java.io.File;

import com.birdhouse.thanhhoang.kimyen.SharedPrefs;

@SuppressLint("Registered")
public class App extends Application {
    public static String APP_DIR;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefs.init(this);
        APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "YenSaoKim";
        File f = new File(APP_DIR);
        if (!f.exists()) {
            f.mkdir();
        }
    }
}

