package com.szip.healthy.Activity.sport;

import android.content.Context;

import com.szip.blewatch.base.Model.ReportInfoData;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.SportData;

import java.util.ArrayList;
import java.util.List;

public class SportListPresenterImpl implements ISportListPresenter{

    private Context mContext;
    private ISportListView iSportListView;

    private ArrayList<SportData> groupList;
    private ArrayList<SportData> childList;

    public SportListPresenterImpl(Context mContext, ISportListView iSportListView) {
        this.mContext = mContext;
        this.iSportListView = iSportListView;
    }


    @Override
    public void initList() {
        groupList = new ArrayList<>();
        childList = new ArrayList<>();
        List<SportData> list = LoadDataUtil.newInstance().getSportList(0);
        if (list.size()==0){
            if (iSportListView!=null)
                iSportListView.updateList(groupList,childList);

            return;
        }
        String oldTime =  DateUtil.getStringDateFromSecond(list.get(0).time,"yyyy/MM");
        SportData sportData = new SportData(DateUtil.getTimeScope(oldTime,"yyyy/MM"));
        groupList.add(sportData);
        childList.add(sportData);
        for (int i = 0;i<list.size();i++){
            SportData data = list.get(i);
            String time = DateUtil.getStringDateFromSecond(data.time,"yyyy/MM");
            if (!time.equals(oldTime)) {
                sportData = new SportData(DateUtil.getTimeScope(time,"yyyy/MM"));
                groupList.add(sportData);
                childList.add(sportData);
                oldTime = time;
            }
            childList.add(data);
        }

        if (iSportListView!=null)
            iSportListView.updateList(groupList,childList);
    }

    @Override
    public void getList(int page) {
        List<SportData> list = LoadDataUtil.newInstance().getSportList(page);
        if (list.size()==0)
            if (iSportListView!=null)
                iSportListView.updateList(groupList,childList);
        String oldTime =  DateUtil.getStringDateFromSecond(groupList.get(groupList.size()-1).time,"yyyy/MM");

        for (int i = 0;i<list.size();i++){
            SportData data = list.get(i);
            String time = DateUtil.getStringDateFromSecond(data.time,"yyyy/MM");
            if (!time.equals(oldTime)) {
                SportData sportData = new SportData(DateUtil.getTimeScope(time,"yyyy/MM"));
                groupList.add(sportData);
                childList.add(sportData);
                oldTime = time;
            }
            childList.add(data);
        }

        if (iSportListView!=null)
            iSportListView.updateList(groupList,childList);
    }
}
