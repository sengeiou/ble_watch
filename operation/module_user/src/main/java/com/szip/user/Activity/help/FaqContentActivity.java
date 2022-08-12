package com.szip.user.Activity.help;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressWebView;
import com.szip.user.HttpModel.FaqBean;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import org.apache.commons.lang3.StringUtils;

import okhttp3.Call;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_USER_FAQ;

@Route(path = PATH_ACTIVITY_USER_FAQ)
public class FaqContentActivity extends BaseActivity {

    private String id;
    private ProgressWebView contentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_faq_content);
        setAndroidNativeLightStatusBar(this,true);
        id = getIntent().getStringExtra("id");
        if (id == null)
            return;
        initView();
        initData();
    }

    private void initView() {
        contentWeb = findViewById(R.id.contentWeb);
        findViewById(R.id.backIv).setOnClickListener(v -> finish());
    }

    private void initData() {
        HttpMessageUtil.newInstance().getFaq(id,StringUtils.isNumeric(id), callback);
    }

    private GenericsCallback<FaqBean> callback = new GenericsCallback<FaqBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(FaqBean response, int id) {
            if (response.getCode() == 200&&response.getData()!=null){
                setTitle(response.getData().getTitle());
                contentWeb.loadData(response.getData().getContent(),"text/html","UTF-8");
            }
        }
    };
}