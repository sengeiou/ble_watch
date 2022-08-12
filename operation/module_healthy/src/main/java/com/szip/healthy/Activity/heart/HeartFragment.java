package com.szip.healthy.Activity.heart;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.ReportConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.View.BaseLazyLoadingFragment;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;
import com.szip.healthy.View.HeartLevelView;
import com.szip.healthy.View.ReportTableView;

import java.util.List;
import java.util.Locale;

public class HeartFragment extends BaseLazyLoadingFragment implements IHeartView, MyHandle {

    private int type;
    private IHeartPresenter iHeartPresenter;
    private ReportTableView tableView;
    private HeartLevelView heartView;
    private TextView reportTypeTv,level1TimeTv,level2TimeTv,level3TimeTv,level4TimeTv,level5TimeTv,level6TimeTv,averageTv,maxTv,minTv;
    private LinearLayout heartLl;

    private ToActivityBroadcast toActivityBroadcast;

    public HeartFragment(int type) {
        this.type = type;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.healthy_fragment_heart;
    }

    @Override
    protected void initView(View root) {
        tableView = root.findViewById(R.id.tableView);
        heartView = root.findViewById(R.id.heartView);
        heartLl = root.findViewById(R.id.heartLl);
        reportTypeTv = root.findViewById(R.id.reportTypeTv);
        level1TimeTv = root.findViewById(R.id.level1TimeTv);
        level2TimeTv = root.findViewById(R.id.level2TimeTv);
        level3TimeTv = root.findViewById(R.id.level3TimeTv);
        level4TimeTv = root.findViewById(R.id.level4TimeTv);
        level5TimeTv = root.findViewById(R.id.level5TimeTv);
        level6TimeTv = root.findViewById(R.id.level6TimeTv);
        averageTv = root.findViewById(R.id.averageDataTv);
        maxTv = root.findViewById(R.id.maxDataTv);
        minTv = root.findViewById(R.id.minDataTv);
        toActivityBroadcast = new ToActivityBroadcast();
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        if (type == ReportConst.REPORT_DAY)
            iHeartPresenter = new HeartDayPresenterImpl(getActivity().getApplicationContext(),this);
        else if (type == ReportConst.REPORT_WEEK)
            iHeartPresenter = new HeartWeekPresenterImpl(getActivity().getApplicationContext(),this);
        else
            iHeartPresenter = new HeartMonthPresenterImpl(getActivity().getApplicationContext(),this);

    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        iHeartPresenter.register(this);
        IntentFilter intentFilter = new IntentFilter(BroadcastConst.UPDATE_REPORT_TIME);
        toActivityBroadcast.registerReceive(this,getActivity().getApplicationContext(),intentFilter);
        iHeartPresenter.loadData(((HeartReportActivity)getActivity()).getReportDate());
    }

    @Override
    protected void onFragmentPause() {
        super.onFragmentPause();
        iHeartPresenter.unRegister();
        toActivityBroadcast.unregister(getActivity().getApplicationContext());
    }

    @Override
    public void updateTable(List<ReportData> reportDataList, int max) {
        if (type == ReportConst.REPORT_DAY)
            tableView.setHeartDayList(reportDataList,max);
        else if (type == ReportConst.REPORT_WEEK)
            tableView.setHeartWeekList(reportDataList,max);
        else
            tableView.setHeartMonthList(reportDataList,max);
    }

    @Override
    public void updateView(String average, String max, String min) {
        if (type == ReportConst.REPORT_DAY)
            reportTypeTv.setText(getString(R.string.healthy_survey));
        else if (type == ReportConst.REPORT_WEEK)
            reportTypeTv.setText(getString(R.string.healthy_week_survey));
        else
            reportTypeTv.setText(getString(R.string.healthy_month_survey));
        averageTv.setText(average);
        maxTv.setText(max);
        minTv.setText(min);
    }

    @Override
    public void updateHeartLevel(int[] date,int size) {
        heartLl.setVisibility(View.VISIBLE);
        heartView.setDatas(date);
        level1TimeTv.setText(String.format(Locale.ENGLISH,"%.1f%%",date[0]/(float)size*100));
        level2TimeTv.setText(String.format(Locale.ENGLISH,"%.1f%%",date[1]/(float)size*100));
        level3TimeTv.setText(String.format(Locale.ENGLISH,"%.1f%%",date[2]/(float)size*100));
        level4TimeTv.setText(String.format(Locale.ENGLISH,"%.1f%%",date[3]/(float)size*100));
        level5TimeTv.setText(String.format(Locale.ENGLISH,"%.1f%%",date[4]/(float)size*100));
        level6TimeTv.setText(String.format(Locale.ENGLISH,"%.1f%%",date[5]/(float)size*100));
    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_REPORT_TIME:{
                iHeartPresenter.loadData(((HeartReportActivity)getActivity()).getReportDate());
            }
            break;
        }
    }
}
