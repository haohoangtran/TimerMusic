package com.birdhouse.thanhhoang.kimyen;

import java.io.File;
import java.util.ArrayList;

public class Database {
    private static Database instance;
    private File audio;
    private ArrayList<Schedule> schedules;

    public File getAutio() {
        return audio;
    }

    public void setAudio(File audio) {
        this.audio = audio;
    }

    private Database() {
        schedules = new ArrayList<Schedule>();
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
    }

    public void clearSchedules() {
        schedules.clear();
    }
}
