package com.szip.user.Activity.dial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.FileUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.user.Adapter.DialAdapter;
import com.szip.user.HttpModel.DialBean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SelectDialPresenterWithFileImpl implements IDialSelectPresenter{
    private Context context;
    private IDialSelectView iDialSelectView;
    private int clock;

    public SelectDialPresenterWithFileImpl(Context context, IDialSelectView iDialSelectView) {
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
            iDialSelectView.setView(dialArrayList.get(0).getPreviewUrl(),
                    dialArrayList.get(0).getPlateBgUrl());
            clock = dialArrayList.get(0).getPointerNumber();
        }

        dialAdapter.setOnItemClickListener(new DialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (iDialSelectView !=null){
                    iDialSelectView.setDialView(dialArrayList.get(position).getPreviewUrl(),
                            dialArrayList.get(position).getPlateBgUrl());
                    clock = dialArrayList.get(position).getPointerNumber();
                }
            }
        });
    }

    @Override
    public void startToSendDial() {
        Intent intent = new Intent(BroadcastConst.SEND_BLE_FILE);
        intent.putExtra("command",3);
        intent.putExtra("clock",clock);
        context.sendBroadcast(intent);
    }

    @Override
    public void sendDial(String fileUrl, int address) {
        fileUrl = context.getExternalFilesDir(null).getPath()+"/"+fileUrl;
        InputStream in;
        try {
            in = new FileInputStream(fileUrl);
            byte[] datas =  FileUtil.getInstance().toByteArray(in);
            int num = datas.length/175/100;
            num = datas.length/175%100 == 0 ? num : num + 1;
            if (iDialSelectView != null)
                iDialSelectView.setDialProgress(num);
            in.close();
            Intent intent  = new Intent(BroadcastConst.SEND_BLE_FILE);
            intent.putExtra("command",4);
            intent.putExtra("fileUrl",fileUrl);
            intent.putExtra("index",address);
            intent.putExtra("page",0);
            context.sendBroadcast(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
