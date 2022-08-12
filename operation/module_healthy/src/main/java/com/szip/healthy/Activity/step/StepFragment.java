package com.szip.healthy.Activity.step;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.TextView;

import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.ReportConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.View.BaseLazyLoadingFragment;
import com.szip.healthy.Activity.heart.HeartReportActivity;
import com.szip.healthy.Model.ReportData;
import com.szip.healthy.R;
import com.szip.healthy.View.RectProgressBar;
import com.szip.healthy.View.ReportTableView;

import java.util.List;

public class StepFragment extends BaseLazyLoadingFragment implements IStepReportView, MyHandle {
    private ReportTableView reportTableView;
    private IStepReportPresenter iStepReportPresenter;
    private TextView stepTv,caloriesTv,distanceTv,goalTv,reportTypeTv,stepTypeTv,calorieTypeTv,distanceTypeTv,goalTypeTv;
    private RectProgressBar rectProgressBar;
    private ToActivityBroadcast toActivityBroadcast;

    private int type;

    public StepFragment(int type) {
        this.type = type;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.healthy_fragment_step;
    }

    @Override
    protected void initView(View root) {
        reportTableView = root.findViewById(R.id.tableView);
        stepTv = root.findViewById(R.id.stepTv);
        caloriesTv = root.findViewById(R.id.caloriesTv);
        distanceTv = root.findViewById(R.id.distanceTv);
        goalTv = root.findViewById(R.id.goalTv);
        reportTypeTv = root.findViewById(R.id.reportTypeTv);
        stepTypeTv = root.findViewById(R.id.stepTypeTv);
        calorieTypeTv = root.findViewById(R.id.calorieTypeTv);
        distanceTypeTv = root.findViewById(R.id.distanceTypeTv);
        goalTypeTv = root.findViewById(R.id.goalTypeTv);
        rectProgressBar = root.findViewById(R.id.rectProgressBar);
        toActivityBroadcast = new ToActivityBroadcast();
    }

    @Override
    protected void onFragmentFirstVisible() {
        super.onFragmentFirstVisible();
        if (type == ReportConst.REPORT_DAY) {
            iStepReportPresenter = new StepDayPresenterImpl(getActivity().getApplicationContext(),this);
        }else if (type == ReportConst.REPORT_WEEK){
            iStepReportPresenter = new StepWeekPresenterImpl(getActivity().getApplicationContext(),this);
        }else {
            iStepReportPresenter = new StepMonthPresenterImpl(getActivity().getApplicationContext(),this);
        }


    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        iStepReportPresenter.register(this);
        IntentFilter intentFilter = new IntentFilter(BroadcastConst.UPDATE_REPORT_TIME);
        toActivityBroadcast.registerReceive(this,getActivity().getApplicationContext(),intentFilter);
        iStepReportPresenter.loadData(((StepReportActivity)getActivity()).getReportDate());
    }

    @Override
    protected void onFragmentPause() {
        super.onFragmentPause();
        iStepReportPresenter.unregister();
        toActivityBroadcast.unregister(getActivity().getApplicationContext());
    }

    @Override
    public void updateTableView(List<ReportData> dataList, int maxValue) {
        if (type == ReportConst.REPORT_DAY) {
            reportTableView.setStepDayList(dataList,maxValue);
        }else if (type == ReportConst.REPORT_WEEK){
            reportTableView.setStepWeekList(dataList,maxValue);
        }else {
            reportTableView.setStepMonthList(dataList,maxValue);
        }


    }

    @Override
    public void updateStepView(String step, String calorie, String distance, String plan) {
        if (type == ReportConst.REPORT_DAY) {
            reportTypeTv.setText(getString(R.string.healthy_survey));
            stepTypeTv.setText(getString(R.string.healthy_all_step));
            calorieTypeTv.setText(getString(R.string.healthy_all_calorie));
            distanceTypeTv.setText(getString(R.string.healthy_all_distance));
            goalTypeTv.setText(getString(R.string.healthy_plan));
            rectProgressBar.setRatio(Float.valueOf(plan.replaceAll("%","")));
        }else if (type == ReportConst.REPORT_WEEK){
            reportTypeTv.setText(getString(R.string.healthy_week_survey));
            stepTypeTv.setText(getString(R.string.healthy_day_step));
            calorieTypeTv.setText(getString(R.string.healthy_day_calorie));
            distanceTypeTv.setText(getString(R.string.healthy_day_distance));
            goalTypeTv.setText(getString(R.string.healthy_radio));
            rectProgressBar.setVisibility(View.GONE);
        }else {
            reportTypeTv.setText(getString(R.string.healthy_month_survey));
            stepTypeTv.setText(getString(R.string.healthy_day_step));
            calorieTypeTv.setText(getString(R.string.healthy_day_calorie));
            distanceTypeTv.setText(getString(R.string.healthy_day_distance));
            goalTypeTv.setText(getString(R.string.healthy_radio));
            rectProgressBar.setVisibility(View.GONE);
        }
        stepTv.setText(step);
        caloriesTv.setText(calorie);
        distanceTv.setText(distance);
        goalTv.setText(plan);
    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_REPORT_TIME:{
                iStepReportPresenter.loadData(((StepReportActivity)getActivity()).getReportDate());
            }
            break;
        }
    }
}
