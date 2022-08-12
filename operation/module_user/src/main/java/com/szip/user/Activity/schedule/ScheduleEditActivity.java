package com.szip.user.Activity.schedule;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.necer.calendar.BaseCalendar;
import com.necer.calendar.MonthCalendar;
import com.necer.listener.OnCalendarChangedListener;
import com.necer.utils.CalendarUtil;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.View.character.OnOptionChangedListener;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.ScheduleData;
import com.szip.blewatch.base.db.dbModel.ScheduleData_Table;
import com.szip.user.R;
import com.szip.user.View.CharacterPickerWindow;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleEditActivity extends BaseActivity implements View.OnClickListener {

    private MonthCalendar monthCalendar;
    private TextView deleteTv;
    private EditText msgEt;
    private TextView timeTv,dateTv,lengthTv;
    private ScheduleData scheduleData;
    private String date;
    private CharacterPickerWindow window;
    private ToActivityBroadcast toActivityBroadcast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_schedule_edit);
        setAndroidNativeLightStatusBar(this,true);
        setTitle(getString(R.string.user_schedule_info));
        scheduleData = (ScheduleData) getIntent().getSerializableExtra("schedule");
        toActivityBroadcast = new ToActivityBroadcast();
        if (scheduleData!=null){
            Log.i("data******","index = "+scheduleData.getIndex());
            date = DateUtil.getStringDateFromSecond(scheduleData.getTime(),"yyyy-MM-dd");
        } else{
            date = DateUtil.getStringDateFromSecond(DateUtil.getTimeOfToday(),"yyyy-MM-dd");
        }
        initView();
        initEvent();
        initWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(BroadcastConst.UPDATE_UI_VIEW);
        toActivityBroadcast.registerReceive(myHandle,ScheduleEditActivity.this,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        toActivityBroadcast.unregister(ScheduleEditActivity.this);
    }

    private void initView() {
        deleteTv = findViewById(R.id.deleteTv);
        timeTv = findViewById(R.id.timeTv);
        msgEt = findViewById(R.id.msgEt);
        dateTv = findViewById(R.id.dateTv);
        lengthTv = findViewById(R.id.lengthTv);
        if (scheduleData!=null){
            deleteTv.setVisibility(View.VISIBLE);
            timeTv.setText(DateUtil.getStringDateFromSecond(scheduleData.getTime(),"HH:mm"));
            msgEt.setText(scheduleData.getMsg());
            lengthTv.setText(String.format("%d/30",scheduleData.getMsg().length()));
        }else {
            timeTv.setText(DateUtil.getStringDateFromSecond(Calendar.getInstance().getTimeInMillis()/1000,"HH:mm"));
            lengthTv.setText("0/30");
        }
        monthCalendar = findViewById(R.id.monthCalendar);
        CalendarUtil.setPointList(new ArrayList<LocalDate>());
        monthCalendar.setInitializeDate(date);
    }

    private void initEvent() {
        findViewById(R.id.timeRl).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.saveTv).setOnClickListener(this);
        findViewById(R.id.lastMonthIv).setOnClickListener(this);
        findViewById(R.id.nextMonthIv).setOnClickListener(this);
        findViewById(R.id.deleteTv).setOnClickListener(this);
        monthCalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, LocalDate localDate, boolean isTouch) {
                dateTv.setText(String.format(Locale.ENGLISH,"%d-%02d",year,month));
                date = localDate.toString();
            }
        });

        msgEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().length();
                lengthTv.setText(String.format("%d/30",len));
                if (len>30)
                    lengthTv.setTextColor(Color.RED);
                else
                    lengthTv.setTextColor(Color.BLACK);
            }
        });
    }

    /**
     * 初始化选择器
     * */
    private void initWindow() {
        //步行计划选择器
        window = new CharacterPickerWindow(this,getString(R.string.user_time));

        final List<String> hourList = MathUtil.newInstance().getNumberList(24);
        final List<String> minList = MathUtil.newInstance().getNumberList(60);
        //初始化选项数据
        window.getPickerView().setPickerWithoutLink(hourList,minList);
        //设置默认选中的三级项目
        String str[] = timeTv.getText().toString().split(":");
        int hour = Integer.valueOf(str[0]);
        int min = Integer.valueOf(str[1]);
        window.setCurrentPositions(hour, min, 0);
        window.setTitleTv(getString(R.string.user_time));
        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                timeTv.setText(String.format("%02d:%02d",option1,option2));
            }
        });
        window.setCyclic(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.timeRl) {
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        } else if (id == R.id.backIv) {
            finish();
        } else if (id == R.id.saveTv) {
            long time = DateUtil.getTimeScope(date + " " +
                    timeTv.getText().toString(), "yyyy-MM-dd HH:mm");
            if (msgEt.getText().toString().length() > 30)
                return;
            if (time < Calendar.getInstance().getTimeInMillis() / 1000) {
                showToast(getString(R.string.user_schedule_past));
                return;
            }
            if (msgEt.getText().toString().trim().equals("")) {
                showToast(getString(R.string.user_schedule_msg));
                return;
            }
            if (scheduleData != null) {
                ProgressHudModel.newInstance().show(ScheduleEditActivity.this, getString(R.string.loading), false);
                scheduleData.setMsg(msgEt.getText().toString().trim());
                scheduleData.setTime(time);
                scheduleData.setType(0);
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command","editSchedule");
                Bundle bundle = new Bundle();
                bundle.putSerializable("schedule",scheduleData);
                intent.putExtra("bundle",bundle);
                sendBroadcast(intent);
            } else {
                if (!LoadDataUtil.newInstance().scheduleCanAdd(time)) {
                    showToast(getString(R.string.user_schedule_same));
                    return;
                }
                ProgressHudModel.newInstance().show(ScheduleEditActivity.this, getString(R.string.loading), false);
                scheduleData = new ScheduleData();
                scheduleData.setMsg(msgEt.getText().toString().trim());
                scheduleData.setTime(time);
                scheduleData.setType(0);
                Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                intent.putExtra("command","addSchedule");
                Bundle bundle = new Bundle();
                bundle.putSerializable("schedule",scheduleData);
                intent.putExtra("bundle",bundle);
                sendBroadcast(intent);
            }
        } else if (id == R.id.lastMonthIv) {
            monthCalendar.toLastPager();
        } else if (id == R.id.nextMonthIv) {
            monthCalendar.toNextPager();
        } else if (id == R.id.deleteTv) {
            Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
            intent.putExtra("command","delSchedule");
            Bundle bundle = new Bundle();
            bundle.putSerializable("schedule",scheduleData);
            intent.putExtra("bundle",bundle);
            sendBroadcast(intent);
        }
    }

    private MyHandle myHandle = new MyHandle() {
        @Override
        public void onReceive(Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastConst.UPDATE_UI_VIEW)){
                ProgressHudModel.newInstance().diss();
                int type = intent.getIntExtra("type",0);
                int state = intent.getIntExtra("state",0xff);
                if (type == 0x52){//添加
                    if (state!=0xff){
                        scheduleData.setIndex(state);
                        scheduleData.save();
                    }else {
                        showToast(getString(R.string.user_schedule_add_fail));
                    }
                }else if (type == 0x53){//删除
                    if (state==1){
                        SQLite.delete().from(ScheduleData.class)
                                .where(ScheduleData_Table.id.is(scheduleData.id))
                                .execute();
                    }else {
                        showToast(getString(R.string.user_schedule_delete_fail));
                    }
                }else if (type== 0x54){//修改
                    if (state==1){
                        scheduleData.update();
                    }else {
                        showToast(getString(R.string.user_schedule_edit_fail));
                    }
                }
                finish();
            }
        }
    };
}