package io.github.haohoangtran.music.model;

import java.io.File;
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
                Date date = new Date();
                date.setHours(Integer.parseInt(arr[0]));
                date.setMinutes(Integer.parseInt(arr[1]));
                date.setSeconds(Integer.parseInt(arr[2]));
                return ((date.getTime() - (new Date()).getTime())
                        / (1000 * 60 * 60 * 24));
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public long getNextTimeStop() {
        // time format HH:mm:ss
        try {
            String[] arr = this.start.split(":");
            if (arr.length == 3) {
                Date date = new Date();
                date.setHours(Integer.parseInt(arr[0]));
                date.setMinutes(Integer.parseInt(arr[1]));
                date.setSeconds(Integer.parseInt(arr[2]));
                return ((date.getTime() - (new Date()).getTime())
                        / (1000 * 60 * 60 * 24));
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }
}
