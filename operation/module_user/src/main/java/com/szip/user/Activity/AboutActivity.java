package com.szip.user.Activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.SendFileConst;
import com.szip.blewatch.base.Model.FirmwareModel;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.HttpModel.FirmwareBean;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_ABOUT;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_USER_CAMERA;

@Route(path = PATH_ACTIVITY_ABOUT)
public class AboutActivity extends BaseActivity implements MyHandle {


    private ImageView deviceIv;
    private CircularImageView dialIv;
    private TextView nameTv,versionTv,macTv;

    private SportWatchAppFunctionConfigDTO data;
    private ToActivityBroadcast toActivityBroadcast;
    private FirmwareModel firmwareModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.user_activity_about);
        setAndroidNativeLightStatusBar(this,true);
//        Bundle bundle = getIntent().getBundleExtra("bundle");
//        firmwareModel = (FirmwareModel) bundle.getSerializable("firmware");
        ARouter.getInstance().inject(this);

        toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_OTA_STATE);
        intentFilter.addAction(BroadcastConst.UPDATE_RESOURCE_STATE);
        toActivityBroadcast.registerReceive(this,this,intentFilter);

        data = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (data == null)
            return;
        initView();

        HttpMessageUtil.newInstance().getFirmware(LoadDataUtil.newInstance().getConfigId(MathUtil.newInstance().getUserId(getApplicationContext())),
                new GenericsCallback<FirmwareBean>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(FirmwareBean response, int id) {
                        if(response.getCode()==200&&response.getData()!=null){
                            firmwareModel = response.getData();
                            MyAlerDialog.getSingle().showAlerDialog("新的固件版本", "检测到新的固件版本，是否现在进行升级？",
                                    "是", "否", false, new MyAlerDialog.AlerDialogOnclickListener() {
                                        @Override
                                        public void onDialogTouch(boolean flag) {
                                            if (flag){
                                                ProgressHudModel.newInstance().show(AboutActivity.this,getString(R.string.loading));
                                                Intent intent = new Intent(BroadcastConst.START_JL_OTA);
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("firmware",firmwareModel);
                                                intent.putExtra("bundle",bundle);
                                                sendBroadcast(intent);
//                                                Bundle bundle = new Bundle();
//                                                bundle.putSerializable("firmware",response.getData());
//                                                ARouter.getInstance().build(PATH_ACTIVITY_ABOUT)
//                                                        .withBundle("bundle",bundle)
//                                                        .navigation();
                                            }
                                        }
                                    },AboutActivity.this);
                        }

                    }
                });

    }

    private void initView() {
        setTitle(getString(R.string.user_about));
        deviceIv = findViewById(R.id.deviceIv);
        nameTv = findViewById(R.id.nameTv);
        versionTv = findViewById(R.id.versionTv);
        macTv = findViewById(R.id.macTv);

        if (data.screenType==0){
            deviceIv.setImageResource(R.mipmap.my_aboutdevice_circle);
            dialIv = findViewById(R.id.circleIv);
        }else {
            deviceIv.setImageResource(R.mipmap.my_aboutdevice_square);
            dialIv = findViewById(R.id.squareIv);
        }
        nameTv.setText(data.appName);
        macTv.setText(data.mac);
        Glide.with(this).load(data.dialImg).into(dialIv);

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.updateTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressHudModel.newInstance().show(AboutActivity.this,getString(R.string.loading));
                Intent intent = new Intent(BroadcastConst.START_JL_OTA);
                Bundle bundle = new Bundle();
                bundle.putSerializable("firmware",firmwareModel);
                intent.putExtra("bundle",bundle);
                sendBroadcast(intent);
            }
        });
    }

    @Override
    public void onReceive(Intent intent) {
        if (intent.getAction().equalsIgnoreCase(BroadcastConst.UPDATE_RESOURCE_STATE)){
            int command = intent.getIntExtra("command",255);
            if (command == SendFileConst.PROGRESS){
                float progress = intent.getFloatExtra("progress",-1);
                ProgressHudModel.newInstance().setProgress((int)progress);
            }else if (command == SendFileConst.START_SEND){
                ProgressHudModel.newInstance().diss();
                ProgressHudModel.newInstance().showWithPie(AboutActivity.this,"更新资源中...",100);
            }else if (command == SendFileConst.ERROR){
                ProgressHudModel.newInstance().diss();
                showToast("升级失败");
            }else if (command == SendFileConst.FINISH){
                ProgressHudModel.newInstance().diss();
            }
        }else if(intent.getAction().equalsIgnoreCase(BroadcastConst.UPDATE_OTA_STATE)){
            int command = intent.getIntExtra("command",255);
            if (command == SendFileConst.PROGRESS){
                float progress = intent.getFloatExtra("progress",-1);
                ProgressHudModel.newInstance().setProgress((int)progress);
            }else if (command == SendFileConst.START_SEND){
                ProgressHudModel.newInstance().showWithPie(AboutActivity.this,"升级中...",100);
            }else if (command == SendFileConst.ERROR){
                ProgressHudModel.newInstance().diss();
                showToast("升级失败");
            }else if (command == SendFileConst.FINISH){
                ProgressHudModel.newInstance().diss();
                showToast("升级成功");

            }
        }
    }
}