package com.szip.healthy.Activity.reportInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;

import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.healthy.Adapter.ReportListAdapter;
import com.szip.healthy.R;

import java.util.ArrayList;

public class ReportInfoActivity extends BaseActivity implements IReportInfoView{

    private int type;
    private RecyclerView reportInfoView;
    private ReportListAdapter reportInfoAdapter = new ReportListAdapter();

    private IReportInfoPresenter iReportInfoPresenter;
    private int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.healthy_activity_report_info);
        setAndroidNativeLightStatusBar(this,true);
        type = getIntent().getIntExtra("type",0);
        if (type == HealthyConst.BLOOD_PRESSURE){
            iReportInfoPresenter = new BloodPressureInfoPresenterImpl(this,getApplicationContext());
        }else if (type == HealthyConst.TEMPERATURE){
            iReportInfoPresenter = new TempInfoPresenterImpl(this,getApplicationContext());
        }else {
            iReportInfoPresenter = new BloodOxygenInfoPresenterImpl(this,getApplicationContext());
        }
        initView();
        iReportInfoPresenter.initList();
    }

    private void initView() {
        setTitle(getString(R.string.healthy_spo_measure_info));
        reportInfoView = findViewById(R.id.reportInfoView);
        reportInfoView.setLayoutManager(new LinearLayoutManager(this));
        reportInfoView.setHasFixedSize(true);
        reportInfoView.setNestedScrollingEnabled(false);
        reportInfoView.setAdapter(reportInfoAdapter);
    }

    @Override
    protected void loadMost() {
        super.loadMost();
        iReportInfoPresenter.loadData(page);
    }

    @Override
    public void updateList(ArrayList<ReportInfoData> groupList, ArrayList<ReportInfoData> childList) {
        reportInfoAdapter.setList(childList,groupList);
        page++;
    }
}