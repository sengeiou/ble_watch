package com.szip.healthy.Activity.card;

import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.szip.blewatch.base.Const.HealthyConst;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.HealthyCardData;
import com.szip.healthy.Adapter.CardEditAdapter;
import com.szip.healthy.R;
import com.szip.healthy.View.DragListView;

import java.util.ArrayList;
import java.util.List;


public class CardEditActivity extends BaseActivity implements ICardEditView{

    private ICardEditPresenter iCardEditPresenter;
    private DragListView dragListView;
    private CardEditAdapter cardEditAdapter;
    private List<HealthyCardData> tagList,dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.healthy_activity_card_edit);
        setAndroidNativeLightStatusBar(this,true);
        iCardEditPresenter = new CardEditPresenterImpl(getApplicationContext(),this);
        initView();
        iCardEditPresenter.initList();
    }

    private void initView() {
        setTitle(getString(R.string.healthy_edit_card));
        dragListView = findViewById(R.id.dragListView);
        dragListView.setiDragListChange(new IDragListChange() {
            @Override
            public void onChange() {
                List<HealthyCardData> list = cardEditAdapter.getList();
                List<HealthyCardData> sqlList = new ArrayList<>();
                int i = 0;
                boolean isOpen = false;
                for (HealthyCardData data:list){
                    if (tagList.get(0).equals(data)){//先遍历有显示的
                        isOpen = true;
                    }else if (tagList.get(1).equals(data)){//再遍历隐藏的
                        isOpen = false;
                    }else {
                        data.state = isOpen;
                        data.sort = i;
                        i++;
                        sqlList.add(data);
                    }
                }
                SaveDataUtil.newInstance().updateHealthyConfigData(sqlList);
                if (cardEditAdapter.getPosition(tagList.get(1))==dataList.size()-1){
                    findViewById(R.id.hiddenLl).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.hiddenLl).setVisibility(View.GONE);
                }

            }
        });
    }

    @Override
    public void updateView(List<HealthyCardData> tagList, List<HealthyCardData> dataList) {
        this.tagList = tagList;
        this.dataList = dataList;
        cardEditAdapter = new CardEditAdapter(getApplicationContext(),dataList,tagList);
        dragListView.setAdapter(cardEditAdapter);
        if (cardEditAdapter.getPosition(tagList.get(1))==dataList.size()-1){
            findViewById(R.id.hiddenLl).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.hiddenLl).setVisibility(View.GONE);
        }
    }
}