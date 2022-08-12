package com.szip.blewatch.Activity;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.szip.blewatch.R;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressWebView;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_PRIVACY;

@Route(path = PATH_ACTIVITY_PRIVACY)
public class PrivacyActivity extends BaseActivity {
    private ProgressWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(com.szip.user.R.layout.user_activity_privacy);
        setAndroidNativeLightStatusBar(this,true);
        initView();
    }

    private void initView() {
        setTitle(getString(com.szip.user.R.string.user_privacy));
        findViewById(com.szip.user.R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        webView = findViewById(R.id.webview);
        if(getResources().getConfiguration().locale.getLanguage().equals("zh"))
            webView.loadUrl("https://cloud.znsdkj.com:8443/file/contract/FitRing/statement-zh.html");
        else
            webView.loadUrl("https://cloud.znsdkj.com:8443/file/contract/FitRing/statement-en.html");
        webView.getSettings().setJavaScriptEnabled(true);
    }
}