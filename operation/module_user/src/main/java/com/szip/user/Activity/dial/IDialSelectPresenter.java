package com.szip.user.Activity.dial;

import androidx.recyclerview.widget.RecyclerView;

import com.szip.user.HttpModel.DialBean;

import java.util.ArrayList;

public interface IDialSelectPresenter {
    void getViewConfig(RecyclerView dialRv, ArrayList<DialBean.Dial> dialArrayList);
    void startToSendDial();
    void sendDial(String resultUri,int address);
}
