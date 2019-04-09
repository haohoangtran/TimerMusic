package io.github.haohoangtran.kimyennew;

import java.io.File;

public class Music {
    private File file;
    private String name;
    private String path;
    private boolean isPlaying;

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
                ", path='" + path + '\'' +
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

    public Music(File file, boolean isPlaying) {
        this.file = file;
        this.path = this.file.getAbsolutePath();
        this.name = file.getName();
        this.isPlaying = isPlaying;
    }
}
