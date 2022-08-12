package com.szip.blewatch.base.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.LogUtil;

import java.util.Calendar;

public class AlarmService extends Service {
    public AlarmService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.getInstance().logd("data******","alarm");
        sendBroadcast(new Intent(BroadcastConst.START_CONNECT_DEVICE));
        initAlarm();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initAlarm() {
        Intent intent = new Intent(this,AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){

        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() +60*1000,pendingIntent);
        }else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+60*1000,pendingIntent);
        }

    }
}