package com.szip.healthy.Activity.bp;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.TextView;

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

public class BpFragment extends BaseLazyLoadingFragment implements IBpView, MyHandle {

    private int type;
    private ReportTableView tableView;
    private TextView reportTypeTv,sbpTv,dbpTv,sbpAbnormalTv,dbpAbnormalTv;
    private RecyclerView infoList;
    private ToActivityBroadcast toActivityBroadcast;

    private IBpPresenter iBpPresenter;
    public BpFragment(int type) {
        this.type = type;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.healthy_fragment_bp;
    }

    @Override
    protected void initView(View root) {
        tableView = root.findViewById(R.id.tableView);
        reportTypeTv = root.findViewById(R.id.reportTypeTv);
        sbpTv = root.findViewById(R.id.sbpTv);
        dbpTv = root.findViewById(R.id.dbpTv);
        sbpAbnormalTv = root.findViewById(R.id.sbpAbnormalTv);
        dbpAbnormalTv = root.findViewById(R.id.dbpAbnormalTv);
        infoList = root.findViewById(R.id.infoList);
        toActivityBroadcast = new ToActivityBroadcast();

        root.findViewById(R.id.listLl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReportInfoActivity.class);
                intent.putExtra("type", HealthyConst.BLOOD_PRESSURE);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        if (type == ReportConst.REPORT_DAY)
            iBpPresenter = new BpDayPresenterImpl(getActivity().getApplicationContext(),this);
        else if (type == ReportConst.REPORT_WEEK)
            iBpPresenter = new BpWeekPresenterImpl(getActivity().getApplicationContext(),this);
        else
            iBpPresenter = new BpMonthPresenterImpl(getActivity().getApplicationContext(),this);

    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        iBpPresenter.register(this);
        IntentFilter intentFilter = new IntentFilter(BroadcastConst.UPDATE_REPORT_TIME);
        toActivityBroadcast.registerReceive(this,getActivity().getApplicationContext(),intentFilter);
        iBpPresenter.loadData(((BpReportActivity)getActivity()).getReportDate());
    }

    @Override
    protected void onFragmentPause() {
        super.onFragmentPause();
        iBpPresenter.unRegister();
        toActivityBroadcast.unregister(getActivity().getApplicationContext());
    }

    @Override
    public void updateTable(List<ReportData> reportDataList) {
        if (type == ReportConst.REPORT_DAY){
            tableView.setPressureDayList(reportDataList);
        } else if (type == ReportConst.REPORT_WEEK) {
            tableView.setPressureWeekList(reportDataList);
        } else {
            tableView.setPressureMonthList(reportDataList);
        }
    }

    @Override
    public void updateView(String sbp, String dbp, String sbpAbnormal, String dbpAbnormal) {
        if (type == ReportConst.REPORT_DAY){
            reportTypeTv.setText(getString(R.string.healthy_survey));
        } else if (type == ReportConst.REPORT_WEEK) {
            reportTypeTv.setText(getString(R.string.healthy_week_survey));
        } else {
            reportTypeTv.setText(getString(R.string.healthy_month_survey));
        }
        sbpTv.setText(sbp);
        dbpTv.setText(dbp);
        sbpAbnormalTv.setText(sbpAbnormal);
        dbpAbnormalTv.setText(dbpAbnormal);
    }

    @Override
    public void updateListView(List<ReportInfoData> list) {
        infoList.setVisibility(View.VISIBLE);
        infoList.setLayoutManager(new LinearLayoutManager(getContext()));
        infoList.setHasFixedSize(true);
        infoList.setNestedScrollingEnabled(false);
        ReportInfoAdapter reportInfoAdapter = new ReportInfoAdapter(list);
        infoList.setAdapter(reportInfoAdapter);
    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_REPORT_TIME:{
                iBpPresenter.loadData(((BpReportActivity)getActivity()).getReportDate());
            }
                break;
        }
    }
}
