package com.szip.user.Activity.help;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.Locale;

import okhttp3.Call;

public class HelpActivity extends BaseActivity {

    private EditText feedbackEt;
    private TextView lengthTv;
    private RelativeLayout feedbackRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_help);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();
    }

    private void initView() {
        setTitle(getString(R.string.user_help));
        feedbackEt = findViewById(R.id.feedbackEt);
        lengthTv = findViewById(R.id.wordLenghtTv);
        feedbackRl = findViewById(R.id.feedbackRl);
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.helpRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, FaqActivity.class));
            }
        });
        findViewById(R.id.sendTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedbackEt.getText().toString().length()>300)
                    showToast(getString(R.string.user_feedback_long));
                else if (feedbackEt.getText().toString().equals(""))
                    showToast(getString(R.string.user_feedback_empty));
                else{
                    ProgressHudModel.newInstance().show(HelpActivity.this,getString(R.string.waiting),
                            false);
                    HttpMessageUtil.newInstance().postSendFeedback(feedbackEt.getText().toString(),callback);
                }
            }
        });

        feedbackEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                lengthTv.setText(String.format(Locale.ENGLISH,"%d/300",len));
                if (len>300)
                    lengthTv.setTextColor(Color.RED);
                else
                    lengthTv.setTextColor(Color.BLACK);
            }
        });


        feedbackRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedbackEt.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(feedbackEt, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {
            ProgressHudModel.newInstance().diss();
            showToast(e.getMessage());
        }

        @Override
        public void onResponse(BaseApi response, int id) {
            ProgressHudModel.newInstance().diss();
            if (response.getCode()==200){
                showToast(getString(R.string.user_feedback_success));
                finish();
            }
        }
    };
}