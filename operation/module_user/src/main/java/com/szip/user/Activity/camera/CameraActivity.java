package com.szip.user.Activity.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.user.R;

import java.io.IOException;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_USER_CAMERA;


@Route(path = PATH_ACTIVITY_USER_CAMERA)
public class CameraActivity extends BaseActivity {

    private FrameLayout preview;

    private ImageView switchIv;

    private boolean cameraAble = true;

    private ICameraPresenter cameraPresenter;

    private ToActivityBroadcast toActivityBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.user_activity_camera);
        toActivityBroadcast = new ToActivityBroadcast();
        initView();
        cameraPresenter = new CameraPresenterImp(getApplicationContext());
        cameraPresenter.initCamera(preview);


        switchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    changeCamera();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPresenter.registerSensor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_UI_VIEW);
        toActivityBroadcast.registerReceive(handle,this,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPresenter.unRegisterSensor();
        toActivityBroadcast.unregister(this);
    }

    @Override
    protected void onDestroy() {
        // 回收Camera资源，必须的
        cameraPresenter.removeCamera();
        super.onDestroy();
    }

    private void initView() {
        preview = findViewById(R.id.camera_preview);
        switchIv = findViewById(R.id.switchIv);
    }


    private void changeCamera() throws IOException{
        cameraPresenter.changeCamera();
    }

    private MyHandle handle = new MyHandle() {
        @Override
        public void onReceive(Intent intent) {
            if (intent.getBooleanExtra("state",false)){
                if (cameraAble){
                    cameraAble = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cameraAble = true;
                        }
                    },1000);
                    cameraPresenter.takePicture();
                }
            } else {
                finish();
            }
        }
    };
}