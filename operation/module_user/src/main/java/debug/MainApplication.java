package debug;

import android.content.Intent;

import com.szip.blewatch.base.BaseApplication;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.http.TokenInterceptor;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.blewatch.base.Service.BleService;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

public class MainApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        MathUtil.newInstance().saveStringData(getApplicationContext(),"token","TjFcXs~ORm3tsLEz!19x");
        MathUtil.newInstance().saveIntData(getApplicationContext(),"userId",46);
        HttpClientUtils.newInstance().setToken("TjFcXs~ORm3tsLEz!19x");
        startService(new Intent(this,BleService.class));


        GetBuilder getBuilder = OkHttpUtils
                .get()
                .addInterceptor(new TokenInterceptor());
        HttpClientUtils.newInstance().buildRequest(getBuilder,"v2/user/getUserInfo",new GenericsCallback<UserInfoBean>(new JsonGenericsSerializator()) {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(UserInfoBean response, int id) {
                if (response.getCode() == 200){
                    SaveDataUtil.newInstance().saveUserInfo(response.getData());

                }else if (response.getCode() == 401){
                    MathUtil.newInstance().saveStringData(MainApplication.this,"token",null);

                }
            }
        });

//        String packageName = getPackageName();
//        String strListener = Settings.Secure.getString(this.getContentResolver(),
//                "enabled_notification_listeners");
//        if (strListener != null
//                && strListener
//                .contains(packageName)) {
//            ComponentName localComponentName = new ComponentName(this, MyNotificationReceiver.class);
//            PackageManager localPackageManager = this.getPackageManager();
//            localPackageManager.setComponentEnabledSetting(localComponentName, 2, 1);
//            localPackageManager.setComponentEnabledSetting(localComponentName, 1, 1);
//        }
    }

    private class UserInfoBean extends BaseApi {
        private UserModel data;

        public UserModel getData() {
            return data;
        }
    }

}