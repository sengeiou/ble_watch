package com.szip.user.Activity.diy;

import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;


import com.szip.user.HttpModel.DialBean;

import java.util.ArrayList;

public interface IDiyPresenter {
    void getViewConfig(RecyclerView dialRv, ArrayList<DialBean.Dial> dialArrayList);
    void startToSendBackground();
    void sendBackground(String resultUri, int clock);
    void cropPhoto(Uri uri);
    void setViewDestory();

    void startToSendDial();
    void sendDial(String resultUri, int address);
}
