package io.github.haohoangtran.music.eventbus;

public class ScheduleTimeChange {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ScheduleTimeChange(String content) {

        this.content = content;
    }
}
