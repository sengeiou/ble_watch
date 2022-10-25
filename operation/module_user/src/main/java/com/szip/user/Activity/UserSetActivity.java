package com.szip.user.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.Activity.help.HelpActivity;
import com.szip.user.Activity.userInfo.UserInfoActivity;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_PRIVACY;
import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class UserSetActivity extends BaseActivity implements View.OnClickListener {

    private CircularImageView iconIv;
    private TextView versionTv,nameTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_user_set);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (userModel==null)
            return;
        Glide.with(this).load(userModel.avatar).error(R.mipmap.my_head_58).fallback(R.mipmap.my_head_58).into(iconIv);
        nameTv.setText(userModel.userName);
        String ver;
        try {
            ver = getPackageManager().getPackageInfo("com.szip.blewatch",
                    0).versionName;
            versionTv.setText("V" + ver);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        setTitle(getString(R.string.user_set));
        iconIv = findViewById(R.id.iconIv);
        versionTv = findViewById(R.id.versionTv);
        nameTv = findViewById(R.id.nameTv);


    }

    private void initEvent() {
        findViewById(R.id.userInfoRl).setOnClickListener(this);
        findViewById(R.id.securityRl).setOnClickListener(this);
        findViewById(R.id.helpRl).setOnClickListener(this);
        findViewById(R.id.privacyRl).setOnClickListener(this);
        findViewById(R.id.logoutTv).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.userInfoRl){
            startActivity(new Intent(UserSetActivity.this, UserInfoActivity.class));
        }else if (id == R.id.backIv){
            finish();
        }else if (id == R.id.securityRl){
            startActivity(new Intent(UserSetActivity.this, SecurityActivity.class));
        }else if (id == R.id.helpRl){
            startActivity(new Intent(UserSetActivity.this, HelpActivity.class));
        }else if (id == R.id.privacyRl){
            ARouter.getInstance().build(PATH_ACTIVITY_PRIVACY).navigation();
        }else if (id == R.id.logoutTv){
            MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip), getString(R.string.user_logout_tip), null, null,
                    false, new MyAlerDialog.AlerDialogOnclickListener() {
                        @Override
                        public void onDialogTouch(boolean flag) {
                            if (flag){
                                UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getApplicationContext()));
                                if (userModel == null)
                                    return;
                                if (userModel.deviceCode==null){
                                    HttpClientUtils.newInstance().setToken("");
                                    Intent intent = new Intent(BroadcastConst.UNBIND_SERVICE);
                                    sendBroadcast(intent);
                                    finish();
                                }else {
                                    ProgressHudModel.newInstance().show(UserSetActivity.this,getString(R.string.user_upload_data),false);
                                    String datas = MathUtil.newInstance().getStringWithJson(getSharedPreferences(FILE,MODE_PRIVATE));
                                    HttpMessageUtil.newInstance().postForUploadReportData(datas, new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                                        @Override
                                        public void onError(Call call, Exception e, int id) {
                                            ProgressHudModel.newInstance().diss();
                                            showToast(e.getMessage());
                                        }
                                        @Override
                                        public void onResponse(BaseApi response, int id) {
                                            if(response.getCode()==200){
                                                ProgressHudModel.newInstance().diss();
                                                MathUtil.newInstance().saveLastTime(getSharedPreferences(FILE,MODE_PRIVATE));
                                                HttpClientUtils.newInstance().setToken("");
                                                Intent intent = new Intent(BroadcastConst.UNBIND_SERVICE);
                                                sendBroadcast(intent);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    },UserSetActivity.this);
        }
    }
}