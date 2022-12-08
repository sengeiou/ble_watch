package com.szip.login.Forget;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.login.HttpModel.ImageVerificationBean;
import com.szip.login.R;
import com.szip.login.HttpModel.CheckVerificationBean;
import com.szip.login.Register.SetPasswordFragment;
import com.szip.login.Utils.HttpMessageUtil;
import com.zaaach.citypicker.Picker;
import com.zaaach.citypicker.adapter.OnPickListener;
import com.zaaach.citypicker.model.City;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText userEt,verifyCodeEt;
    private TextView sendTv,nextTv,countryTv;
    private Timer timer;
    private int time;

    private String countryStr,codeStr;
    private SharedPreferences sharedPreferences;
    private boolean isPhone = false;

    private ImageView imageIv;
    private String imageId;
    private EditText imageEt;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    String getCodeAgain = getString(R.string.login_send);
                    time--;
                    if (time <= 0){
                        timer.cancel();
                        sendTv.setEnabled(true);
                        sendTv.setText(getCodeAgain);
                    }else {
                        sendTv.setText(time+"s");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login_activity_forget_password);
        setAndroidNativeLightStatusBar(this,true);
        sharedPreferences = getSharedPreferences(FILE,MODE_PRIVATE);
        countryStr = sharedPreferences.getString("countryName","");
        codeStr = sharedPreferences.getString("countryCode","");
        initView();
        initEvent();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initEvent() {
        sendTv.setOnClickListener(this);
        nextTv.setOnClickListener(this);
        findViewById(R.id.countryRl).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.imageIv).setOnClickListener(this);
    }

    private void initView() {
        setTitle(getString(R.string.login_forget_psw));
        userEt = findViewById(R.id.userEt);
        countryTv = findViewById(R.id.countryTv);
        imageIv = findViewById(R.id.imageIv);
        imageEt = findViewById(R.id.imageEt);
        if (!countryStr.equals("")){
            countryTv.setText(countryStr);
            countryTv.setTextColor(Color.BLACK);
        }

        verifyCodeEt = findViewById(R.id.verifyCodeEt);
        sendTv = findViewById(R.id.sendTv);
        nextTv = findViewById(R.id.nextTv);
        updateImageVerification();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sendTv){
            if (userEt.getText().toString().equals("")){
                showToast(getString(R.string.login_input_account));
            }else if (countryStr.equals("")){
                showToast(getString(R.string.login_chose_location));
            }else {
                if (!MathUtil.newInstance().isNumeric(userEt.getText().toString())){
                    if (!MathUtil.newInstance().isEmail(userEt.getText().toString()))
                        showToast(getString(R.string.login_right_email));
                    else
                        startTimer();
                } else
                    startTimer();
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
                                countryTv.setTextColor(getResources().getColor(R.color.rayblue));
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
        }else if (id == R.id.nextTv){
            if (userEt.getText().toString().equals("")){
                showToast(getString(R.string.login_input_account));
            }else if (countryStr.equals("")){
                showToast(getString(R.string.login_chose_location));
            }else if (verifyCodeEt.getText().toString().equals("")){
                showToast(getString(R.string.login_input_verification));
            }else if (MathUtil.newInstance().isNumeric(userEt.getText().toString())){
                HttpMessageUtil.newInstance().postCheckVerifyCode("1",codeStr,
                        userEt.getText().toString(),"", verifyCodeEt.getText().toString(),verificationBeanGenericsCallback);
                isPhone = true;
            }else {
                HttpMessageUtil.newInstance().postCheckVerifyCode("2","","",userEt.getText().toString(),
                        verifyCodeEt.getText().toString(),verificationBeanGenericsCallback);
                isPhone = false;
            }

        }else if (id == R.id.backIv){
            finish();
        }else if (id == R.id.imageIv){
            updateImageVerification();
        }
    }

    /**
     * 开始倒计时
     * */
    private void startTimer(){

        if (!MathUtil.newInstance().isNumeric(userEt.getText().toString()))
            HttpMessageUtil.newInstance().postCheckVerifyCode_v2(2,"","",
                    userEt.getText().toString().trim(),imageId,1,imageEt.getText().toString().trim(),callback);
        else
            HttpMessageUtil.newInstance().postCheckVerifyCode_v2(1,codeStr,
                    userEt.getText().toString(),"",imageId,1,imageEt.getText().toString().trim(),callback);
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {

            if (response.getCode() != 200) {
                updateImageVerification();
                showToast(response.getMessage());
            }else {
                sendTv.setEnabled(false);
                time = 60;
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(100);
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask,1000,1000);
            }
        }
    };

    private GenericsCallback<CheckVerificationBean> verificationBeanGenericsCallback = new GenericsCallback<CheckVerificationBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(CheckVerificationBean response, int id) {
            if (response.getCode() == 200){
                if (response.getData().isValid()){
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    final Fragment prev = fragmentManager.findFragmentByTag("forget");
                    if (prev != null){
                        ft.remove(prev).commit();
                        ft = fragmentManager.beginTransaction();
                    }
                    ft.addToBackStack(null);
                    SetPasswordFragment setPasswordFragment = new SetPasswordFragment(isPhone,codeStr,userEt.getText().toString(),
                            verifyCodeEt.getText().toString());
                    setPasswordFragment.show(ft, "forget");
                }
            }else {
                MathUtil.newInstance().showToast(getApplicationContext(),response.getMessage());
            }
        }
    };

    private void updateImageVerification(){
        HttpMessageUtil.newInstance().getImageVerifyCode(new GenericsCallback<ImageVerificationBean>(new JsonGenericsSerializator()) {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(ImageVerificationBean response, int id) {
                imageId = response.getData().getId();
                Glide.with(ForgetPasswordActivity.this)
                        .load(response.getData().getInputImage())
                        .into(imageIv);
            }
        });
    }

}