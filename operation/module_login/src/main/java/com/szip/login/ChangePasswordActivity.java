package com.szip.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.login.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_CHANGE_PASSWORD;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_LOGIN;


@Route(path = PATH_ACTIVITY_CHANGE_PASSWORD)
public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {

    private EditText oldEt,passwordEt,confirmPasswordEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.login_activity_change_password);
        setAndroidNativeLightStatusBar(this,true);
        initView();
        initEvent();
    }

    private void initView() {
        ((TextView)findViewById(R.id.titleBigTv)).setText(R.string.login_change_password);
        passwordEt = findViewById(R.id.pswEt);
        confirmPasswordEt = findViewById(R.id.confirmPswEt);
        oldEt = findViewById(R.id.oldPswEt);
    }

    private void initEvent() {

        findViewById(R.id.finishTv).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);

        ((CheckBox)findViewById(R.id.oldLawsCb)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psd = oldEt.getText().toString();
                if (isChecked){
                    oldEt.setInputType(0x90);
                }else {
                    oldEt.setInputType(0x81);
                }
                oldEt.setSelection(psd.length());
            }
        });

        ((CheckBox)findViewById(R.id.lawsCb)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psd = passwordEt.getText().toString();
                if (isChecked){
                    passwordEt.setInputType(0x90);
                }else {
                    passwordEt.setInputType(0x81);
                }
                passwordEt.setSelection(psd.length());
            }
        });
        ((CheckBox)findViewById(R.id.lawsCb1)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psd = confirmPasswordEt.getText().toString();
                if (isChecked){
                    confirmPasswordEt.setInputType(0x90);
                }else {
                    confirmPasswordEt.setInputType(0x81);
                }
                confirmPasswordEt.setSelection(psd.length());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.finishTv){
            if (passwordEt.getText().toString().equals("")||confirmPasswordEt.getText().toString().equals("")||oldEt.getText().toString().equals("")){
                showToast(getString(R.string.login_input_psw));
            } else if (!passwordEt.getText().toString().equals(confirmPasswordEt.getText().toString())){
                showToast(getString(R.string.login_inconsistent));
            }else{
                ProgressHudModel.newInstance().show(this,getString(R.string.waiting));
                HttpMessageUtil.newInstance().postChangePassword(oldEt.getText().toString().trim(),passwordEt.getText().toString().trim()
                        ,callback);
            }
        }else if (id == R.id.backIv){
            finish();
        }
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {
            ProgressHudModel.newInstance().diss();
        }

        @Override
        public void onResponse(BaseApi response, int id) {
            ProgressHudModel.newInstance().diss();
            if (response.getCode() == 200){
                showToast(getString(R.string.login_change_success));
                finish();
            }else {
                showToast(response.getMessage());
            }
        }
    };
}