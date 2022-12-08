package com.szip.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Model.HealthyConfig;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.login.Forget.ForgetPasswordActivity;
import com.szip.login.HttpModel.DeviceConfigBean;
import com.szip.login.HttpModel.LoginBean;
import com.szip.login.Register.RegisterActivity;
import com.szip.login.Utils.HttpMessageUtil;
import com.zaaach.citypicker.Picker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.Calendar;

import okhttp3.Call;

import static com.szip.blewatch.base.Util.MathUtil.FILE;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_LOGIN;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_PRIVACY;

/**
 * @author ddnosh
 * @website http://blog.csdn.net/ddnosh
 */

@Route(path = PATH_ACTIVITY_LOGIN)
public class LoginMainActivity extends BaseActivity implements View.OnClickListener {

    private EditText userEt,pswEt;
    private TextView countryTv;
    private CheckBox checkBox;

    private String countryStr,codeStr;

    private SharedPreferences sharedPreferences;

    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login_main);
        setAndroidNativeLightStatusBar(this,true);
        sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        countryStr = sharedPreferences.getString("countryName","");
        codeStr = sharedPreferences.getString("countryCode","");
        initView();
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.loginTv).setOnClickListener(this);
        findViewById(R.id.registerTv).setOnClickListener(this);
        findViewById(R.id.forgetTv).setOnClickListener(this);
        findViewById(R.id.countryRl).setOnClickListener(this);
        findViewById(R.id.privacyTv).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        ((CheckBox)findViewById(R.id.lawsCb)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            String psd = pswEt.getText().toString();
            if (isChecked){
                pswEt.setInputType(0x90);
            }else {
                pswEt.setInputType(0x81);
            }
            pswEt.setSelection(psd.length());
        });
    }

    private void initView() {
        setTitle(getString(R.string.login_login_account));
        userEt = findViewById(R.id.userEt);
        pswEt = findViewById(R.id.pswEt);
        countryTv = findViewById(R.id.countryTv);
        if (!countryStr.equals("")){
            countryTv.setText(countryStr);
            countryTv.setTextColor(Color.BLACK);
        }

        checkBox = findViewById(R.id.checkbox);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.loginTv){
            if (userEt.getText().toString().equals("")){
                showToast(getString(R.string.login_input_account));
            }else if (countryStr.equals("")){
                showToast(getString(R.string.login_chose_location));
            }else if (!checkBox.isChecked()){
                showToast(getString(R.string.login_checkPrivacy));
            }else if (pswEt.getText().toString().equals("")){
                showToast(getString(R.string.login_input_psw));
            }else {
                if (!MathUtil.newInstance().isNumeric(userEt.getText().toString())){//邮箱
                    if (MathUtil.newInstance().isEmail(userEt.getText().toString())){//如果是邮箱登录，判断邮箱格式是否正确
                        ProgressHudModel.newInstance().show(this,
                                getString(R.string.loading),false);
                        HttpMessageUtil.newInstance().postLogin("2","","",
                                userEt.getText().toString(),pswEt.getText().toString(),"","",callback);
                    }else {
                        showToast(getString(R.string.login_right_email));
                    }
                }else {//电话
                    ProgressHudModel.newInstance().show(this,
                            getString(R.string.loading),false);
                    HttpMessageUtil.newInstance().postLogin("1",codeStr,
                            userEt.getText().toString(), "",pswEt.getText().toString(),"","",callback);
                }
            }
        }else if (id == R.id.countryRl){
            Picker.getInstance()
                    .setFragmentManager(getSupportFragmentManager())
                    .enableAnimation(true)
                    .setAnimationStyle(R.style.CustomAnim)
                    .setOnPickListener(new OnPickListener() {
                        @Override
                        public void onPick(int position, City data) {
                            if (data!=null){
                                countryTv.setText(data == null ? "" :  data.getName());
                                countryTv.setTextColor(Color.BLACK);
                                if (sharedPreferences==null)
                                    sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                countryStr = data.getName();
                                codeStr = data.getCode();
                                editor.putString("countryName",countryStr);
                                editor.putString("countryCode",codeStr);
                                editor.commit();
                            }
                        }
                        @Override
                        public void onLocate() {

                        }
                    }).show();
        }else if (id == R.id.registerTv){
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }else if (id == R.id.privacyTv){
            ARouter.getInstance().build(PATH_ACTIVITY_PRIVACY).navigation();
        }else if (id == R.id.forgetTv){
            startActivity(new Intent(LoginMainActivity.this, ForgetPasswordActivity.class));
        }else if (id == R.id.backIv){
            finish();
        }
    }


    private void loadDeviceConfig(){
        if (userModel.deviceCode==null||userModel.deviceCode.equals("")){//如果账户没绑定蓝牙，直接登录成功回到主页
            finish();
        }else {
            HttpMessageUtil.newInstance().getDeviceConfig(loadConfigCallback);

        }
    }


    private GenericsCallback<LoginBean> callback = new GenericsCallback<LoginBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(LoginBean loginBean, int id) {
            ProgressHudModel.newInstance().diss();
            if (loginBean.getCode()==200){
                HttpClientUtils.newInstance().setToken(loginBean.getData().getToken());
                MathUtil.newInstance().saveStringData(getApplicationContext(),"token",loginBean.getData().getToken());
                MathUtil.newInstance().saveIntData(getApplicationContext(),"userId",loginBean.getData().getUserInfo().id);
                SaveDataUtil.newInstance().saveUserInfo(loginBean.getData().getUserInfo());
                userModel = loginBean.getData().getUserInfo();
                //登录成功之后请求一次天气信息
                Intent intent = new Intent(BroadcastConst.UPDATE_WEATHER);
                sendBroadcast(intent);
                loadDeviceConfig();
            }else {
                showToast(loginBean.getMessage());
            }
        }
    };

    private GenericsCallback loadConfigCallback = new GenericsCallback<DeviceConfigBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {
            ProgressHudModel.newInstance().diss();
            showToast(e.getMessage());
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onResponse(DeviceConfigBean response, int id) {
            ProgressHudModel.newInstance().diss();
            if(response.getCode()==200){
                for (SportWatchAppFunctionConfigDTO data:response.getData()){
                    if (data.appName.equals(userModel.product)){
                        data.mac = userModel.deviceCode;
                        HealthyConfig healthyConfig = data.getHealthMonitorConfig();
                        SaveDataUtil.newInstance().saveConfigData(data);
                        SaveDataUtil.newInstance().saveHealthyConfigData(healthyConfig);
                        HttpMessageUtil.newInstance().getForDownloadReportData(Calendar.getInstance().getTimeInMillis()/1000+"","30");
                        break;
                    }
                }
                finish();
            }
        }
    };
}
