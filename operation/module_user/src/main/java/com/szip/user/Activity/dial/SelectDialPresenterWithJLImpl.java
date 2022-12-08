package com.szip.user.Activity.dial;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.FileUtil;
import com.szip.user.Adapter.DialAdapter;
import com.szip.user.HttpModel.DialBean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SelectDialPresenterWithJLImpl implements IDialSelectPresenter{
    private Context context;
    private IDialSelectView iDialSelectView;
    private String fileName = "";

    public SelectDialPresenterWithJLImpl(Context context, IDialSelectView iDialSelectView) {
        this.context = context;
        this.iDialSelectView = iDialSelectView;
    }


    @Override
    public void getViewConfig(RecyclerView dialRv, final ArrayList<DialBean.Dial> dialArrayList) {
        dialRv.setLayoutManager(new GridLayoutManager(context, 3));
        DialAdapter dialAdapter = new DialAdapter(dialArrayList,context);
        dialRv.setAdapter(dialAdapter);
        dialRv.setHasFixedSize(true);
        dialRv.setNestedScrollingEnabled(false);

        if (iDialSelectView !=null&&dialArrayList.size()!=0){
            fileName = "watch"+dialArrayList.get(0).getPointerNumber();
            iDialSelectView.setView(dialArrayList.get(0).getPreviewUrl(),
                    dialArrayList.get(0).getPlateBgUrl(),fileName);

        }

        dialAdapter.setOnItemClickListener(position -> {
            fileName = "watch"+dialArrayList.get(position).getPointerNumber();
            if (iDialSelectView !=null){
                iDialSelectView.setDialView(dialArrayList.get(position).getPreviewUrl(),
                        dialArrayList.get(position).getPlateBgUrl(),fileName);

            }
        });
    }

    @Override
    public void startToSendDial() {
        if (iDialSelectView != null)
            iDialSelectView.setDialProgress(100);
        Intent intent = new Intent(BroadcastConst.SEND_JL_DIAL);
        String fileUrl = context.getExternalFilesDir(null).getPath()+"/"+fileName;
        Log.d("data******","file = "+fileUrl);
        intent.putExtra("fileUrl",fileUrl);
        context.sendBroadcast(intent);
    }

    @Override
    public void sendDial(String fileUrl, int address) {

    }

}
