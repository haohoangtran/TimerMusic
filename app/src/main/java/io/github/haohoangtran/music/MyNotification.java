package io.github.haohoangtran.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MyNotification extends Notification {

    private Context ctx;
    private NotificationManager mNotificationManager;

    @SuppressLint("NewApi")
    public MyNotification(Context ctx) {
        super();
        this.ctx = ctx;
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) ctx.getSystemService(ns);
        CharSequence tickerText = "Shortcuts";
        long when = System.currentTimeMillis();
        Notification.Builder builder = new Notification.Builder(ctx);
        @SuppressWarnings("deprecation")
        Notification notification = builder.getNotification();
        notification.when = when;
        notification.tickerText = tickerText;
        notification.icon = R.drawable.ic_play_arrow_black_24dp;
        RemoteViews contentView = new RemoteViews(ctx.getPackageName(), R.layout.messageview);
        //set the button listeners
        setListeners(contentView);
        notification.contentView = contentView;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        CharSequence contentTitle = "From Shortcuts";
        mNotificationManager.notify(548853, notification);
    }

    public void setListeners(RemoteViews view) {
        //radio listener
        Intent radio=new Intent(ctx,HelperActivity.class);
        radio.putExtra("DO", "radio");
        PendingIntent pRadio = PendingIntent.getActivity(ctx, 0, radio, 0);
        view.setOnClickPendingIntent(R.id.radio, pRadio);

        //pre listener
        Intent pre=new Intent(ctx, HelperActivity.class);
        pre.putExtra("DO", "pre");
        PendingIntent pback = PendingIntent.getActivity(ctx, 1, pre, 0);
        view.setOnClickPendingIntent(R.id.ibBack, pback);
        //play listener
        Intent play=new Intent(ctx, HelperActivity.class);
        play.putExtra("DO", "play");
        PendingIntent pPause = PendingIntent.getActivity(ctx, 5, play, 0);
        view.setOnClickPendingIntent(R.id.ibPause, pPause);
        //pause listener
        Intent pause=new Intent(ctx, HelperActivity.class);
        pause.putExtra("DO", "pause");
        PendingIntent pPlay = PendingIntent.getActivity(ctx, 5, pause, 0);
        view.setOnClickPendingIntent(R.id.ibPlay, pPlay);
        //resume listener
        Intent resume=new Intent(ctx, HelperActivity.class);
        resume.putExtra("DO", "resume");
        PendingIntent pReboot = PendingIntent.getActivity(ctx, 5, resume, 0);
        view.setOnClickPendingIntent(R.id.ibPlay, pReboot);
    }

    public class MediaButtonNoti extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "test " + intent.getAction());
        }

    }
}
