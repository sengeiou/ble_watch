package com.szip.blewatch.base.Notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Administrator on 2019/12/27.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyNotificationReceiver extends NotificationListenerService{
    private ArrayList<String> musicList = new ArrayList<>(Arrays.asList("com.kugou.android","com.sds.android.ttpod","cn.kuwo.player",
            "com.tencent.qqmusic","com.netease.cloudmusic","com.duomi.android","com.foobar2000.foobar2000","cmccwm.mobilemusic",
            "com.miui.player","com.samsung.android.app.music.chn","com.htc.music","com.amazon.mp3",
            "com.oppo.music","com.sonyericsson.music","com.lge.music","com.android.music"));

    private NotificationDataManager notificationDataManager = null;

    private Looper mServiceLooper;
    public MyNotificationReceiver() {
        notificationDataManager = new NotificationDataManager(this);
    }

    @Override
    public void onCreate() {
        mServiceLooper = Looper.getMainLooper();
        super.onCreate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("notify******","Notification Posted, " + "ID: " + sbn.getId() + ", Package: "
                + sbn.getPackageName());
        configureMusicControl(sbn);
        if(Build.VERSION.SDK_INT  < 18){
            Log.i("notify******","Android platform version is lower than 18.");
            return;
        }
        Notification notification = (Notification) sbn.getNotification();

        if (notification == null) {
            Log.e("notify******","Notification is null, return");
            return;
        }
        RemoteViews contentView = notification.contentView;
        PendingIntent intent = notification.contentIntent;
        if (intent==null);
        if (contentView == null);
        Log.i("notify******","packagename = " + sbn.getPackageName() + "tag = " +sbn.getTag()+"Id = " + sbn.getId());
        NotificationData notificationData = notificationDataManager.getNotificationData(
                notification, sbn.getPackageName(),sbn.getTag(),sbn.getId());

        notificationDataManager.sendNotificationData(notificationData);
    }

    /**
     * 判断手机有几个播放器
     * */
    private void configureMusicControl(StatusBarNotification snb) {
        if(musicList.contains(snb.getPackageName()))
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("notify_posted"));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("notify******", "Notification Removed, " + "ID: " + sbn.getId() + ", Package: "
                + sbn.getPackageName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("notify******", "onUnbind()");
        return false;
    }
}
