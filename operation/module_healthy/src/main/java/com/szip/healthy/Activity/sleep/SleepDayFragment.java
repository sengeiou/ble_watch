package com.szip.healthy.Activity.sleep;

import android.content.Intent;
import android.content.IntentFilter;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.View.BaseLazyLoadingFragment;
import com.szip.healthy.Activity.heart.HeartReportActivity;
import com.szip.healthy.Activity.step.StepReportActivity;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;
import com.szip.healthy.View.ReportTableView;
import com.szip.healthy.View.SleepArcProgressBar;

import java.util.List;
import java.util.Locale;

public class SleepDayFragment extends BaseLazyLoadingFragment implements ISleepReportView, MyHandle {

    private TextView deepTv,lightTv,bedTv,wakeTv,allSleepTv;
    private ReportTableView tableView;
    private SleepArcProgressBar sleepPb;
    private ISleepReportPresenter iSleepReportPresenter;
    private ToActivityBroadcast toActivityBroadcast;

    @Override
    protected int getLayoutId() {
        return R.layout.healthy_fragment_sleep_day;
    }

    @Override
    protected void initView(View root) {
        deepTv = root.findViewById(R.id.deepTv);
        lightTv = root.findViewById(R.id.lightTv);
        bedTv = root.findViewById(R.id.bedTv);
        wakeTv = root.findViewById(R.id.wakeTv);
        allSleepTv = root.findViewById(R.id.allSleepTv);
        tableView = root.findViewById(R.id.tableView);
        sleepPb = root.findViewById(R.id.sleepPb);
        toActivityBroadcast = new ToActivityBroadcast();
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        iSleepReportPresenter = new SleepDayPresenterImpl(getActivity().getApplicationContext(),this);
    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        iSleepReportPresenter.register(this);
        IntentFilter intentFilter = new IntentFilter(BroadcastConst.UPDATE_REPORT_TIME);
        toActivityBroadcast.registerReceive(this,getActivity().getApplicationContext(),intentFilter);
        iSleepReportPresenter.loadData(((SleepReportActivity)getActivity()).getReportDate());
    }

    @Override
    protected void onFragmentPause() {
        super.onFragmentPause();
        iSleepReportPresenter.unRegister();
        toActivityBroadcast.unregister(getActivity().getApplicationContext());
    }

    @Override
    public void updateTableView(List<ReportData> reportData, int startTime,int allTime) {
        tableView.setSleepDayList(reportData,startTime,allTime);
    }

    @Override
    public void updateProgressView(int startTime, int deepTime, int lightTime) {
        deepTv.setText(String.format(Locale.ENGLISH,"%dh%dmin",deepTime/60,deepTime%60));
        lightTv.setText(String.format(Locale.ENGLISH,"%dh%dmin",lightTime/60,lightTime%60));
        allSleepTv.setText(Html.fromHtml(String.format(Locale.ENGLISH,
                "<font color='#000000'><big><big>%d</big></big></font>h<font color='#000000'><big><big>%d</big></big></font>min",
                (lightTime+deepTime)/60,(lightTime+deepTime)%60)));
        bedTv.setText(String.format(Locale.ENGLISH,"%02d:%02d",startTime/60,startTime%60));
        wakeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d",(startTime+deepTime+lightTime)%1440/60,
                (startTime+deepTime+lightTime)%1440%60));
        sleepPb.setCurrentValues(deepTime+lightTime,startTime);
    }

    @Override
    public void updateView(String allTime, String deepTime, String lightTime, String plan) {

    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_REPORT_TIME:{
                iSleepReportPresenter.loadData(((SleepReportActivity)getActivity()).getReportDate());
            }
            break;
        }
    }
}
