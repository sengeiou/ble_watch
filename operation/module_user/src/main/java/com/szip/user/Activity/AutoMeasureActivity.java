package com.szip.user.Activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.View.character.OnOptionChangedListener;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.AutoMeasureData;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.R;
import com.szip.user.View.CharacterPickerWindow;

import java.util.List;

public class AutoMeasureActivity extends BaseActivity {

    private Switch heartSw,bpSw,spoSw,tempSw;
    private LinearLayout bpLl,heartLl,spoLl,tempLl;
    private TextView heartFrequencyTv,bpFrequencyTv,spoFrequencyTv,tempFrequencyTv;
    private TextView heartStartTv,bpStartTv,spoStartTv,tempStartTv;
    private TextView heartEndTv,bpEndTv,spoEndTv,tempEndTv;
    private CharacterPickerWindow window;
    private AutoMeasureData autoMeasureData;

    private ToActivityBroadcast toActivityBroadcast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_auto_measure);
        setAndroidNativeLightStatusBar(this,true);
        autoMeasureData = LoadDataUtil.newInstance().getAutoMeasureData();
        initView();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(toActivityBroadcast==null)
            toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_UI_VIEW);
        toActivityBroadcast.registerReceive(myHandle,this,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        toActivityBroadcast.unregister(this);
    }

    private void initView() {
        setTitle(getString(R.string.user_auto));
        heartSw = findViewById(R.id.heartSw);
        heartSw.setChecked(autoMeasureData.heartState==1);
        bpSw = findViewById(R.id.bpSw);
        bpSw.setChecked(autoMeasureData.bpState==1);
        spoSw = findViewById(R.id.spoSw);
        spoSw.setChecked(autoMeasureData.spoState==1);
        tempSw = findViewById(R.id.tempSw);
        tempSw.setChecked(autoMeasureData.tempState==1);

        heartFrequencyTv = findViewById(R.id.heartFrequencyTv);
        heartFrequencyTv.setText(String.format("%02dmin",autoMeasureData.heartFrequency));
        bpFrequencyTv = findViewById(R.id.bpFrequencyTv);
        bpFrequencyTv.setText(String.format("%02dmin",autoMeasureData.bpFrequency));
        spoFrequencyTv = findViewById(R.id.spoFrequencyTv);
        spoFrequencyTv.setText(String.format("%02dmin",autoMeasureData.spoFrequency));
        tempFrequencyTv = findViewById(R.id.tempFrequencyTv);
        tempFrequencyTv.setText(String.format("%02dmin",autoMeasureData.tempFrequency));

        heartStartTv = findViewById(R.id.heartStartTv);
        heartStartTv.setText(String.format("%02d:%02d",autoMeasureData.heartStartTime/60,autoMeasureData.heartStartTime%60));
        bpStartTv = findViewById(R.id.bpStartTv);
        bpStartTv.setText(String.format("%02d:%02d",autoMeasureData.bpStartTime/60,autoMeasureData.bpStartTime%60));
        spoStartTv = findViewById(R.id.spoStartTv);
        spoStartTv.setText(String.format("%02d:%02d",autoMeasureData.spoStartTime/60,autoMeasureData.spoStartTime%60));
        tempStartTv = findViewById(R.id.tempStartTv);
        tempStartTv.setText(String.format("%02d:%02d",autoMeasureData.tempStartTime/60,autoMeasureData.tempStartTime%60));

        heartEndTv = findViewById(R.id.heartEndTv);
        heartEndTv.setText(String.format("%02d:%02d",autoMeasureData.heartEndTime/60,autoMeasureData.heartEndTime%60));
        bpEndTv = findViewById(R.id.bpEndTv);
        bpEndTv.setText(String.format("%02d:%02d",autoMeasureData.bpEndTime/60,autoMeasureData.bpEndTime%60));
        spoEndTv = findViewById(R.id.spoEndTv);
        spoEndTv.setText(String.format("%02d:%02d",autoMeasureData.spoEndTime/60,autoMeasureData.spoEndTime%60));
        tempEndTv = findViewById(R.id.tempEndTv);
        tempEndTv.setText(String.format("%02d:%02d",autoMeasureData.tempEndTime/60,autoMeasureData.tempEndTime%60));

        bpLl = findViewById(R.id.bpLl);
        spoLl = findViewById(R.id.spoLl);
        heartLl = findViewById(R.id.heartLl);
        tempLl = findViewById(R.id.tempLl);

        SportWatchAppFunctionConfigDTO sportWatchAppFunctionConfigDTO = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (sportWatchAppFunctionConfigDTO==null)
            return;
        if(sportWatchAppFunctionConfigDTO.bloodPressureAutoTest==1)
            bpLl.setVisibility(View.VISIBLE);
        if(sportWatchAppFunctionConfigDTO.bloodOxygenAutoTest==1)
            spoLl.setVisibility(View.VISIBLE);
        if(sportWatchAppFunctionConfigDTO.heartRateAutoTest==1)
            heartLl.setVisibility(View.VISIBLE);
        if(sportWatchAppFunctionConfigDTO.temperatureAutoTest==1)
            tempLl.setVisibility(View.VISIBLE);
    }

    private void initEvent() {
        findViewById(R.id.heartStartLl).setOnClickListener(onClickListener);
        findViewById(R.id.heartEndLl).setOnClickListener(onClickListener);
        findViewById(R.id.heartFrequencyLl).setOnClickListener(onClickListener);
        findViewById(R.id.heartSw).setOnClickListener(onClickListener);
        findViewById(R.id.bpStartLl).setOnClickListener(onClickListener);
        findViewById(R.id.bpEndLl).setOnClickListener(onClickListener);
        findViewById(R.id.bpFrequencyLl).setOnClickListener(onClickListener);
        findViewById(R.id.bpSw).setOnClickListener(onClickListener);
        findViewById(R.id.spoStartLl).setOnClickListener(onClickListener);
        findViewById(R.id.spoEndLl).setOnClickListener(onClickListener);
        findViewById(R.id.spoFrequencyLl).setOnClickListener(onClickListener);
        findViewById(R.id.spoSw).setOnClickListener(onClickListener);
        findViewById(R.id.tempStartLl).setOnClickListener(onClickListener);
        findViewById(R.id.tempEndLl).setOnClickListener(onClickListener);
        findViewById(R.id.tempFrequencyLl).setOnClickListener(onClickListener);
        findViewById(R.id.tempSw).setOnClickListener(onClickListener);
        findViewById(R.id.saveTv).setOnClickListener(onClickListener);
        findViewById(R.id.backIv).setOnClickListener(onClickListener);
    }

    /**
     * 初始化选择器
     * */
    private void initWindow(int title,List<String> first,List<String> second,List<String> third,
                          int current1,int current2,int current3,OnOptionChangedListener listener) {
        //步行计划选择器
        window = new CharacterPickerWindow(this,getString(title));
        //初始化选项数据
        window.getPickerView().setPickerWithoutLink(first,second,third);
        //设置默认选中的三级项目
        window.setCurrentPositions(current1,current2,current3);
        //监听确定选择按钮
        window.setOnoptionsSelectListener(listener);
        window.setCyclic(true);
    }

    private OnOptionChangedListener heartFrequency = (option1, option2, option3) -> {
        autoMeasureData.heartFrequency = (option1+1)*30;
        heartFrequencyTv.setText(String.format("%02dmin",(option1+1)*30));
    };
    private OnOptionChangedListener heartStart = (option1, option2, option3) -> {
        autoMeasureData.heartStartTime = option1*60+option2;
        heartStartTv.setText(String.format("%02d:%02d",option1,option2));
    };
    private OnOptionChangedListener heartEnd = (option1, option2, option3) -> {
        autoMeasureData.heartEndTime = option1*60+option2;
        heartEndTv.setText(String.format("%02d:%02d",option1,option2));
    };

    private OnOptionChangedListener bpFrequency = (option1, option2, option3) -> {
        autoMeasureData.bpFrequency = (option1+1)*30;
        bpFrequencyTv.setText(String.format("%02dmin",(option1+1)*30));
    };
    private OnOptionChangedListener bpStart = (option1, option2, option3) -> {
        autoMeasureData.bpStartTime = option1*60+option2;
        bpStartTv.setText(String.format("%02d:%02d",option1,option2));
    };
    private OnOptionChangedListener bpEnd = (option1, option2, option3) -> {
        autoMeasureData.bpEndTime = option1*60+option2;
        bpEndTv.setText(String.format("%02d:%02d",option1,option2));
    };

    private OnOptionChangedListener spoFrequency = (option1, option2, option3) -> {
        autoMeasureData.spoFrequency = (option1+1)*30;
        spoFrequencyTv.setText(String.format("%02dmin",(option1+1)*30));
    };
    private OnOptionChangedListener spoStart = (option1, option2, option3) -> {
        autoMeasureData.spoStartTime = option1*60+option2;
        spoStartTv.setText(String.format("%02d:%02d",option1,option2));
    };
    private OnOptionChangedListener spoEnd = (option1, option2, option3) -> {
        autoMeasureData.spoEndTime = option1*60+option2;
        spoEndTv.setText(String.format("%02d:%02d",option1,option2));
    };

    private OnOptionChangedListener tempFrequency = (option1, option2, option3) -> {
        autoMeasureData.tempFrequency = (option1+1)*30;
        tempFrequencyTv.setText(String.format("%02dmin",(option1+1)*30));
    };
    private OnOptionChangedListener tempStart = (option1, option2, option3) -> {
        autoMeasureData.tempStartTime = option1*60+option2;
        tempStartTv.setText(String.format("%02d:%02d",option1,option2));
    };
    private OnOptionChangedListener tempEnd = (option1, option2, option3) -> {
        autoMeasureData.tempEndTime = option1*60+option2;
        tempEndTv.setText(String.format("%02d:%02d",option1,option2));
    };

    private View.OnClickListener onClickListener = v -> {
        int id = v.getId();
        if (id == R.id.heartFrequencyLl){
            final List<String> frequencyList = MathUtil.newInstance().getFrequencyList(6);
            String str = heartFrequencyTv.getText().toString();
            str = str.substring(0,str.length()-3);
            int current = Integer.valueOf(str)/30-1;
            initWindow(R.string.user_start_time,frequencyList,null,null,current,0,0,heartFrequency);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.heartStartLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = heartStartTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,heartStart);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.heartEndLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = heartEndTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,heartEnd);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.bpFrequencyLl){
            final List<String> frequencyList = MathUtil.newInstance().getFrequencyList(6);
            String str = bpFrequencyTv.getText().toString();
            str = str.substring(0,str.length()-3);
            int current = Integer.valueOf(str)/30-1;
            initWindow(R.string.user_start_time,frequencyList,null,null,current,0,0,bpFrequency);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.bpStartLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = bpStartTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,bpStart);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.bpEndLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = bpEndTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,bpEnd);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.spoFrequencyLl){
            final List<String> frequencyList = MathUtil.newInstance().getFrequencyList(6);
            String str = spoFrequencyTv.getText().toString();
            str = str.substring(0,str.length()-3);
            int current = Integer.valueOf(str)/30-1;
            initWindow(R.string.user_start_time,frequencyList,null,null,current,0,0,spoFrequency);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.spoStartLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = spoStartTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,spoStart);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.spoEndLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = spoEndTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,spoEnd);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.tempFrequencyLl){
            final List<String> frequencyList = MathUtil.newInstance().getFrequencyList(6);
            String str = tempFrequencyTv.getText().toString();
            str = str.substring(0,str.length()-3);
            int current = Integer.valueOf(str)/30-1;
            initWindow(R.string.user_start_time,frequencyList,null,null,current,0,0,tempFrequency);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.tempStartLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = tempStartTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,tempStart);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.tempEndLl){
            final List<String> hourList = MathUtil.newInstance().getNumberList(24);
            final List<String> minList = MathUtil.newInstance().getNumberList(60);
            String str[] = tempEndTv.getText().toString().split(":");
            int hour = Integer.valueOf(str[0]);
            int min = Integer.valueOf(str[1]);
            initWindow(R.string.user_start_time,hourList,minList,null,hour,min,0,tempEnd);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }else if (id == R.id.heartSw){
            autoMeasureData.heartState=heartSw.isChecked()?1:0;
        }else if (id == R.id.bpSw){
            autoMeasureData.bpState=bpSw.isChecked()?1:0;
        }else if (id == R.id.spoSw){
            autoMeasureData.spoState=spoSw.isChecked()?1:0;
        }else if (id == R.id.tempSw){
            autoMeasureData.tempState=tempSw.isChecked()?1:0;
        }else if (id == R.id.saveTv){
            autoMeasureData.save();
            Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
            intent.putExtra("command","setAuto");
            sendBroadcast(intent);
        }else if (id == R.id.backIv){
            finish();
        }
    };



    private MyHandle myHandle = new MyHandle() {
        @Override
        public void onReceive(Intent intent) {
            switch (intent.getAction()){
                case BroadcastConst.UPDATE_UI_VIEW:{
                    String command = intent.getStringExtra("command");
                    if (null!=command&&command.equals("setAuto")){
                        showToast(getString(R.string.save_success));
                        finish();
                    }
                }
                break;
            }
        }
    };
}