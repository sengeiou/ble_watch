package com.szip.blewatch.Activity.welcome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;


import com.szip.blewatch.R;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.HttpModel.UserInfoBean;
import com.szip.blewatch.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.io.IOException;

import okhttp3.Call;

import static android.content.Context.MODE_PRIVATE;
import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class WelcomePresenterImpl implements IWelcomePresenter{

    private IWelcomeView iWelcomeView;
    private Handler handler;

    public WelcomePresenterImpl(IWelcomeView iWelcomeView) {
        this.iWelcomeView = iWelcomeView;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void checkPrivacy(Context context) {

        final SharedPreferences sharedPreferences = context.getSharedPreferences("ble_watch",MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirst",true)){
            MyAlerDialog.getSingle().showAlerDialogWithPrivacy(context.getString(R.string.privacy1), context.getString(R.string.privacyTip),
                    null, null, false, new MyAlerDialog.AlerDialogOnclickListener() {
                        @Override
                        public void onDialogTouch(boolean flag) {
                            if (flag){
                                sharedPreferences.edit().putBoolean("isFirst",false).commit();
                                if (iWelcomeView!=null)
                                    iWelcomeView.checkPrivacyResult(true);
                            }else{
                                if (iWelcomeView!=null)
                                    iWelcomeView.checkPrivacyResult(false);
                            }
                        }
                    },context);
        }else {
            if (iWelcomeView!=null)
                iWelcomeView.checkPrivacyResult(true);
        }

    }

    @Override
    public void initBle(Context context) {
        if (iWelcomeView!=null)
            iWelcomeView.initBleFinish();
    }

    @Override
    public void initDeviceConfig() {
        if (iWelcomeView!=null)
            iWelcomeView.initDeviceConfigFinish();
    }

    @Override
    public void initUserInfo(final Context context) {
        //判断登录状态
        String token = MathUtil.newInstance().getToken(context);
        if (token!=null){//已登录
            HttpClientUtils.newInstance().setToken(token);
            try {
                HttpMessageUtil.newInstance().getForGetInfo(new GenericsCallback<UserInfoBean>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (iWelcomeView!=null)
                            iWelcomeView.initUserinfoFinish(false);
                    }

                    @Override
                    public void onResponse(UserInfoBean response, int id) {
                        if (response.getCode() == 200){
                            SaveDataUtil.newInstance().saveUserInfo(response.getData());
                            if (iWelcomeView!=null)
                                iWelcomeView.initUserinfoFinish(false);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                if (iWelcomeView!=null)
                    iWelcomeView.initUserinfoFinish(false);
            }
        }else {
            if (iWelcomeView!=null)
                iWelcomeView.initUserinfoFinish(true);
        }
    }

    @Override
    public void setViewDestory() {
        iWelcomeView = null;
    }

}
