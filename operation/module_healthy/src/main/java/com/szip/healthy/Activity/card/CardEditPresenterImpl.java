package com.szip.healthy.Activity.card;

import android.content.Context;

import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.HealthyCardData;

import java.util.ArrayList;
import java.util.List;

public class CardEditPresenterImpl implements ICardEditPresenter{

    private Context mContext;
    private ICardEditView iCardEditView;

    public CardEditPresenterImpl(Context mContext, ICardEditView iCardEditView) {
        this.mContext = mContext;
        this.iCardEditView = iCardEditView;
    }

    @Override
    public void initList() {
        List<HealthyCardData> tagList = new ArrayList<>();
        List<HealthyCardData> dataList = new ArrayList<>();
        HealthyCardData trueData = new HealthyCardData(-1,true,-1);
        tagList.add(trueData);
        dataList.add(trueData);
        dataList.addAll(LoadDataUtil.newInstance().getHealthyCard(true));
        HealthyCardData falseData = new HealthyCardData(-1,false,-1);
        tagList.add(falseData);
        dataList.add(falseData);
        dataList.addAll(LoadDataUtil.newInstance().getHealthyCard(false));
        if (iCardEditView!=null)
            iCardEditView.updateView(tagList,dataList);
    }
}
