package com.szip.blewatch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.blewatch.base.BaseApplication;
import com.szip.blewatch.base.Broadcast.UtilBroadcat;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Constant;
import com.szip.blewatch.base.Notification.MyNotificationReceiver;
import com.szip.blewatch.base.Service.AlarmService;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.HealthyCardData;
import com.szip.blewatch.base.db.dbModel.HealthyCardData_Table;
import com.szip.blewatch.base.db.dbModel.NotificationData;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.SportData_Table;
import com.szip.blewatch.base.db.dbModel.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * @author ddnosh
 * @website http://blog.csdn.net/ddnosh
 */
public class MyApplication extends BaseApplication {
    private int mFinalCount;

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 注册广播
         * */
        UtilBroadcat broadcat = new UtilBroadcat(getApplicationContext());
        broadcat.onRegister();

        initNotifyList();

//        initAlarm();
        String packageName = getPackageName();
        String strListener = Settings.Secure.getString(this.getContentResolver(),
                "enabled_notification_listeners");
        if (strListener != null
                && strListener
                .contains(packageName)) {
            ComponentName localComponentName = new ComponentName(this, MyNotificationReceiver.class);
            PackageManager localPackageManager = this.getPackageManager();
            localPackageManager.setComponentEnabledSetting(localComponentName, 2, 1);
            localPackageManager.setComponentEnabledSetting(localComponentName, 1, 1);
        }
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mFinalCount++;
                //如果mFinalCount ==1，说明是从后台到前台
                Log.e("onActivityStarted", mFinalCount + "");
                if (mFinalCount == 1) {
                    //说明从后台回到了前台
                    Log.i("DATA******", " 返回到了 前台");
                    sendBroadcast(new Intent(BroadcastConst.START_CONNECT_DEVICE));
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mFinalCount--;
                //如果mFinalCount ==0，说明是前台到后台

                Log.i("onActivityStopped", mFinalCount + "");
                if (mFinalCount == 0) {
                    //说明从前台回到了后台
                    Log.i("DATA******", " 切换到了 后台");

                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

//    private void initAlarm() {
//        Intent intent = new Intent(this,AlarmService.class);
//        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,0);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmManager.cancel(pendingIntent);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    SystemClock.elapsedRealtime()+60*1000, 60*1000, pendingIntent);
//        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() +60*1000,pendingIntent);
//        }else {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+60*1000,pendingIntent);
//        }
//
//    }

    private void initNotifyList() {
        List<NotificationData> list = new ArrayList<>();
        list.add(new NotificationData("message", R.mipmap.cp_icon_empty, getString(R.string.message), true));
        list.add(new NotificationData("com.tencent.mm", R.mipmap.cp_icon_empty, getString(R.string.wechat), true));
        list.add(new NotificationData("com.tencent.mobileqq", R.mipmap.cp_icon_empty, "QQ", true));
        list.add(new NotificationData("com.facebook.katana", R.mipmap.cp_icon_empty, "facebook", true));
        list.add(new NotificationData("com.facebook.orca", R.mipmap.cp_icon_empty, "facebook message", true));
        list.add(new NotificationData("com.twitter.android", R.mipmap.cp_icon_empty, "twitter", true));
        list.add(new NotificationData("com.whatsapp", R.mipmap.cp_icon_empty, "WhatApps", true));
        list.add(new NotificationData("com.instagram.android", R.mipmap.cp_icon_empty, "Instagram", true));
//        list.add(new NotificationData("com.skype.rover", R.mipmap.cp_icon_empty, "Skype", true));
//        list.add(new NotificationData("com.linkedin.android", R.mipmap.cp_icon_empty, "Linkedin", true));
        list.add(new NotificationData("jp.naver.line.android", R.mipmap.cp_icon_empty, "Line", true));
        list.add(new NotificationData("com.snapchat.android", R.mipmap.cp_icon_empty, "Snapchat", true));
//        list.add(new NotificationData("com.pinterest", R.mipmap.cp_icon_empty, "Pinterest", true));
//        list.add(new NotificationData("com.google.android.apps.plus", R.mipmap.cp_icon_empty, "Google+", true));
        list.add(new NotificationData("com.tumblr", R.mipmap.cp_icon_empty, "Tumblr", true));
//        list.add(new NotificationData("com.viber.voip", R.mipmap.cp_icon_empty, "Viber", true));
//        list.add(new NotificationData("com.vkontakte.android", R.mipmap.cp_icon_empty, "Vkontakte", true));
//        list.add(new NotificationData("org.telegram.messenger", R.mipmap.cp_icon_empty, "Telegram", true));
//        list.add(new NotificationData("com.zhiliaoapp.musically", R.mipmap.cp_icon_empty, "Tiktok", true));
        SaveDataUtil.newInstance().saveNotificationList(list);
    }
}
