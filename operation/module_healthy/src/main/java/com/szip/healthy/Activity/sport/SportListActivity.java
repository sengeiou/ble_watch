package com.szip.healthy.Activity.sport;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.SportData_Table;
import com.szip.healthy.Adapter.SportListAdapter;
import com.szip.healthy.R;

import java.util.ArrayList;
import java.util.List;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_SPORT_RESULT;

public class SportListActivity extends BaseActivity implements ISportListView {

    private RecyclerView sportList;
    private SportListAdapter sportListAdapter;
    private ArrayList<SportData> groupList;
    private ArrayList<SportData> childList;

    private int page = 0;
    private ISportListPresenter iSportListPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.healthy_activity_sport_list);
        setAndroidNativeLightStatusBar(this,true);
        iSportListPresenter = new SportListPresenterImpl(getApplicationContext(),this);
        initView();
        iSportListPresenter.initList();
    }

    private void initView() {

        List<SportData> list = SQLite.select()
                .from(SportData.class)
                .orderBy(OrderBy.fromString(SportData_Table.time+OrderBy.DESCENDING))
                .queryList();

        Log.d("data******","list size = "+list.size());

        setTitle(getString(R.string.healthy_sport_list));
        sportList = findViewById(R.id.sportList);
        sportList.setLayoutManager(new LinearLayoutManager(this));
        sportList.setHasFixedSize(true);
        sportList.setNestedScrollingEnabled(false);
        sportListAdapter = new SportListAdapter(getApplicationContext());
        sportList.setAdapter(sportListAdapter);
        sportListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!groupList.contains(childList.get(position))){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("sportData",childList.get(position));
                    ARouter.getInstance().build(PATH_ACTIVITY_SPORT_RESULT)
                            .withBundle("bundle",bundle)
                            .navigation();
                }
            }
        });

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void loadMost() {
        super.loadMost();
        if (iSportListPresenter!=null)
            iSportListPresenter.getList(page);
    }

    @Override
    public void updateList(ArrayList<SportData> groupList, ArrayList<SportData> childList) {
        this.groupList = groupList;
        this.childList = childList;
        sportListAdapter.setDataList(groupList,childList);
        page++;
    }
}