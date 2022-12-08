package com.szip.blewatch.base.View;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.szip.blewatch.base.Interfere.OnProgressTimeout;
import com.szip.blewatch.base.R;


/**
 * Created by Administrator on 2018/12/22.
 */

public class ProgressHudModel {
    private static ProgressHudModel progressHudModel;
    private KProgressHUD progressHUD;
    private Context mContext;
    private OnProgressTimeout onProgressTimeout;
    private int progressOld = -1,progressNew = -1;

    private ProgressHudModel(){

    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (progressHUD!=null){
                if (progressOld==-1&&progressNew==-1){
                    progressHUD.dismiss();
                    progressHUD = null;
                   Toast.makeText(mContext,mContext.getString(R.string.http_error),Toast.LENGTH_SHORT).show();
                    if (onProgressTimeout!=null)
                        onProgressTimeout.onTimeout();
                }else {
                    if (progressOld==progressNew){
                        progressHUD.dismiss();
                        progressHUD = null;
                        progressOld = -1;
                        progressNew = -1;
                        Toast.makeText(mContext,mContext.getString(R.string.http_error),Toast.LENGTH_SHORT).show();
                    }else {
                        progressOld = progressNew;
                        handler.postDelayed(run,30*1000);
                    }
                }
            }
        }
    };

    public static ProgressHudModel newInstance(){// 单例模式，双重锁
        if( progressHudModel == null ){
            synchronized (ProgressHudModel.class){
                if( progressHudModel == null ){
                    progressHudModel = new ProgressHudModel();
                }
            }
        }
        return progressHudModel ;
    }

    public boolean isShow(){
        if (progressHUD!=null)
            return true;
        else
            return false;
    }

    public void show(final Context mContext, String title){
        progressHUD  = KProgressHUD.create(mContext)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(title)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        progressHUD.show();
        this.onProgressTimeout = null;
        this.mContext = mContext;
    }

    public void show(final Context mContext, String title, boolean cancelAble){
        progressHUD  = KProgressHUD.create(mContext)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(title)
                .setCancellable(cancelAble)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        progressHUD.show();
        this.onProgressTimeout = null;
        this.mContext = mContext;
        handler.postDelayed(run,30*1000);
    }

    public void show(final Context mContext, String title,boolean cancelAble,OnProgressTimeout onProgressTimeout){
        progressHUD  = KProgressHUD.create(mContext)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(title)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(cancelAble);
        progressHUD.show();
        this.onProgressTimeout = onProgressTimeout;
        this.mContext = mContext;
        handler.postDelayed(run,30*1000);
    }

    public void showWithPie(final Context mContext, String title,int max){
        progressOld = 0;
        progressNew = 0;
        progressHUD  = KProgressHUD.create(mContext)
                .setMaxProgress(max)
                .setStyle(KProgressHUD.Style.PIE_DETERMINATE)
                .setLabel(title)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        progressHUD.show();
        this.mContext = mContext;
        handler.postDelayed(run,30*1000);
    }

    public void setProgress(){
        if (progressHUD!=null){
            progressNew++;
            progressHUD.setProgress(progressNew);
        }

    }

    public void setProgress(int progress){
        if (progressHUD!=null){
            progressNew++;
            progressHUD.setProgress(progress);
        }

    }

    public void setLabel(String label){
        if (progressHUD!=null)
            progressHUD.setLabel(label);
    }

    public void diss(){
        if (progressHUD!=null){
            progressHUD.dismiss();
            progressHUD = null;
            handler.removeCallbacks(run);
            progressOld = -1;
            progressNew = -1;
        }
    }
}
