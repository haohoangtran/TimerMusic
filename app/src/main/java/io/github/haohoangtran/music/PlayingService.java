package io.github.haohoangtran.music;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

@SuppressLint("Registered")
public class PlayingService extends Service implements MediaPlayer.OnPreparedListener {
    public MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public void audioPlayer(File file) {
        //set up MediaPlayer
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        try {
            if (mPlayer.isPlaying()) {
                mPlayer.reset();
            }
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setLooping(true);
            mPlayer.setOnPreparedListener(this);
            mPlayer.prepareAsync();
            mPlayer.start();
        } catch (Exception e) {
            Log.e("cc", "audioPlayer: " + e.toString());
            e.printStackTrace();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String name;
        if (intent != null) {
            name = intent.getStringExtra("uri");
            Log.e("qq", "onStartCommand: " + name);
            audioPlayer(new File(name));
        } else {
            Log.e("qq", "onStartCommand: trống ");
        }
        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }
}
