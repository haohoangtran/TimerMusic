package io.github.haohoangtran.kimyennew;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MusicServiceV2 extends Service {
    private static final String SUBJECT = "schedule";
    private static String TAG = MusicServiceV2.class.toString();
    public static MediaPlayer mPlayer;
    private static ScheduledExecutorService scheduleTaskExecutor;
    private static ScheduledFuture<?> scheduledFuture;
    private static Music musicPlaying;
    private static final Handler handler = new Handler();
    private static Context context;

    public MusicServiceV2() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_kimyen)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
        mPlayer = new MediaPlayer();
        mPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initSchedule(getApplicationContext(), false);
        Log.e(TAG, "onStartCommand: chay service");
        return START_STICKY;
    }

    private static long xulySchedule(Context context) {
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int seconds = rightNow.get(Calendar.SECOND);
        int minuteInday = hour * 60 + minute;
        int secondInDay = minuteInday * 60 + seconds;
        int posSchedule = minuteInday / DbContext.TIME_INTERVAL;// vi tri schedule set = phan nguyen cua so phut chia  schedule time
        final Schedule schedule = DbContext.getInstance().getSchedules().get(posSchedule);
        long milisecondNextTime = (DbContext.TIME_INTERVAL * 60 - secondInDay % (DbContext.TIME_INTERVAL * 60)) * 1000;

        boolean isOn = SharePref.getInstance().isOn();
        Music music = DbContext.getInstance().getCurrentMusic();
        Log.e(TAG, "run: " + isOn + (rightNow.getTime()) + " " + schedule + music);
        if (isOn) {
            if (schedule.isSelect()) {
                if (music != null && musicPlaying != null && music.getId() != musicPlaying.getId()) {
                    //chay file khac
                    stopPlayingFile();
                    musicPlaying = music;
                    playingFile(musicPlaying.getFile());
                } else if (music != null && musicPlaying == null) {
                    //chay lan dau
                    stopPlayingFile();
                    musicPlaying = music;
                    playingFile(musicPlaying.getFile());
                } else if (music != null && musicPlaying != null && music.getId() == musicPlaying.getId()) {
                    //chay lai file dang chay
                    resume();
                } else {
                    Log.e(TAG, "handleSchedule: k ro " + music + musicPlaying);
                    Toast.makeText(context, "Vui lòng chọn file", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (music != null) {
                    pause();
                }
            }
        } else {
            Toast.makeText(context, "Thiết bị đang tắt", Toast.LENGTH_SHORT).show();
        }
        return milisecondNextTime;// chay schedule va tra ve time lan tiep theo
    }

    private static void handleSchedule(Context context) {
        long milis = xulySchedule(context);
        Log.e(TAG, "handleSchedule: lan chay tiep theo " + milis / 1000);
        if (interval != null)
            handler.removeCallbacks(interval);
        interval = new Runnable() {
            @Override
            public void run() {
                handleSchedule(context);
            }
        };
        handler.postDelayed(interval, milis);
    }

    public static void initSchedule(Context context, boolean reset_audio) {
        MusicServiceV2.context = context;
        Log.e(TAG, "initSchedule: Khoi dong lai");
        /*
         * Lay so phut hien tai % cho so phut schedule (15) se ra so phut timeout tiep theo*/
        handleSchedule(context);

    }

    private static Runnable interval;

    public static void resume() {
        if (mPlayer != null && !mPlayer.isPlaying() && mPlayer.getCurrentPosition() > 1) {
            mPlayer.seekTo(mPlayer.getCurrentPosition());
            mPlayer.start();
        }
    }

    public static void playingFile(File file) {
        Log.e(TAG, "playingFile: " + file.getName());
        audioPlayer(file);
    }

    public static void stopPlayingFile() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setLooping(true);
        }
        mPlayer.stop();
        mPlayer.reset();
    }

    private static void audioPlayer(File file) {
        if (mPlayer == null) {

            mPlayer = new MediaPlayer();
            mPlayer.setLooping(true);
        }
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

    public static void pause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }
}
