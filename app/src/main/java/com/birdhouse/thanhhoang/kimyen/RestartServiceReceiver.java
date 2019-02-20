package com.birdhouse.thanhhoang.kimyen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive tắt rồi bật lại");
        context.startService(new Intent(context.getApplicationContext(), MusicService.class));
    }
}
