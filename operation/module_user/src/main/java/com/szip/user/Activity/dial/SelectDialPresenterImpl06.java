package com.szip.user.Activity.dial;

import android.content.Context;
import android.content.Intent;

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

public class SelectDialPresenterImpl06 implements IDialSelectPresenter{

    private Context context;
    private IDialSelectView iDialSelectView;
    private int clock;
    private String fileName;

    public SelectDialPresenterImpl06(Context context, IDialSelectView iDialSelectView) {
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
            fileName = "PICTURE"+dialArrayList.get(0).getPointerNumber();
            clock = dialArrayList.get(0).getPointerNumber();
            iDialSelectView.setView(dialArrayList.get(0).getPreviewUrl(),
                    dialArrayList.get(0).getPlateBgUrl(),fileName);
        }


        dialAdapter.setOnItemClickListener(new DialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (iDialSelectView !=null){
                    clock = dialArrayList.get(position).getPointerNumber();
                    fileName = "PICTURE"+dialArrayList.get(position).getPointerNumber();
                    iDialSelectView.setDialView(dialArrayList.get(position).getPreviewUrl(),
                            dialArrayList.get(position).getPlateBgUrl(),fileName);
                }
            }
        });
    }

    @Override
    public void startToSendDial() {
        Intent intent = new Intent(BroadcastConst.SEND_BLE_BACKGROUND);
        intent.putExtra("clock",clock);
        context.sendBroadcast(intent);
    }

    @Override
    public void sendDial(String pictureUrl, int address) {
        if (pictureUrl != null) {
            pictureUrl = context.getExternalFilesDir(null).getPath()+"/"+fileName;
            final int PAGENUM = 200;//分包长度
            InputStream in = null;
            try {
                in = new FileInputStream(pictureUrl);
                byte[] datas =  FileUtil.getInstance().toByteArray(in);
                in.close();
                int num = datas.length / PAGENUM;
                num = datas.length % PAGENUM == 0 ? num : num + 1;
                if (iDialSelectView != null)
                    iDialSelectView.setDialProgress(num);
                Intent intent = new Intent(BroadcastConst.SEND_BLE_BACKGROUND);
                intent.putExtra("command",1);
                intent.putExtra("pictureUrl",pictureUrl);
                context.sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
