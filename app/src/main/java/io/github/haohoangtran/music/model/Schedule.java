package io.github.haohoangtran.music.model;

import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private String start;
    private String end;
    private String audio;
    private String path;

    @Override
    public String toString() {
        return "Schedule{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", audio='" + audio + '\'' +
                ", path='" + path + '\'' +
                ", file=" + file +
                '}';
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private File file;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Schedule(String start, String end, String audio) {
        this.start = start;
        this.end = end;
        this.audio = audio;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public long getNextTimeStart() {
        //tra ve mili s lan bat dau gan nhat
        // time format HH:mm:ss
        try {
            String[] arr = this.start.split(":");
            if (arr.length == 3) {
                Date dt = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(dt);
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr[0]));
                c.set(Calendar.MINUTE, Integer.parseInt(arr[1]));
                c.set(Calendar.SECOND, Integer.parseInt(arr[2]));
                dt = c.getTime();
                long diff=(dt.getTime() - (new Date()).getTime());
                if (diff < 0) {
                    c.add(Calendar.DATE, 1);
                    dt = c.getTime();
                    diff = dt.getTime() - (new Date()).getTime();
                }
                return diff;
            } else {
                return -1;
            }
        } catch (Exception e) {
            Log.e("loi", "getNextTimeStart: " + e.toString());
        }
        return -1;
    }

    public boolean isScheduleTime() {
        try {
            String[] arrStart = this.start.split(":");
            String[] arrStop = this.end.split(":");
            if (arrStart.length == 3 && arrStop.length == 3) {
                Date startTime = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(startTime);
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrStart[0]));
                c.set(Calendar.MINUTE, Integer.parseInt(arrStart[1]));
                c.set(Calendar.SECOND, Integer.parseInt(arrStart[2]));
                startTime = c.getTime();
                Date stopTime = new Date();
                c.setTime(stopTime);
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arrStop[0]));
                c.set(Calendar.MINUTE, Integer.parseInt(arrStop[1]));
                c.set(Calendar.SECOND, Integer.parseInt(arrStop[2]));
                stopTime = c.getTime();
                Date now = new Date();
                return (startTime.getTime() - now.getTime() < 0 && stopTime.getTime() - now.getTime() > 0);
            }
        } catch (Exception ex) {

        }
        return false;
    }

    public long getNextTimeStop() {
        //tra ve mili s lan bat dau gan nhat
        // time format HH:mm:ss
        try {
            String[] arr = this.end.split(":");
            if (arr.length == 3) {
                Date dt = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(dt);
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr[0]));
                c.set(Calendar.MINUTE, Integer.parseInt(arr[1]));
                c.set(Calendar.SECOND, Integer.parseInt(arr[2]));
                dt = c.getTime();
                long diff = dt.getTime() - (new Date()).getTime();
                if (diff < 0) {
                    c.add(Calendar.DATE, 1);
                    dt = c.getTime();
                    diff = dt.getTime() - (new Date()).getTime();
                }
                return diff;
            } else {
                return -1;
            }
        } catch (Exception e) {
            Log.e("loi", "getNextTimeStart: " + e.toString());
        }
        return -1;
    }
}
