package io.github.haohoangtran.music;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.opencsv.CSVReader;
import com.sun.mail.imap.IMAPFolder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import io.github.haohoangtran.music.databases.Database;
import io.github.haohoangtran.music.eventbus.PlayFileEvent;
import io.github.haohoangtran.music.eventbus.ReloadData;
import io.github.haohoangtran.music.mail.MailPending;
import io.github.haohoangtran.music.model.Schedule;
import io.github.haohoangtran.music.sharepref.SharedPrefs;
import io.github.haohoangtran.music.utils.Compressor;

@SuppressLint("Registered")
public class MusicService extends Service {
    private static final String SUBJECT = "fwdandpop";
    private static String TAG = MusicService.class.toString();
    private ArrayList<Schedule> schedules;
    private ArrayList<CountDownTimer> countDownTimers;
    public static MediaPlayer mPlayer;
    private Intent intent;
    private String messageLog = "";
    private ArrayList<MailPending> mailPendings;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mailPendings = new ArrayList<>();
    }

    public void onMessageEvent(PlayFileEvent event) {
        Log.e(TAG, "onMessageEvent: " + event.getFile().getName());
        playingFile(event.getFile());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            this.intent = intent;
            schedules = new ArrayList<>();
            File csv = getScheduleCSV(new File(App.APP_DIR));
            if (csv != null) {
                CSVReader reader = new CSVReader(new FileReader(csv));
                List<String[]> myEntries = reader.readAll();

                if (myEntries.size() > 0) {
                    myEntries.remove(0);
                    for (String[] line : myEntries) {
                        String[] arr = line[0].split("\t");
                        if (arr.length == 3) {
                            schedules.add(new Schedule(arr[0], arr[1], arr[2]));
                        }
                    }
                }
                setMusicPlay(null);
            }
            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
// This schedule a runnable task every 2 minutes
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    getMail();
                }
            }, 0, 2, TimeUnit.MINUTES);
        } catch (IOException e) {
            Log.e(TAG, "onStartCommand: " + e.toString());
        }
        return START_STICKY;
    }


    public static void resume() {
        if (mPlayer != null && !mPlayer.isPlaying() && mPlayer.getCurrentPosition() > 1) {
            mPlayer.seekTo(mPlayer.getCurrentPosition());
            mPlayer.start();
        }
    }

    public static void pause() {
        if (mPlayer != null && mPlayer.isPlaying())
            mPlayer.pause();
    }

    public File findFileByName(ArrayList<File> files, String name) {
        for (File file : files) {
            String filename = file.getName();
            if (filename.equalsIgnoreCase(name)) {
                return file;
            }
        }
        return null;
    }

    public static void playingFile(File file) {
        audioPlayer(file);
        Database.getInstance().setPlaying(file);
        EventBus.getDefault().post(new ReloadData());
    }

    private void stopPlayingFile(File file) {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        mPlayer.stop();
        mPlayer.reset();
        Database.getInstance().setPlaying(null);
    }

    //tra ve so giay dang chay
    public static int getSecond() {
        if (mPlayer.getCurrentPosition() > 0) {
            return mPlayer.getCurrentPosition();
        }
        return -1;
    }

    private static void audioPlayer(File file) {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        try {
            if (mPlayer.isPlaying()) {
                mPlayer.reset();
            }
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setLooping(true);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer.start();
                }
            });
            mPlayer.prepareAsync();
            mPlayer.start();

        } catch (Exception e) {
            Log.e(TAG, "audioPlayer: " + e.toString());
            e.printStackTrace();
        }
    }

    private void setMusicPlay(Message message) {
        Log.e(TAG, "setMusicPlay: " + (new Date()).toString());
        messageLog += " bắt đầu set schedule " + (new Date()).toString() + '\n';
        if (countDownTimers == null) {
            countDownTimers = new ArrayList<>();
        }
        for (CountDownTimer countDownTimer : countDownTimers) {
            countDownTimer.cancel();
        }
        ArrayList<File> songs = getScheduMusic(new File(App.APP_DIR));
        for (Schedule schedule : schedules) {
            File file = findFileByName(songs, schedule.getAudio());
            if (file == null) {
                continue;
            }
            schedule.setPath(file.getAbsolutePath());
            schedule.setFile(file);
            long futurestart = schedule.getNextTimeStart();
            long futurestop = schedule.getNextTimeStart();
            if (futurestart < -1 && futurestop < -1) {
                //boqua
            } else if (futurestart < -1 && futurestop > -1) {
                //bat dau
                playingFile(file);
                countDownTimers.add(new CountDownTimer(futurestop, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        stopPlayingFile(file);
                    }

                }.start());
            } else {
                countDownTimers.add(new CountDownTimer(futurestart, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        playingFile(file);

                    }

                }.start());
                countDownTimers.add(new CountDownTimer(futurestop, 1000) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        stopPlayingFile(file);
                    }

                }.start());
            }
        }
        try {
            if (message != null) {
                StringBuilder body = new StringBuilder();
                for (Schedule schedule : schedules) {
                    body.append(schedule.toString());
                }
                String from = message.getFrom()[0].toString();
                from = from.substring(from.indexOf('<') + 1, from.indexOf('>'));
                sendMail(from, messageLog + "\n" + body.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "setMusicPlay: " + e);
        }

    }

    private ArrayList<File> getScheduMusic(File folder) {
        ArrayList<File> musics = new ArrayList<>();
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
        return musics;
    }

    private File getScheduleCSV(File folder) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                return getScheduleCSV(fileEntry);
            } else {
                String type = getMimeType(fileEntry.getAbsolutePath());
                if (type != null && type.contains("text")) {
                    return fileEntry;
                }
            }
        }
        return null;
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void sendMail(String to, String body) {
        try {
            final String username = SharedPrefs.getInstance().getEmail();
            final String password = SharedPrefs.getInstance().getPass();

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });


            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject("Setup xong " + (new Date()).toString());
            message.setText(body);

            Transport.send(message);

            Log.e(TAG, "sendMailReport: Xong " + to);
        } catch (Exception e) {
            Log.e(TAG, "sendMailReport: Lỗi " + to + e.toString());
        }

    }


    private void getMail() {
        long lastTime = 0;
        IMAPFolder folder = null;
        Store store = null;
        Message message=null;
        String mail = SharedPrefs.getInstance().getEmail();
        String pass = SharedPrefs.getInstance().getPass();
        Flags.Flag flag = null;
        messageLog = "";
        Log.e(TAG, "getMail: Bắt đầu " + mail + pass + " " + (new Date()).toString());
        messageLog += "getMail: Bắt đầu " + mail + pass + " " + (new Date()).toString() + '\n';
        String pathLastTime = App.APP_DIR + "/last.txt";
        try {
            File old = new File(pathLastTime);
            if (old.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(old));
                lastTime = Long.parseLong(reader.readLine());
            }
            Log.e(TAG, "getMail: thời gian cũ  " + (new Date(lastTime)).toString());

            messageLog += "getMail: thời gian cũ  " + (new Date(lastTime)).toString() + '\n';
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");

            Session session = Session.getDefaultInstance(props, null);

            store = session.getStore("imaps");
            store.connect("imap.googlemail.com", mail, pass);
            SearchTerm search = new SearchTerm() {

                @Override
                public boolean match(javax.mail.Message message) {
                    try {
                        if (message.getSubject() != null && message.getSubject().contains(SUBJECT)) {
                            return true;
                        }
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    return false;
                }

            };
            folder = (IMAPFolder) (store.getFolder("Inbox"));
            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            javax.mail.Message[] messages = folder.search(search);
            Log.e(TAG, "getMail: Search xong " + (new Date()).toString());
            messageLog += "getMail: Search xong " + (new Date()).toString() + '\n';
            if (messages.length > 0) {
                message = messages[messages.length - 1];
                Date sent = message.getSentDate();
                if (sent.getTime() > lastTime) {
                    Multipart multipart = (Multipart) message.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                                (bodyPart.getFileName() == null || bodyPart.getFileName().isEmpty()
                                        || !bodyPart.getFileName().contains(".zip"))) {
                            continue; // dealing with attachments only
                        }
                        String path = App.APP_DIR;
                        FileUtils.deleteDirectory(new File(path));
                        Log.e(TAG, "getMail: Lấy xong đang lưu zip" + (new Date()).toString());
                        messageLog += "getMail: Lấy xong đang lưu zip" + (new Date()).toString() + '\n';
                        InputStream is = bodyPart.getInputStream();
                        Log.e(TAG, "getMail: " + path);
                        File dir = new File(path);
                        if (!dir.exists())
                            dir.mkdirs();
                        else {
                            FileUtils.deleteDirectory(dir);
                        }
                        File f = new File(path, bodyPart.getFileName());
                        FileOutputStream fos = new FileOutputStream(f);
                        byte[] buf = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buf)) != -1) {
                            fos.write(buf, 0, bytesRead);
                        }
                        Log.e(TAG, "getMail:  lưu xong file bắt đầu giải nén" + (new Date()).toString());
                        messageLog += "getMail:  lưu xong file bắt đầu giải nén" + (new Date()).toString() + '\n';
                        File dirContent = new File(path, "content");
                        if (dirContent.exists()) {
                            FileUtils.deleteDirectory(dirContent);
                        } else {
                            dirContent.mkdir();
                        }
                        fos.close();
                        if (!unpackZip(f, dirContent.getAbsolutePath())) {
                            Log.e(TAG, "getMail:  giải nén lỗi" + (new Date()).toString());
                        } else {
                            Log.e(TAG, "getMail:  giải nén xong" + (new Date()).toString());
                            messageLog += "getMail:  giải nén xong" + (new Date()).toString() + '\n';
                            FileOutputStream stream = new FileOutputStream(new File(pathLastTime));
                            stream.write((message.getSentDate().getTime() + "").getBytes());
                            stream.close();
                            setMusicPlay(message);
                            Database.getInstance().setAudio();
                            EventBus.getDefault().post(new ReloadData());
                        }
                    }
                }
            }
            Log.e(TAG, "getMail:  Xong toàn bộ" + (new Date()).toString());
        } catch (Exception e) {
            Log.e(TAG, "getMail: " + e.toString());
            messageLog+=e.toString();
            if(message!=null){
                try{
                    String from = message.getFrom()[0].toString();
                    from = from.substring(from.indexOf('<') + 1, from.indexOf('>'));
                    sendMail(from, messageLog);
                }catch (Exception ex){
                    Log.e(TAG, "getMail: "+e.toString());
                }
            }
        } finally {
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "getMail: " + e.toString() + (new Date()).toString());
            }

        }
    }

    private boolean unpackZip(File targetFile, String unzipFolder) {
        try {
            String password = "";
            Compressor.unzip(targetFile.getAbsolutePath(), unzipFolder, password);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "unpackZip: " + e.toString() + (new Date()).toString());
            return false;
        }
    }
}