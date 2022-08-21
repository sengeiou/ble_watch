package com.szip.blewatch.Activity.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.szip.blewatch.Activity.main.MainActivity;
import com.szip.blewatch.R;
import com.szip.blewatch.base.View.BaseActivity;

public class WelcomeActivity extends BaseActivity implements IWelcomeView{

    private boolean isConfig = false;
    private boolean isInitInfo = false;
    private boolean isInitBle = false;
    private IWelcomePresenter welcomePresenter;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        mContext = this;
        welcomePresenter = new WelcomePresenterImpl(this);
        welcomePresenter.checkPrivacy(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        welcomePresenter.setViewDestory();
    }

    @Override
    public void checkPrivacyResult(boolean comfirm) {
        if (comfirm){//隐私协议通过
            welcomePresenter.initBle(getApplicationContext());
            welcomePresenter.initDeviceConfig();
            welcomePresenter.initUserInfo(getApplicationContext());
        }else {
            finish();
        }
    }


    @Override
    public void initDeviceConfigFinish() {
        isConfig = true;
        if (isInitInfo&&isInitBle){
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        }
    }

    @Override
    public void initBleFinish() {
        isInitBle = true;
        if (isInitInfo&&isConfig){
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        }
    }

    @Override
    public void initUserinfoFinish(boolean isNeedLogin) {
        isInitInfo = true;
        if (isInitBle&&isConfig){
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        }
    }
}