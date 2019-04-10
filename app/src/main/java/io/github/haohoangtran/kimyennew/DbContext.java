package io.github.haohoangtran.kimyennew;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.realm.Realm;

public class DbContext {
    private String TAG = this.getClass().getSimpleName();
    private Realm realm;
    private static DbContext instance;
    private List<Music> musics;
    private List<Schedule> schedules;
    private Music currentMusic;
    public static final int TIME_INTERVAL = 15;

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public DbContext(Context context) {
        Realm.init(context);
        this.realm = Realm.getDefaultInstance();
        musics = new ArrayList<>();
        schedules = new ArrayList<>();
        setSchedules();
    }

    public Schedule getScheduleInPosition(int pos) {
        //neu qua range tro ve 0;
        if (pos < schedules.size()) {
            return schedules.get(pos);
        }
        return schedules.get(0);
    }

    public void insertOrUpdateSchedule(Schedule schedule) {
        realm.beginTransaction();
        realm.insertOrUpdate(schedule);
        realm.commitTransaction();
        Log.e(TAG, "insertOrUpdateSchedule: " + schedule);
    }

    public static void init(Context context) {
        instance = new DbContext(context);
    }

    public static DbContext getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setCurrentMusic(Music currentMusic) {
        this.currentMusic = currentMusic;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void readMusicFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        getListFiles(dir);
        EventBus.getDefault().post(new DataChangeEvent());
    }

    public void changePlayFile(Music music) {
        for (Music music1 : musics) {
            music1.setPlaying(false);
        }
        music.setPlaying(true);
        this.currentMusic = music;
        Log.e(TAG, "changePlayFile: " + currentMusic);
        SharePref.getInstance().savePathRunning(music.getPath());
    }

    private void getListFiles(File parentDir) {
        String pathCurrentSelect = SharePref.getInstance().getPathMusic();
        musics.clear();
        Queue<File> files = new LinkedList<>();
        files.addAll(Arrays.asList(parentDir.listFiles()));
        while (!files.isEmpty()) {
            File file = files.remove();
            if (file.isDirectory()) {
                files.addAll(Arrays.asList(file.listFiles()));
            } else if (isMusicFile(file.getName())) {
                boolean isPlaying = file.getAbsolutePath().equals(pathCurrentSelect);
                Music music = new Music(file, isPlaying, musics.size());
                musics.add(music);
                if (isPlaying) {
                    currentMusic = music;
                }
            }
        }
    }

    public void handleAllScheduleState(boolean check) {
        for (Schedule schedule : this.schedules) {
            schedule.setSelect(check);
            insertOrUpdateSchedule(schedule);
        }
        EventBus.getDefault().post(new DataChangeEvent());
    }


    public boolean isCheckAllSchedule() {
        for (Schedule schedule : schedules) {
            if (!schedule.isSelect()) {
                return false;
            }
        }
        return true;
    }

    private void setSchedules() {
        List<Schedule> schedulesOld = realm.where(Schedule.class).findAll();
        Log.e(TAG, "setSchedules: " + schedulesOld.size());
        if (schedulesOld.size() == 0) {
            setNewSchedules();
        } else {
            this.schedules = schedulesOld;
        }
    }

    private void setNewSchedules() {
        int dayMinutes = 24 * 60;
        int time = 0;
        while (time < dayMinutes) {
            Schedule schedule = new Schedule(time);
            schedules.add(schedule);
            insertOrUpdateSchedule(schedule);
            time += TIME_INTERVAL;
        }
    }

    private boolean isMusicFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }
}
