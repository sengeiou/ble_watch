package com.szip.user.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Interfere.OnSmsStateListener;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.NotificationData;
import com.szip.user.Adapter.NotificationAdapter;
import com.szip.user.R;

import java.util.ArrayList;
import java.util.List;

import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class NotificationActivity extends BaseActivity {

    private Switch allSw;
    private ListView switchList;
    private NotificationAdapter notificationAdapter;

    private boolean notificationState = false;
    private SharedPreferences sharedPreferences;

    private boolean openService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_notification);
        setAndroidNativeLightStatusBar(this,true);
        sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        notificationState = sharedPreferences.getBoolean("notificationState",false);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (openService){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isNotificationListenerActived()){
                        notificationState = true;
                        initData();
                    }else {
                        allSw.setChecked(false);
                    }
                    openService = false;
                }
            },500);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putBoolean("notificationState",notificationState).commit();
    }

    private void initData() {
        if (allSw.isChecked()){
            List<NotificationData> list = LoadDataUtil.newInstance().getNotificationList();
            notificationAdapter.setNotificationDatas(list);
        }
    }

    private void initView() {
        setTitle(getString(R.string.user_notification));
        allSw = findViewById(R.id.allSw);
        switchList = findViewById(R.id.switchList);
        notificationAdapter = new NotificationAdapter(getApplicationContext(),onSmsStateListener);
        switchList.setAdapter(notificationAdapter);

        allSw.setChecked(notificationState);
        allSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (isNotificationListenerActived()){
                        notificationState = true;
                        initData();
                    }else {
                        showNotifiListnerPrompt();
                    }
                }else {
                    notificationState = false;
                    notificationAdapter.setNotificationDatas(null);
                }
            }
        });

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private boolean isNotificationListenerActived() {
        String packageName = getPackageName();
        String strListener = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return strListener != null
                && strListener
                .contains(packageName);
    }

    private void showNotifiListnerPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.user_notify_title);
        builder.setMessage(R.string.user_notify_msg);

        builder.setNegativeButton(R.string.user_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                allSw.setChecked(false);
                dialog.dismiss();
            }
        });
        // Go to notification listener settings
        builder.setPositiveButton(R.string.user_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openService = true;
                dialog.dismiss();
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });
        builder.create().show();
    }

    private OnSmsStateListener onSmsStateListener = new OnSmsStateListener() {
        @Override
        public void onSmsStateChange(boolean check) {
            if (check) {
                LogUtil.getInstance().logd("DATA******","进入回调");
                checkPermission();
            } else {
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command","stopSms");
                sendBroadcast(intent);
            }
        }
    };

    private void checkPermission() {
        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED){
                LogUtil.getInstance().logd("DATA******","申请权限");
                requestPermissions(new String[]{Manifest.permission.READ_SMS},
                        100);
            }else {
                LogUtil.getInstance().logd("DATA******","申请已经打开");
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command","startSms");
                sendBroadcast(intent);
            }
        }else {
            Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
            intent.putExtra("command","startSms");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command","startSms");
                sendBroadcast(intent);
            }else {
                showToast(getString(R.string.user_permission_error_sms));
                notificationAdapter.setSmsError();
            }
        }
    }
}