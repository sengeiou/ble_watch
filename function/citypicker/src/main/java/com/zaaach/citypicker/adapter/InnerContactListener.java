package com.zaaach.citypicker.adapter;

import com.szip.blewatch.base.Model.ContactModel;
import com.zaaach.citypicker.model.City;

public interface InnerContactListener {
    void dismiss(int position, ContactModel data,boolean check);
}
