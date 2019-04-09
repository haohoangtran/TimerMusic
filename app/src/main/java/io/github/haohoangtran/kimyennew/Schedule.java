package io.github.haohoangtran.kimyennew;

import android.annotation.SuppressLint;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Schedule extends RealmObject {

    private String time;
    @PrimaryKey
    private int minute;

    public Schedule() {
    }


    public void setSelect(boolean select) {
        DbContext.getInstance().getRealm().beginTransaction();
        this.isSelect = select;
        DbContext.getInstance().getRealm().commitTransaction();
    }

    private boolean isSelect;

    @Override
    public String toString() {
        return "Schedule{" +
                "time='" + time + '\'' +
                ", minute=" + minute +
                ", isSelect=" + isSelect +
                '}';
    }

    public String getTime() {
        return time;
    }


    public boolean isSelect() {
        return isSelect;
    }

    public Schedule(int minute) {
        this.minute = minute;
        time = miliToTime(minute);
    }

    public int getMinute() {
        return minute;
    }

    private String miliToTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
