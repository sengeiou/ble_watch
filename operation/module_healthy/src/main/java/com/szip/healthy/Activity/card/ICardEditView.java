package com.szip.healthy.Activity.card;

import com.szip.blewatch.base.db.dbModel.HealthyCardData;

import java.util.List;

public interface ICardEditView {
    void updateView(List<HealthyCardData> tagList,List<HealthyCardData> dataList);
}
