package com.szip.user.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.R;

public class AboutActivity extends BaseActivity {


    private ImageView deviceIv;
    private CircularImageView dialIv;
    private TextView nameTv,versionTv,macTv;

    private SportWatchAppFunctionConfigDTO data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_about);
        setAndroidNativeLightStatusBar(this,true);
        data = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (data == null)
            return;
        initView();

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
                Intent intent = new Intent(BroadcastConst.START_JL_OTA);
                sendBroadcast(intent);
            }
        });
    }
}