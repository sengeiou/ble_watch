package com.szip.healthy.Activity.sleep;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.TextView;

import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.View.BaseLazyLoadingFragment;
import com.szip.healthy.Activity.heart.HeartReportActivity;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;
import com.szip.healthy.View.ReportTableView;

import java.util.List;

public class SleepFragment extends BaseLazyLoadingFragment implements ISleepReportView, MyHandle {
    private ISleepReportPresenter iSleepReportPresenter;
    private TextView deepTv,lightTv,sleepTv,goalTv,reportTypeTv;
    private ReportTableView tableView;

    private int type;

    private ToActivityBroadcast toActivityBroadcast;

    public SleepFragment(int type) {
        this.type = type;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.healthy_fragment_sleep;
    }

    @Override
    protected void initView(View root) {
        deepTv = root.findViewById(R.id.deepTv);
        lightTv = root.findViewById(R.id.lightTv);
        sleepTv = root.findViewById(R.id.sleepTv);
        goalTv = root.findViewById(R.id.goalTv);
        reportTypeTv = root.findViewById(R.id.reportTypeTv);
        tableView = root.findViewById(R.id.tableView);
        toActivityBroadcast = new ToActivityBroadcast();
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        if (type==1)
            iSleepReportPresenter = new SleepWeekPresenterImpl(getActivity().getApplicationContext(),this);
        else
            iSleepReportPresenter = new SleepMonthPresenterImpl(getActivity().getApplicationContext(),this);

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
    public void updateTableView(List<ReportData> reportData, int startTime, int allTime) {
        if (type == 1)
            tableView.setSleepWeekList(reportData,startTime);
        else
            tableView.setSleepMonthList(reportData,startTime);
    }

    @Override
    public void updateProgressView(int startTime, int deepTime, int lightTime) {

    }

    @Override
    public void updateView(String allTime, String deepTime, String lightTime, String plan) {
        if (type == 1)
            reportTypeTv.setText(getString(R.string.healthy_week_survey));
        else
            reportTypeTv.setText(getString(R.string.healthy_month_survey));
        sleepTv.setText(allTime);
        deepTv.setText(deepTime);
        lightTv.setText(lightTime);
        goalTv.setText(plan);
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
