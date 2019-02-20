package com.birdhouse.thanhhoang.kimyen;

public class Schedule {
    private String start;
    private String end;
    private boolean onPlay;

    public Schedule(String start, String end) {
        this.start = start;
        this.end = end;
        this.onPlay = false;
    }

    public Schedule(String start, String end, boolean onPlay) {
        this.start = start;
        this.end = end;
        this.onPlay = onPlay;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStartTime() {
        return this.start;
    }

    public String getEndTime() {
        return this.end;
    }

    public String toString() {
        return this.start + " - " + this.end;
    }

    public boolean onPlay() {
        return onPlay;
    }
}
