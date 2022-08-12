package com.szip.user.ModuleMain;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.View.BaseFragment;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.user.Activity.AboutActivity;
import com.szip.user.Activity.AutoMeasureActivity;
import com.szip.user.Activity.NotificationActivity;
import com.szip.user.Activity.TargetActivity;
import com.szip.user.Activity.UserSetActivity;
import com.szip.user.Activity.camera.CameraSetActivity;
import com.szip.user.Activity.dial.DialSelectActivity;
import com.szip.user.Activity.schedule.ScheduleActivity;
import com.szip.user.Activity.userInfo.UserInfoActivity;
import com.szip.user.R;
import com.szip.user.Activity.UnitSelectActivity;
import com.szip.user.Adapter.DeviceManagementAdapter;
import com.szip.user.Activity.search.DeviceActivity;
import com.szip.user.Utils.HttpMessageUtil;
import com.szip.user.View.RoundProgressBar;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import okhttp3.Call;

import static android.content.Context.MODE_PRIVATE;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_USER_FAQ;
import static com.szip.blewatch.base.Util.MathUtil.FILE;

/**
 * Created by Administrator on 2019/12/1.
 */

public class MineFragment extends BaseFragment implements View.OnClickListener, MyHandle ,IMineView{

    private CircularImageView iconIv,dialIv;
    private TextView nameTv,stepTv,stepDataTv,stepRateTv,sleepTv,sleepDataTv,sleepRateTv,calorieTv,calorieDataTv,calorieRateTv,stateTv,watchTv;
    private RoundProgressBar stepSb,sleepSb,calorieSb;
    private RecyclerView menuList;
    private RelativeLayout addDeviceRl;
    private LinearLayout deviceLl;
    private ToActivityBroadcast toActivityBroadcast;
    private DeviceManagementAdapter deviceManagementAdapter;
    private IMinePresenter iMinePresenter;
    @Override
    protected int getLayoutId() {
        return R.layout.user_fragment_mine;
    }

    @Override
    protected void afterOnCreated(Bundle savedInstanceState) {
        iMinePresenter = new MinePresenterImpl(getActivity().getApplicationContext(),this);
        initView();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        iMinePresenter.initUser();
        if (toActivityBroadcast == null)
            toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_UI_VIEW);
        intentFilter.addAction(BroadcastConst.UPDATE_BLE_STATE);
        toActivityBroadcast.registerReceive(this,getActivity().getApplicationContext(),intentFilter);
        getActivity().sendBroadcast(new Intent(BroadcastConst.CHECK_BLE_STATE));
        if (deviceManagementAdapter!=null){
            deviceManagementAdapter.update();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        toActivityBroadcast.unregister(getActivity().getApplicationContext());
    }

    private void initEvent() {
        nameTv.setOnClickListener(this);
        iconIv.setOnClickListener(this);
        addDeviceRl.setOnClickListener(this);
        getView().findViewById(R.id.setIv).setOnClickListener(this);
        getView().findViewById(R.id.unbindIv).setOnClickListener(this);
        getView().findViewById(R.id.updateIv).setOnClickListener(this);
        getView().findViewById(R.id.editPlanTv).setOnClickListener(this);
        getView().findViewById(R.id.dialLl).setOnClickListener(this);
    }
    private void initView() {
        nameTv = getView().findViewById(R.id.nameTv);
        stepTv = getView().findViewById(R.id.stepTv);
        stepDataTv = getView().findViewById(R.id.stepDataTv);
        stepRateTv = getView().findViewById(R.id.stepRateTv);
        sleepTv = getView().findViewById(R.id.sleepTv);
        sleepDataTv = getView().findViewById(R.id.sleepDataTv);
        sleepRateTv = getView().findViewById(R.id.sleepRateTv);
        calorieTv = getView().findViewById(R.id.calorieTv);
        calorieDataTv = getView().findViewById(R.id.calorieDataTv);
        calorieRateTv = getView().findViewById(R.id.calorieRateTv);
        stepSb = getView().findViewById(R.id.stepSb);
        sleepSb = getView().findViewById(R.id.sleepSb);
        calorieSb = getView().findViewById(R.id.calorieSb);
        dialIv = getView().findViewById(R.id.dialIv);
        iconIv = getView().findViewById(R.id.iconIv);
        watchTv = getView().findViewById(R.id.watchTv);
        stateTv = getView().findViewById(R.id.stateTv);
        menuList = getView().findViewById(R.id.menuList);
        addDeviceRl = getView().findViewById(R.id.addDeviceRl);
        deviceLl = getView().findViewById(R.id.deviceLl);
        menuList.setLayoutManager(new GridLayoutManager(getContext(), 3));
        deviceManagementAdapter = new DeviceManagementAdapter(getActivity().getApplicationContext());
        deviceManagementAdapter.setOnItemClickListener(listener);
        menuList.setAdapter(deviceManagementAdapter);
        menuList.setHasFixedSize(true);
        menuList.setNestedScrollingEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iconIv || id == R.id.nameTv) {
            if (!MathUtil.newInstance().needLogin(getActivity())) {
                startActivity(new Intent(getActivity(), UserInfoActivity.class));
            }
        }else if (id == R.id.setIv) {
            if (!MathUtil.newInstance().needLogin(getActivity())) {
                startActivity(new Intent(getActivity(), UserSetActivity.class));
            }
        }else if (id == R.id.addDeviceRl) {
            if (!MathUtil.newInstance().needLogin(getActivity())) {
                startActivity(new Intent(getActivity(), DeviceActivity.class));
            }
        }else if (id == R.id.unbindIv){
            MyAlerDialog.getSingle().showAlerDialog(getString(R.string.user_unbind_tip), getString(R.string.user_unbind_message),
                    getString(R.string.user_confirm), getString(R.string.user_cancel),
                    true, flag -> {
                        if (flag){
                            iMinePresenter.unbind();
                        }
                    }, getActivity());
        }else if (id == R.id.updateIv){
            ProgressHudModel.newInstance().show(getActivity(),getString(R.string.user_upload_data),false);
            String datas = MathUtil.newInstance().getStringWithJson(getContext().getSharedPreferences(FILE,MODE_PRIVATE));
            HttpMessageUtil.newInstance().postForUploadReportData(datas, new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                @Override
                public void onError(Call call, Exception e, int id) {
                    ProgressHudModel.newInstance().diss();
                    showToast(e.getMessage());
                }
                @Override
                public void onResponse(BaseApi response, int id) {
                    if(response.getCode()==200){
                        ProgressHudModel.newInstance().diss();
                        MathUtil.newInstance().saveLastTime(getContext().getSharedPreferences(FILE,MODE_PRIVATE));
                        showToast(getString(R.string.user_upload_data_success));
                    }
                }
            });
        }else if (id == R.id.dialLl){
            startActivity(new Intent(getActivity(), DialSelectActivity.class));
        }else if (id == R.id.editPlanTv){
            if (!MathUtil.newInstance().needLogin(getActivity())) {
                startActivity(new Intent(getActivity(), TargetActivity.class));
            }
        }
    }

    private OnItemClickListener listener = new OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position == R.string.user_find_watch) {
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command", "findWatch");
                getActivity().sendBroadcast(intent);
            } else if (position == R.string.user_ble_call) {
                ARouter.getInstance().build(PATH_ACTIVITY_USER_FAQ)
                        .withString("id", "CALL")
                        .navigation();
            } else if (position == R.string.user_ble_camera) {
                startActivity(new Intent(getActivity(), CameraSetActivity.class));
            } else if (position == R.string.user_unit) {
                startActivity(new Intent(getActivity(), UnitSelectActivity.class));
            } else if (position == R.string.user_notification) {
                startActivity(new Intent(getActivity(), NotificationActivity.class));
            } else if (position == R.string.user_about) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }else if (position == R.string.user_schedule) {
                startActivity(new Intent(getActivity(), ScheduleActivity.class));
            }else if (position == R.string.user_auto) {
                startActivity(new Intent(getActivity(), AutoMeasureActivity.class));
            }
        }
    };

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_BLE_STATE:
                int state = intent.getIntExtra("state",0);
                if (state==3){
                    stateTv.setText(getString(R.string.user_connect));
                }else if (state == 2){
                    stateTv.setText(getString(R.string.user_connecting));
                }else if (state == 4){
                    stateTv.setText(getString(R.string.user_searching));
                }else {
                    stateTv.setText(getString(R.string.user_disconnect));
                }
                break;
            case BroadcastConst.UPDATE_UI_VIEW:{
                String command = intent.getStringExtra("command");
                if (null!=command&&command.equals("find")){
                    showToast(getString(R.string.watch_ring));
                }else {
                    if(null!=iMinePresenter)
                        iMinePresenter.initUser();
                }
            }
                break;
        }
    }

    @Override
    public void updateUserInfo(boolean userVisible) {
        if (userVisible){
            deviceLl.setVisibility(View.VISIBLE);
            addDeviceRl.setVisibility(View.GONE);
        }else {
            deviceLl.setVisibility(View.GONE);
            addDeviceRl.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateUserView(UserModel userModel) {
        if (userModel!=null){
            nameTv.setText(userModel.userName);
            Glide.with(getActivity()).load(userModel.avatar)
                    .fallback(R.mipmap.my_head_58)
                    .error(R.mipmap.my_head_58)
                    .into(iconIv);
        }else {
            nameTv.setText(getString(R.string.user_name));
            iconIv.setImageResource(R.mipmap.my_head_58);
        }

    }

    @Override
    public void updateDeviceView(int step, int sleep, int calorie,int stepPlan,int sleepPlan,int caloriePlan, SportWatchAppFunctionConfigDTO device) {
        stepTv.setText(String.format("%d",stepPlan));
        sleepTv.setText(String.format("%.1f h",sleepPlan/60f));
        calorieTv.setText(String.format("%d kcal",caloriePlan/1000));

        stepSb.setRatio(step>=stepPlan?1:step/(float)stepPlan);
        sleepSb.setRatio(sleep>=sleepPlan?1:sleep/(float)sleepPlan);
        calorieSb.setRatio(calorie>=caloriePlan?1:calorie/(float)caloriePlan);

        stepDataTv.setText(String.format("%d",step));
        sleepDataTv.setText(String.format("%dh%dmin",sleep/60,sleep%60));
        calorieDataTv.setText(String.format("%.1fkcal",((calorie+55)/100)/10f));

        stepRateTv.setText(String.format("%.1f%%",step>=stepPlan?100:step/(float)stepPlan*100));
        sleepRateTv.setText(String.format("%.1f%%",sleep>=sleepPlan?100:sleep/(float)sleepPlan*100));
        calorieRateTv.setText(String.format("%.1f%%",calorie>=caloriePlan?100:calorie/(float)caloriePlan*100));

        watchTv.setText(device.appName);
        Glide.with(getActivity()).load(device.dialImg).into(dialIv);
    }
}
