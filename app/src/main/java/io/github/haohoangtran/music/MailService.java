package io.github.haohoangtran.music;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MailService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
