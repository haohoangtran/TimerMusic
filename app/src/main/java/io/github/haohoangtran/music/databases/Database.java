package io.github.haohoangtran.music.databases;

import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import io.github.haohoangtran.music.App;
import io.github.haohoangtran.music.model.Schedule;

public class Database {
    private static Database instance;
    private ArrayList<File> audio;
    private File playing;

    public ArrayList<File> getAudio() {
        return audio;
    }

    public void setAudio(ArrayList<File> audio) {
        this.audio = audio;
    }

    private Database() {
        audio = new ArrayList<>();
    }

    public File getPlaying() {
        return playing;
    }

    public void setPlaying(File playing) {
        this.playing = playing;
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void setAudio() {
        audio = getScheduMusic(new File(App.APP_DIR));
    }

    private ArrayList<File> getScheduMusic(File folder) {
        ArrayList<File> musics = new ArrayList<>();
        if (folder.exists())
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    musics.addAll(getScheduMusic(fileEntry));
                } else {
                    String type = getMimeType(fileEntry.getAbsolutePath());
                    if (type != null && type.contains("audio")) {
                        musics.add(fileEntry);
                    }
                }
            }
        else {
            folder.mkdir();
        }
        return musics;
    }


    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
