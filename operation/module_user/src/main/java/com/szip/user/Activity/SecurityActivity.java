package com.szip.user.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.io.IOException;

import okhttp3.Call;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_CHANGE_PASSWORD;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_USER_FAQ;

public class SecurityActivity extends BaseActivity {

    private TextView accountTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_security);
        setAndroidNativeLightStatusBar(this,true);

        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (userModel==null)
            return;

        setTitle(getString(R.string.user_account_safe));

        accountTv = findViewById(R.id.accountTv);
        if (userModel.phoneNumber!=null)
            accountTv.setText(userModel.phoneNumber);
        else
            accountTv.setText(userModel.email);

        findViewById(R.id.changeRl).setOnClickListener(listener);
        findViewById(R.id.deleteRl).setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.changeRl){
                ARouter.getInstance().build(PATH_ACTIVITY_CHANGE_PASSWORD)
                        .navigation();
            }else if (id == R.id.deleteRl){
                MyAlerDialog.getSingle().showAlerDialog(getString(R.string.tip), getString(R.string.user_deleteTip), getString(R.string.confirm),
                        getString(R.string.cancel), false, new MyAlerDialog.AlerDialogOnclickListener() {
                            @Override
                            public void onDialogTouch(boolean flag) {
                                if (flag){
                                    HttpMessageUtil.newInstance().deleteAccount(new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                                        @Override
                                        public void onError(Call call, Exception e, int id) {

                                        }

                                        @Override
                                        public void onResponse(BaseApi response, int id) {
                                            if (response.getCode()==200){
                                                showToast(getString(R.string.user_deleteSuccess));
                                            }else {
                                                showToast(getString(R.string.user_deleteNow));
                                            }
                                        }
                                    });
                                }
                            }
                        },SecurityActivity.this);
            }
        }
    };
}