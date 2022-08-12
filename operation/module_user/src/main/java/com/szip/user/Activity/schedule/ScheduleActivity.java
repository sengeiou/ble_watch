package com.szip.user.Activity.schedule;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.ScheduleData;
import com.szip.user.Adapter.ScheduleAdapter;
import com.szip.user.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends BaseActivity {

    private ImageView rightIv;
    private ListView listView;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleData> list = new ArrayList<>();

    private ToActivityBroadcast toActivityBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_schedule);
        setAndroidNativeLightStatusBar(this,true);
        toActivityBroadcast = new ToActivityBroadcast();
        setTitle(getString(R.string.user_schedule));
        SQLite.delete()
                .from(ScheduleData.class)
                .execute();

        initView();
        initEvent();
        Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
        intent.putExtra("command","getSchedule");
        sendBroadcast(intent);
    }

    private void initData() {
        list = LoadDataUtil.newInstance().getScheduleData();
        if(list!=null&&list.size()!=0)
            findViewById(R.id.listEmptyTv).setVisibility(View.GONE);
        else {
            findViewById(R.id.listEmptyTv).setVisibility(View.VISIBLE);
        }
        scheduleAdapter.setList(list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_UI_VIEW);
        toActivityBroadcast.registerReceive(myHandle,ScheduleActivity.this,intentFilter);
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        toActivityBroadcast.unregister(ScheduleActivity.this);
    }

    private void initView() {
        rightIv = findViewById(R.id.rightIv);
        rightIv.setVisibility(View.VISIBLE);
        rightIv.setImageResource(R.mipmap.schedule_icon_add);
        listView = findViewById(R.id.listView);
        scheduleAdapter = new ScheduleAdapter(getApplicationContext());
        listView.setAdapter(scheduleAdapter);
    }

    private void initEvent() {
        rightIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.size()<10)
                    startActivityForResult(new Intent(ScheduleActivity.this,ScheduleEditActivity.class),100);
                else
                    showToast(getString(R.string.user_schedule_fall));
            }
        });

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScheduleActivity.this,ScheduleEditActivity.class);
                intent.putExtra("schedule",list.get(position));
                startActivityForResult(intent,100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            if (resultCode==101){
                ScheduleData scheduleData = (ScheduleData) data.getSerializableExtra("schedule");
                if (scheduleData==null)
                    return;
                findViewById(R.id.listEmptyTv).setVisibility(View.GONE);
                list.add(scheduleData);
                scheduleAdapter.setList(list);
            }else if (resultCode == 102){
                int index = data.getIntExtra("index",-1);
                if (index == -1)
                    return;
                for (int i = 0;i<list.size();i++){
                    if (list.get(i).getIndex()==index){
                        list.remove(i);
                        break;
                    }
                }
                scheduleAdapter.setList(list);
                if (list.size()==0)
                    findViewById(R.id.listEmptyTv).setVisibility(View.VISIBLE);
            }else if (resultCode == 103){
                ScheduleData scheduleData = (ScheduleData) data.getSerializableExtra("schedule");
                if (scheduleData == null)
                    return;
                for (int i = 0;i<list.size();i++){
                    if (list.get(i).getIndex()==scheduleData.getIndex()){
                        list.set(i,scheduleData);
                        break;
                    }
                }
                scheduleAdapter.setList(list);
            }
        }
    }

    private MyHandle myHandle = intent -> {
        String action = intent.getAction();
        if (action.equals(BroadcastConst.UPDATE_UI_VIEW)){
            initData();
        }
    };
}