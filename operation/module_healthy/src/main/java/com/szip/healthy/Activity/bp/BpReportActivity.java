package com.szip.healthy.Activity.bp;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.tabs.TabLayout;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.ReportConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.healthy.Adapter.MyPagerAdapter;
import com.szip.healthy.R;
import com.szip.healthy.View.CalendarPicker;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_REPORT_BP;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_USER_FAQ;

@Route(path = PATH_ACTIVITY_REPORT_BP)
public class BpReportActivity extends BaseActivity {

    private String[] tabs;
    private TabLayout mTab;
    private ViewPager mPager;

    private long reportDate;
    private TextView timeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.healthy_activity_report);
        ARouter.getInstance().inject(this);
        setAndroidNativeLightStatusBar(this,true);
        tabs = new String[]{getString(R.string.healthy_day),getString(R.string.healthy_week),getString(R.string.healthy_month)};
        reportDate = DateUtil.getTimeOfToday();
        LoadDataUtil.newInstance().initCalendarPoint("bp");
        initView();
        initPage();
    }

    private void initView() {
        setTitle(getString(R.string.healthy_blood_pressure));
        mTab = findViewById(R.id.reportTl);
        mPager = findViewById(R.id.reportVp);
        timeTv = findViewById(R.id.timeTv);
        setTimeText();
        findViewById(R.id.rightIv).setVisibility(View.VISIBLE);
        findViewById(R.id.rightIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARouter.getInstance().build(PATH_ACTIVITY_USER_FAQ)
                        .withString("id","BlOOD_PRESSURE")
                        .navigation();
            }
        });

        timeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarPicker.getInstance()
                        .enableAnimation(true)
                        .setFragmentManager(getSupportFragmentManager())
                        .setAnimationStyle(R.style.CustomAnim)
                        .setDate(DateUtil.getStringDateFromSecond(reportDate,"yyyy-MM-dd"))
                        .setFlag(0)
                        .setCalendarListener(new CalendarPicker.CalendarListener() {
                            @Override
                            public void onClickDate(String date) {
                                if (DateUtil.getTimeScopeForDay(date,"yyyy-MM-dd")>DateUtil.getTimeOfToday()){
                                    showToast(getString(R.string.healthy_tomorrow));
                                }else {
                                    reportDate = DateUtil.getTimeScopeForDay(date,"yyyy-MM-dd");
                                    setTimeText();
                                    Intent intent = new Intent(BroadcastConst.UPDATE_REPORT_TIME);
                                    sendBroadcast(intent);
                                }
                            }
                        })
                        .show();
            }
        });

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initPage() {

        ArrayList<Fragment> fragments = new ArrayList<>();
        // ??????????????????,??????Fragment
        BpFragment dayFragment =  new BpFragment(ReportConst.REPORT_DAY);
        BpFragment weekFragment =  new BpFragment(ReportConst.REPORT_WEEK);
        BpFragment monthFragment =  new BpFragment(ReportConst.REPORT_MONTH);
        // ??????
        fragments.add(dayFragment);
        fragments.add(weekFragment);
        fragments.add(monthFragment);

        // ??????ViewPager?????????
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        myPagerAdapter.setFragmentArrayList(fragments);

        mPager.setAdapter(myPagerAdapter);
        // ?????? TabLayout ??? ViewPager ?????????
        mTab.setupWithViewPager(mPager);

        // TabLayout ????????? (????????????????????????3???Fragment,????????? app?????????Fragment ?????? V4????????? Fragment)
        for (int i = 0; i < myPagerAdapter.getCount(); i++) {
            TabLayout.Tab tab = mTab.getTabAt(i);//???????????????tab
            tab.setCustomView(R.layout.healthy_main_top_layout);//????????????tab??????view
            if (i == 0) {
                // ???????????????tab???TextView?????????????????????
                tab.getCustomView().findViewById(R.id.main_tv).setSelected(true);//?????????tab?????????
            }
            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.main_tv);
            textView.setText(tabs[i]);//??????tab????????????
        }
        mTab.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.main_tv).setSelected(true);
                mPager.setCurrentItem(tab.getPosition());
                setTimeText();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.main_tv).setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setTimeText(){
        if (mTab.getSelectedTabPosition()==2){
            timeTv.setText(DateUtil.getStringDateFromSecond(reportDate,"yyyy/MM"));
        }else if (mTab.getSelectedTabPosition() == 1){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(reportDate*1000);
            calendar.set(Calendar.DAY_OF_WEEK,1);
            timeTv.setText(DateUtil.getStringDateFromSecond(calendar.getTimeInMillis()/1000,"yyyy/MM/dd")
            +"~"+DateUtil.getStringDateFromSecond(calendar.getTimeInMillis()/1000+6*24*60*60,"yyyy/MM/dd"));
        }else {
            timeTv.setText(DateUtil.getStringDateFromSecond(reportDate,"yyyy/MM/dd"));
        }
    }

    public long getReportDate() {
        return reportDate;
    }
}