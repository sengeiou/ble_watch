package com.szip.healthy.Activity.temp;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Const.ReportConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.View.BaseLazyLoadingFragment;
import com.szip.healthy.Activity.heart.HeartReportActivity;
import com.szip.healthy.Activity.reportInfo.ReportInfoActivity;
import com.szip.healthy.Adapter.ReportInfoAdapter;
import com.szip.healthy.Model.ReportData;
import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.healthy.R;
import com.szip.healthy.View.ReportTableView;

import java.util.List;

public class TempFragment extends BaseLazyLoadingFragment implements ITempView, MyHandle {

    private int type;
    private ReportTableView tableView;
    private TextView reportTypeTv,averageTv,maxTv,minTv,smallTv,measureTv;
    private RecyclerView infoList;
    private ITempPresenter iTempPresenter;


    private ToActivityBroadcast toActivityBroadcast;

    public TempFragment(int type) {
        this.type = type;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.healthy_fragment_oxygen_temp;
    }

    @Override
    protected void initView(View root) {
        ((TextView)root.findViewById(R.id.abnormalTv)).setText(getString(R.string.healthy_temp_abnormal));
        ((TextView)root.findViewById(R.id.averageTv)).setText(getString(R.string.healthy_temp_average));
        ((TextView)root.findViewById(R.id.maxTv)).setText(getString(R.string.healthy_temp_max));
        ((TextView)root.findViewById(R.id.minTv)).setText(getString(R.string.healthy_temp_min));
        tableView = root.findViewById(R.id.tableView);
        reportTypeTv = root.findViewById(R.id.reportTypeTv);
        averageTv = root.findViewById(R.id.averageDataTv);
        maxTv = root.findViewById(R.id.maxDataTv);
        minTv = root.findViewById(R.id.minDataTv);
        smallTv = root.findViewById(R.id.smallTv);
        measureTv = root.findViewById(R.id.measureTv);
        infoList = root.findViewById(R.id.infoList);
        toActivityBroadcast = new ToActivityBroadcast();
        root.findViewById(R.id.listLl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ReportInfoActivity.class);
                intent.putExtra("type",HealthyConst.TEMPERATURE);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        if (type == ReportConst.REPORT_DAY)
            iTempPresenter = new TempDayPresenterImpl(getActivity().getApplicationContext(),this);
        else if (type == ReportConst.REPORT_WEEK)
            iTempPresenter = new TempWeekPresenterImpl(getActivity().getApplicationContext(),this);
        else
            iTempPresenter = new TempMonthPresenterImpl(getActivity().getApplicationContext(),this);

    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        iTempPresenter.register(this);
        IntentFilter intentFilter = new IntentFilter(BroadcastConst.UPDATE_REPORT_TIME);
        toActivityBroadcast.registerReceive(this,getActivity().getApplicationContext(),intentFilter);
        iTempPresenter.loadData(((TempReportActivity)getActivity()).getReportDate());
    }

    @Override
    protected void onFragmentPause() {
        super.onFragmentPause();
        iTempPresenter.unRegister();
        toActivityBroadcast.unregister(getActivity().getApplicationContext());
    }


    @Override
    public void updateTable(List<ReportData> reportData) {
        if (type == ReportConst.REPORT_DAY)
            tableView.setTempDayList(reportData);
        else if (type == ReportConst.REPORT_WEEK)
            tableView.setTempWeekList(reportData);
        else
            tableView.setTempMonthList(reportData);
    }

    @Override
    public void updateView(String average, String max, String min, String abnormal) {
        if (type == ReportConst.REPORT_DAY){
            reportTypeTv.setText(getString(R.string.healthy_survey));
        } else if (type == ReportConst.REPORT_WEEK) {
            reportTypeTv.setText(getString(R.string.healthy_week_survey));
        } else {
            reportTypeTv.setText(getString(R.string.healthy_month_survey));
        }
        averageTv.setText(average);
        maxTv.setText(max);
        minTv.setText(min);
        smallTv.setText(abnormal);
    }

    @Override
    public void updateList(List<ReportInfoData> reportInfoData) {
        infoList.setVisibility(View.VISIBLE);
        infoList.setLayoutManager(new LinearLayoutManager(getContext()));
        infoList.setHasFixedSize(true);
        infoList.setNestedScrollingEnabled(false);
        ReportInfoAdapter reportInfoAdapter = new ReportInfoAdapter(reportInfoData);
        infoList.setAdapter(reportInfoAdapter);
    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_REPORT_TIME:{
                iTempPresenter.loadData(((TempReportActivity)getActivity()).getReportDate());
            }
            break;
        }
    }

}
