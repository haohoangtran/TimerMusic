package io.github.haohoangtran.kimyennew;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@SuppressLint("Registered")
public class MusicService extends Service {
    private static final String SUBJECT = "schedule";
    private static String TAG = MusicService.class.toString();
    public static MediaPlayer mPlayer;
    private static ScheduledExecutorService scheduleTaskExecutor;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
        mPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        initSchedule(getApplicationContext());
        return START_STICKY;
    }

    public static void playingFile(File file) {
        Log.e(TAG, "playingFile: " + file.getName());
        audioPlayer(file);
    }

    public static void stopPlayingFile() {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        mPlayer.stop();
        mPlayer.reset();
    }

    private static void audioPlayer(File file) {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        try {
            mPlayer.reset();
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setLooping(true);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer.start();
                }
            });
            mPlayer.prepareAsync();
            mPlayer.start();

        } catch (Exception e) {
            Log.e(TAG, "audioPlayer: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void initSchedule(Context context) {
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

        if (SharePref.getInstance().isOn()) {
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int currentMinuteOfDay = ((hour * 60) + minute);
            int positionSchedule = currentMinuteOfDay / DbContext.TIME_INTERVAL;
            int timetoNextStep = (positionSchedule + 1) * DbContext.TIME_INTERVAL - currentMinuteOfDay;
            //vào sẽ tính thời gian lần kế tiếp chạy, và set lần này
            setSchedule(positionSchedule, timetoNextStep, context);
        } else {
            Toast.makeText(context, "Khong on switch", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setSchedule(int position, int timeNextStep, Context context) {
        Schedule schedule = DbContext.getInstance().getSchedules().get(position);
        Music music = DbContext.getInstance().getCurrentMusic();
        if (schedule.isSelect()) {
            if (music != null) {
                playingFile(music.getFile());
            } else {
                Toast.makeText(context, "Vui long chon file de chay", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (music != null) {
                stopPlayingFile();
            }
        }
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                int size = DbContext.getInstance().getSchedules().size();
                if (position + 1 >= size) {
                    setSchedule(0, DbContext.TIME_INTERVAL, context);
                } else {
                    setSchedule(position + 1, DbContext.TIME_INTERVAL, context);
                }
            }
        }, timeNextStep != DbContext.TIME_INTERVAL ? timeNextStep : 0, timeNextStep, TimeUnit.MINUTES);
    }

    public static void resume() {
        if (mPlayer != null && !mPlayer.isPlaying() && mPlayer.getCurrentPosition() > 1) {
            mPlayer.seekTo(mPlayer.getCurrentPosition());
            mPlayer.start();
        }
    }

    public static void pause() {
        if (mPlayer != null && mPlayer.isPlaying())
            mPlayer.pause();
    }

}
