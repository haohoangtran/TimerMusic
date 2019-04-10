package io.github.haohoangtran.kimyennew;

import java.io.File;

public class Music {
    public File file;
    public String name;
    public String path;
    public boolean isPlaying;
    public int id;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public Music(String path) {
        this.path = path;
        this.file = new File(path);
        this.name = file.getName();
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "Music{" +
                "name='" + name + '\'' +
                ", isPlaying=" + isPlaying +
                ", id=" + id +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Music(File file) {
        this.file = file;
        this.path = this.file.getAbsolutePath();
        this.name = file.getName();
        this.isPlaying = file.getAbsolutePath().equals(SharePref.getInstance().getPathMusic());
        if (this.isPlaying) {
            DbContext.getInstance().setCurrentMusic(this);
        }
    }

    public Music(File file, boolean isPlaying, int id) {
        this.id = id;
        this.file = file;
        this.path = this.file.getAbsolutePath();
        this.name = file.getName();
        this.isPlaying = isPlaying;
        if (this.isPlaying) {
            DbContext.getInstance().setCurrentMusic(this);
        }
    }

    public int getId() {
        return id;
    }
}
