package com.szip.user.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

public class UnitSelectActivity extends BaseActivity implements View.OnClickListener {

    private ImageView cIv,fIv,metricIv,britishIv;

    private int unit;//单位制式
    private int temp;

    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_unit_select);
        setAndroidNativeLightStatusBar(this,true);
        userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(this));
        initView();
        initEvent();
    }

    private void initView() {
        setTitle(getString(R.string.user_unit));
        cIv = findViewById(R.id.cIv);
        fIv = findViewById(R.id.fIv);
        metricIv = findViewById(R.id.metricIv);
        britishIv = findViewById(R.id.britishIv);
        if (userModel!=null){
            unit = userModel.unit;
            if (unit==0){
                metricIv.setVisibility(View.VISIBLE);
                britishIv.setVisibility(View.GONE);
            }else {
                metricIv.setVisibility(View.GONE);
                britishIv.setVisibility(View.VISIBLE);
            }

            temp = userModel.tempUnit;
            if (temp==0){
                cIv.setVisibility(View.VISIBLE);
                fIv.setVisibility(View.GONE);
            }else {
                cIv.setVisibility(View.GONE);
                fIv.setVisibility(View.VISIBLE);
            }
        }

    }

    private void initEvent() {
        findViewById(R.id.britishRl).setOnClickListener(this);
        findViewById(R.id.metricRl).setOnClickListener(this);
        findViewById(R.id.fRl).setOnClickListener(this);
        findViewById(R.id.cRl).setOnClickListener(this);
        findViewById(R.id.saveTv).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backIv) {
            finish();
        } else if (id == R.id.metricRl) {
            metricIv.setVisibility(View.VISIBLE);
            britishIv.setVisibility(View.GONE);
            unit = 0;
        } else if (id == R.id.britishRl) {
            metricIv.setVisibility(View.GONE);
            britishIv.setVisibility(View.VISIBLE);
            unit = 1;
        } else if (id == R.id.cRl) {
            cIv.setVisibility(View.VISIBLE);
            fIv.setVisibility(View.GONE);
            temp = 0;
        } else if (id == R.id.fRl) {
            cIv.setVisibility(View.GONE);
            fIv.setVisibility(View.VISIBLE);
            temp = 1;
        } else if (id == R.id.saveTv) {
            if (unit == userModel.unit && temp == userModel.tempUnit) {
                showToast(getString(R.string.user_saved));
                finish();
            } else {
                HttpMessageUtil.newInstance().postForSetUnit(unit + "", temp + "", callback);
            }
        }
    }


    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {
            if (response.getCode()==200){
                showToast(getString(R.string.user_saved));
                userModel.unit = unit;
                userModel.tempUnit = temp;
                SaveDataUtil.newInstance().saveUserInfo(userModel);
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command","setUnit");
                sendBroadcast(intent);
                finish();
            }else {
                showToast(response.getMessage());
            }

        }
    };
}