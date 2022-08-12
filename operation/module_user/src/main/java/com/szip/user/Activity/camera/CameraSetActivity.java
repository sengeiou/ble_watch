package com.szip.user.Activity.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.R;

public class CameraSetActivity extends BaseActivity {

    private Switch cameraSw;

    private SportWatchAppFunctionConfigDTO data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_camera_set);
        setAndroidNativeLightStatusBar(this,true);
        data = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (data==null)
            return;
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.user_ble_camera));
        cameraSw = findViewById(R.id.cameraSw);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED&&
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    cameraSw.setChecked(data.cameraSwitch);
                }
            }else {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    cameraSw.setChecked(data.cameraSwitch);
                }
            }
        }else {
            cameraSw.setChecked(data.cameraSwitch);
        }

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cameraSw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED||
                                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                            MyAlerDialog.getSingle().showAlerDialog(getString(R.string.permission_tip), getString(R.string.user_camera_permission_tip),
                                    getString(R.string.agree), getString(R.string.disagree), false, new MyAlerDialog.AlerDialogOnclickListener() {
                                        @Override
                                        public void onDialogTouch(boolean flag) {
                                            if (flag){
                                                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                }, 103);
                                            }else {
                                                showToast(getString(R.string.user_camera_permission_error));
                                                cameraSw.setChecked(false);
                                                data.cameraSwitch = false;
                                                data.update();
                                            }
                                        }
                                    },CameraSetActivity.this);
                        }else {
                            data.cameraSwitch = cameraSw.isChecked();
                            data.update();
                        }
                    }else {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                            MyAlerDialog.getSingle().showAlerDialog(getString(R.string.permission_tip), getString(R.string.user_camera_permission_tip),
                                    getString(R.string.agree), getString(R.string.disagree), false, new MyAlerDialog.AlerDialogOnclickListener() {
                                        @Override
                                        public void onDialogTouch(boolean flag) {
                                            if (flag){
                                                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                                }, 102);
                                            }else {
                                                showToast(getString(R.string.user_camera_permission_error));
                                                cameraSw.setChecked(false);
                                                data.cameraSwitch = false;
                                                data.update();
                                            }
                                        }
                                    },CameraSetActivity.this);


                        }else {
                            data.cameraSwitch = cameraSw.isChecked();
                            data.update();
                        }
                    }
                }else {
                    data.cameraSwitch = cameraSw.isChecked();
                    data.update();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102){
            int code = grantResults[0];
            if (!(code == PackageManager.PERMISSION_GRANTED)){
                showToast(getString(R.string.user_camera_permission_error));
                cameraSw.setChecked(false);
                data.cameraSwitch = false;
            }else {
                data.cameraSwitch = true;
            }
            data.update();
        }else if (requestCode == 103){
            int code = grantResults[0];
            int code1 = grantResults[1];
            if (!(code == PackageManager.PERMISSION_GRANTED&&code1 == PackageManager.PERMISSION_GRANTED)){
                showToast(getString(R.string.user_camera_permission_error));
                cameraSw.setChecked(false);
                data.cameraSwitch = false;
            }else {
                data.cameraSwitch = true;
            }
            data.update();
        }
    }
}