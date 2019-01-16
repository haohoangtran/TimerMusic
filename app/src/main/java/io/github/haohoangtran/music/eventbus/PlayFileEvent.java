package io.github.haohoangtran.music.eventbus;

import java.io.File;

public class PlayFileEvent {
    public PlayFileEvent(File file) {
        this.file = file;
    }

    public File getFile() {

        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private File file;
}
