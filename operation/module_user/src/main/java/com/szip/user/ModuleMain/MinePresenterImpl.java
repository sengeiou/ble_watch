package com.szip.user.ModuleMain;

import android.content.Context;
import android.content.Intent;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

public class MinePresenterImpl implements IMinePresenter{
    private UserModel userModel;
    private Context context;
    private IMineView iMineView;

    public MinePresenterImpl(Context context, IMineView iMineView) {
        this.context = context;
        this.iMineView = iMineView;
    }

    @Override
    public void initUser() {
        userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(context));
        if (userModel!=null){
            if (userModel.deviceCode==null){
                iMineView.updateUserInfo(false);
            }else {
                iMineView.updateUserInfo(true);
                getDeviceData();
            }
        }else {
            iMineView.updateUserInfo(false);
        }
        iMineView.updateUserView(userModel);
    }

    @Override
    public void unbind() {
        Intent intent = new Intent(BroadcastConst.UNBIND_SERVICE);
        context.sendBroadcast(intent);
        HttpMessageUtil.newInstance().getUnbindDevice(callback);
    }

    private void getDeviceData(){
        int step = 0;
        int sleep = 0;
        int calorie = 0;
        StepData stepData = LoadDataUtil.newInstance().getStepWithDay(DateUtil.getTimeOfToday());
        if (stepData==null){
            step = 0;
            calorie = 0;
        } else {
            step = stepData.steps;
            calorie = stepData.calorie;
        }


        SleepData sleepData = LoadDataUtil.newInstance().getSleepWithDay(DateUtil.getTimeOfToday());
        if (sleepData==null)
            sleep = 0;
        else
            sleep = sleepData.getDeepTime()+sleepData.getLightTime();
        SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO = LoadDataUtil.newInstance().getSportConfig(userModel.id);
        if (sportWatchAppFunctionConfigDTO == null)
            return;


        iMineView.updateDeviceView(step,sleep,calorie,userModel.stepsPlan,userModel.sleepPlan,userModel.caloriePlan*1000,sportWatchAppFunctionConfigDTO);
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {
            if (response.getCode() == 200){
                SaveDataUtil.newInstance().unbind(MathUtil.newInstance().getUserId(context));
                initUser();
            }
        }
    };
}
