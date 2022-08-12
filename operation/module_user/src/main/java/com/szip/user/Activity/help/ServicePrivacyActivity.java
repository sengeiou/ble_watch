package com.szip.user.Activity.help;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;


import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressWebView;
import com.szip.user.R;

public class ServicePrivacyActivity extends BaseActivity {

    private ProgressWebView contentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_service_privacy);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();
    }
    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    private void initView() {
        findViewById(R.id.rightIv).setVisibility(View.GONE);
        setTitle(getString(R.string.user_service));
        contentWeb = findViewById(R.id.contentWeb);
        if(getResources().getConfiguration().locale.getLanguage().equals("zh"))
            contentWeb.loadUrl("https://cloud.znsdkj.com/guideline/#/home");
        else
            contentWeb.loadUrl("https://cloud.znsdkj.com/guideline/#/home?&lang=en");

        contentWeb.getSettings().setJavaScriptEnabled(true);
    }
}