package com.szip.user.Activity.diy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.FileUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.Adapter.DIYAdapter;
import com.szip.user.HttpModel.DialBean;
import com.szip.user.R;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DiyPresenterImpl06 implements IDiyPresenter{

    private Handler handler;
    private Context context;
    private IDiyView iDiyView;

    private int clock;

    public DiyPresenterImpl06(Context context, IDiyView iDiyView) {
        this.context = context;
        this.iDiyView = iDiyView;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void getViewConfig(RecyclerView dialRv, final ArrayList<DialBean.Dial> dialArrayList) {
        dialRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        SportWatchAppFunctionConfigDTO data = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(context));
        boolean isCircle = false;
        if (data!=null)
            isCircle = data.screenType==0;
        DIYAdapter diyAdapter = new DIYAdapter(dialArrayList,context);
        dialRv.setAdapter(diyAdapter);
        dialRv.setHasFixedSize(true);
        dialRv.setNestedScrollingEnabled(false);

        if (iDiyView!=null)
            iDiyView.setView(isCircle);


        diyAdapter.setOnItemClickListener(new DIYAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (iDiyView!=null){
                    iDiyView.setDialView(dialArrayList.get(position).getPointerImg(),dialArrayList.get(position).getPlateBgUrl(),
                            dialArrayList.get(position).getPointerNumber());
                    clock = dialArrayList.get(position).getPointerNumber();
                }
            }
        });
    }

    @Override
    public void startToSendBackground() {
        Intent intent = new Intent(BroadcastConst.SEND_BLE_BACKGROUND);
        intent.putExtra("clock",clock);
        context.sendBroadcast(intent);
    }

    @Override
    public void sendBackground(String pictureUrl, int clock) {
        final int PAGENUM = 128;//分包长度
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(pictureUrl));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final byte[] datas = baos.toByteArray();
        int num = datas.length/PAGENUM;
        num = datas.length%PAGENUM==0?num:num+1;
        if (iDiyView!=null)
            iDiyView.setDialProgress(num,context.getString(R.string.user_send_background));

        Intent intent = new Intent(BroadcastConst.SEND_BLE_BACKGROUND);
        intent.putExtra("command",1);
        intent.putExtra("pictureUrl",pictureUrl);
        context.sendBroadcast(intent);
    }

    @Override
    public void cropPhoto(Uri uri) {
        try {
            String[] spaceType = MathUtil.newInstance().getFaceType(context);
            Uri path = uri;
            //临时用一个名字用来保存裁剪后的图片
            String fileName = context.getExternalFilesDir(null).getPath()+"/crop";
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            Uri target = Uri.fromFile(file);
            UCrop.Options options = new UCrop.Options();
            options.setToolbarColor(Color.BLACK);
            options.setStatusBarColor(Color.BLACK);
            options.setActiveWidgetColor(Color.BLACK);
            options.setCompressionQuality(80);
            UCrop uCrop = UCrop.of(path, target)
                    .withAspectRatio(Float.valueOf(spaceType[0])/Float.valueOf(spaceType[1]), 1f)
                    .withMaxResultSize(Integer.valueOf(spaceType[0]), Integer.valueOf(spaceType[1]))
                    .withOptions(options);
            if (iDiyView!=null){
                iDiyView.getCropPhoto(uCrop);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void setViewDestory() {
        iDiyView = null;
    }

    @Override
    public void startToSendDial() {
        Intent intent = new Intent(BroadcastConst.SEND_BLE_FILE);
        intent.putExtra("command",6);
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
            if (iDiyView != null)
                iDiyView.setDialProgress(num,context.getString(R.string.user_send_dial));
            in.close();
            Intent intent  = new Intent(BroadcastConst.SEND_BLE_FILE);
            intent.putExtra("command",7);
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
