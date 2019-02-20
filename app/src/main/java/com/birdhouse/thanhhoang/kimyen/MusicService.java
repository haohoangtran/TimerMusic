package com.birdhouse.thanhhoang.kimyen;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import com.sun.mail.imap.IMAPFolder;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

@SuppressLint("Registered")
public class MusicService extends Service {
    private static final String SUBJECT = "schedule";
    private static String TAG = MusicService.class.toString();
    public static MediaPlayer mPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        setMusicPlay(null);

        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        // This schedule a runnable task every 2 minutes
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.e(TAG, "getMail");
//                getMail();
//                readExcelFile("Schedule.xlsx");
            }
        }, 0, 2, TimeUnit.MINUTES);

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

    public static void readExcelFile(String filename) {
        //try load data from json file to save loading time

        //else load from excel
        String schedule_start = "", schedule_end = "";
        boolean add = true, write = false;
        Database.getInstance().clearSchedules();
        try {
            // Creating Input Stream
            XSSFRow myRow;
            Iterator cellIter;
            XSSFCell myCell;
            File file = new File(App.APP_DIR, filename);
            InputStream myInput = new FileInputStream(file);

            // Create a workbook using the File System
            XSSFWorkbook wb = new XSSFWorkbook(myInput);

            // Get the first sheet from workbook
            XSSFSheet mySheet = wb.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();
            rowIter.next(); // Skip the first row
            DateFormat formatter = new SimpleDateFormat("HH:mm");

            while (rowIter.hasNext()) {
                myRow = (XSSFRow) rowIter.next();
                cellIter = myRow.cellIterator();

                //start time
                myCell = (XSSFCell) cellIter.next();
                Date date = myCell.getDateCellValue();
                String start = formatter.format(date);

                //end time
                myCell = (XSSFCell) cellIter.next();
                date = myCell.getDateCellValue();
                String end = formatter.format(date);

                //On Off
                myCell = (XSSFCell) cellIter.next();
                boolean on = myCell.getStringCellValue().equals("On");

                //start, end, on ready to use
                Log.d("Excel", "Start: " + start + " End: " + end + " On: " + on);

                //if write enable
                //update stop time
                if (on) {
                    if (add) {
                        schedule_start = start;
                        add = false;
                    }
                    schedule_end = end;
                    write = true;
                } else {
                    if (write) {
//                        schedules.add(new Schedule(schedule_start, schedule_end));
                        Database.getInstance().addSchedule(new Schedule(schedule_start, schedule_end));
                        write = false;
                    }
                    add = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //save to json
//        return schedules;
    }

    private void getMail() {

        String _mail = SharedPrefs.getInstance().getEmail();
        String _pass = SharedPrefs.getInstance().getPass();
        if (_mail == null || _mail.isEmpty()) {
            return;
        }

        long lastTime = SharedPrefs.getInstance().getLastTime();
        if (lastTime <= 0) {
            return;
        }

        IMAPFolder folder = null;
        Store store = null;
        Message message = null;

        Flags.Flag flag = null;
//        messageLog = new StringBuilder();
        try {
            Log.e(TAG, "Request Email");
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect("imap.googlemail.com", _mail, _pass);
            SearchTerm search = new SearchTerm() {
                @Override
                public boolean match(javax.mail.Message message) {
                    try {
                        if (message.getSubject() != null &&
//                                message.getSentDate().getTime() > lastTime &&
                                message.getSubject().contains(SUBJECT)
                        )
                            return true;
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    return false;
                }
            };
            folder = (IMAPFolder) (store.getFolder("Inbox"));
            if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
            javax.mail.Message[] messages = folder.search(search);
            if (messages.length > 0) {
                message = messages[messages.length - 1]; // get the lastest email
                SharedPrefs.getInstance().putLastTime(message.getSentDate().getTime());
                Log.e(TAG, "Got New Email");
//                parseMail(message);
            } else {
                Log.e(TAG, "No New Email");
            }
        } catch (Exception e) {
            Log.e(TAG, "getMail: " + e.toString());
//            messageLog.append(e.toString());
        } finally {
            try {
                if (folder != null && folder.isOpen())
                    folder.close(true);
                if (store != null)
                    store.close();
            } catch (Exception e) {
                Log.e(TAG, "getMail: " + e.toString() + (new Date()).toString());
            }
        }
    }
}
