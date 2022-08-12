package com.szip.user.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.View.character.OnOptionChangedListener;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.szip.user.View.CharacterPickerWindow;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;

public class TargetActivity extends BaseActivity implements View.OnClickListener {

    private CharacterPickerWindow window,window1,window2;
    private TextView stepTv,sleepTv,calorieTv;
    private int stepPlan = 0,sleepPlan = 0,caloriePlan = 0;
    private int STEP_FLAG = 1,SLEEP_FLAG = 2,CALORIE_FLAG = 3;


    private UserModel userModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_target);
        setAndroidNativeLightStatusBar(this,true);
        userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (userModel == null)
            return;
        initView();
        initEvent();
        initWindow();
    }


    private void initView() {
        setTitle(getString(R.string.user_plan));
        stepTv = findViewById(R.id.stepTv);
        sleepTv = findViewById(R.id.sleepTv);
        calorieTv = findViewById(R.id.calorieTv);

        stepTv.setText(userModel.stepsPlan+"");
        sleepTv.setText(String.format(Locale.ENGLISH,"%.1fh",userModel.sleepPlan/60f));
        calorieTv.setText(String.format(Locale.ENGLISH,"%dkcal",userModel.caloriePlan));
    }

    private void initEvent() {
        findViewById(R.id.stepRl).setOnClickListener(this);
        findViewById(R.id.sleepRl).setOnClickListener(this);
        findViewById(R.id.calorieRl).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
    }

    private void initWindow() {
        //步行计划选择器
        window = new CharacterPickerWindow(this,getString(R.string.user_step));

        final List<String> stepList = MathUtil.newInstance().getStepPlanList();
        //初始化选项数据
        window.getPickerView().setPicker(stepList);
        //设置默认选中的三级项目
        window.setCurrentPositions(userModel.stepsPlan/500-8, 0, 0);
        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    ProgressHudModel.newInstance().show(TargetActivity.this,getString(R.string.waiting),false);
                    HttpMessageUtil.newInstance().postForSetStepsPlan(stepList.get(option1),STEP_FLAG,callback);
                    stepPlan = Integer.valueOf(stepList.get(option1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //睡眠计划选择器
        window1 = new CharacterPickerWindow(this,getString(R.string.user_sleep));

        final ArrayList<String> sleepList = MathUtil.newInstance().getSleepPlanList();
        window1.getPickerView().setText("h","");
        //初始化选项数据
        window1.getPickerView().setPicker(sleepList);
        //设置默认选中的三级项目
        window1.setCurrentPositions(userModel.sleepPlan/30-10, 0, 0);
        //监听确定选择按钮
        window1.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    ProgressHudModel.newInstance().show(TargetActivity.this,getString(R.string.waiting),false);
                    HttpMessageUtil.newInstance().postForSetSleepPlan((int)(Float.valueOf(sleepList.get(option1))*60)+"",SLEEP_FLAG,callback);
                    sleepPlan = (int)(Float.valueOf(sleepList.get(option1))*60);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //消耗计划选择器
        window2 = new CharacterPickerWindow(this,getString(R.string.user_sleep));

        final ArrayList<String> calorieList = MathUtil.newInstance().getCaloriePlanList();
        window2.getPickerView().setText("kcal","");
        //初始化选项数据
        window2.getPickerView().setPicker(calorieList);
        //设置默认选中的三级项目
        window2.setCurrentPositions(userModel.caloriePlan/50-2, 0, 0);
        //监听确定选择按钮
        window2.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                try {
                    ProgressHudModel.newInstance().show(TargetActivity.this,getString(R.string.waiting),false);
                    HttpMessageUtil.newInstance().postForSetCaloriePlan(Integer.valueOf(calorieList.get(option1))+"",CALORIE_FLAG,callback);
                    caloriePlan = Integer.valueOf(calorieList.get(option1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.stepRl){
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.sleepRl){
            window1.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.calorieRl){
            window2.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.backIv){
            finish();
        }
    }

    private GenericsCallback<BaseApi> callback = new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(BaseApi response, int id) {
            if (response.getCode()==200){
                ProgressHudModel.newInstance().diss();
                if (id == STEP_FLAG){
                    stepTv.setText(stepPlan+"");
                    userModel.stepsPlan = stepPlan;
                    Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                    intent.putExtra("command","setInfo");
                    sendBroadcast(intent);
                }else if (id == SLEEP_FLAG){
                    sleepTv.setText(String.format(Locale.ENGLISH,"%.1fh",sleepPlan/60f));
                    userModel.sleepPlan = sleepPlan;
                }else if (id == CALORIE_FLAG){
                    calorieTv.setText(String.format(Locale.ENGLISH,"%d kcal",caloriePlan));
                    userModel.caloriePlan = caloriePlan;
                }
                userModel.update();
            }else {
                showToast(response.getMessage());
            }
        }
    };
}