package com.zaaach.citypicker.adapter;

import com.szip.blewatch.base.Model.ContactModel;
import com.zaaach.citypicker.model.City;

import java.util.ArrayList;
import java.util.List;

public interface OnContactPickListener {
    void onPick(ArrayList<ContactModel> contactModelList);
}
