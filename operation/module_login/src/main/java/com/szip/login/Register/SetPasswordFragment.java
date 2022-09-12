package com.szip.login.Register;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.login.LoginMainActivity;
import com.szip.login.R;
import com.szip.login.HttpModel.LoginBean;
import com.szip.login.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

public class SetPasswordFragment extends DialogFragment implements View.OnClickListener {

    private View mRootView;

    private EditText passwordEt,confirmPasswordEt;

    private boolean isPhone;
    private String countryCode,user,verifyCode;


    public SetPasswordFragment(boolean isPhone, String countryCode, String user, String verifyCode) {
        this.isPhone = isPhone;
        this.countryCode = countryCode;
        this.user = user;
        this.verifyCode = verifyCode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView == null){
            mRootView = inflater.inflate(R.layout.login_fragment_set_password, container, false);
        }
        initView();
        initEvent();
        return mRootView;
    }



    private void initView() {
        ((TextView)mRootView.findViewById(R.id.titleBigTv)).setText(R.string.login_set_password);
        passwordEt = mRootView.findViewById(R.id.pswEt);
        confirmPasswordEt = mRootView.findViewById(R.id.confirmPswEt);
    }

    private void initEvent() {

        mRootView.findViewById(R.id.finishTv).setOnClickListener(this);
        mRootView.findViewById(R.id.backIv).setOnClickListener(this);

        ((CheckBox)mRootView.findViewById(R.id.lawsCb)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        ((CheckBox)mRootView.findViewById(R.id.lawsCb1)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if(window != null) {
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawableResource(R.color.white);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setWindowAnimations(R.style.CustomAnim);
        }
        return dialog;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.finishTv){
            if (passwordEt.getText().toString().equals("")){
                Toast.makeText(getActivity(),getString(R.string.login_input_psw),Toast.LENGTH_SHORT).show();
            } else if (!passwordEt.getText().toString().equals(confirmPasswordEt.getText().toString())){
                Toast.makeText(getActivity(),getString(R.string.login_inconsistent),Toast.LENGTH_SHORT).show();
            }else if (isPhone){
                if (getTag().equals("forget")){
                    HttpMessageUtil.newInstance().postForgotPassword("1",countryCode,
                            user,"", verifyCode,passwordEt.getText().toString(),callback);
                }else {
                    HttpMessageUtil.newInstance().postRegister("1",countryCode,user,"",
                            verifyCode, passwordEt.getText().toString(), MathUtil.newInstance().getDeviceId(getActivity()),"1",callback);
                }
            }else {
                if (getTag().equals("forget")){
                    HttpMessageUtil.newInstance().postForgotPassword("2","",""
                            ,user, verifyCode,passwordEt.getText().toString(),callback);
                }else {
                    HttpMessageUtil.newInstance().postRegister("2","","",user,
                            verifyCode, passwordEt.getText().toString(),MathUtil.newInstance().getDeviceId(getContext()),"1",callback);
                }

            }
        }else if (id == R.id.backIv){
            dismiss();
        }
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {

            if (response.getCode() == 200) {
                if (id == 100){//注册
                    if (isPhone){
                        HttpMessageUtil.newInstance().postLogin("1",countryCode,user,"",
                                passwordEt.getText().toString(), "","",loginBeanGenericsCallback);
                    }else {
                        HttpMessageUtil.newInstance().postLogin("2","","",user,
                                passwordEt.getText().toString(),"","",loginBeanGenericsCallback);
                    }
                }else {
                    Toast.makeText(getActivity(),getString(R.string.login_reset_Success),Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }else {
                Toast.makeText(getActivity(),response.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    };

    private GenericsCallback<LoginBean> loginBeanGenericsCallback = new GenericsCallback<LoginBean>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(LoginBean response, int id) {
            if (response.getCode()==200){
                HttpClientUtils.newInstance().setToken(response.getData().getToken());
                MathUtil.newInstance().saveStringData(getActivity().getApplicationContext(),"token",response.getData().getToken());
                MathUtil.newInstance().saveIntData(getActivity().getApplicationContext(),"userId",response.getData().getUserInfo().id);
                SaveDataUtil.newInstance().saveUserInfo(response.getData().getUserInfo());
                getActivity().finish();
            }
        }
    };
}
