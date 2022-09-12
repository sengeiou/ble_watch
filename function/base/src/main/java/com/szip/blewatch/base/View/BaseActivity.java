package com.szip.blewatch.base.View;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.szip.blewatch.base.Interfere.OnScrollListener;
import com.szip.blewatch.base.R;
import com.szip.blewatch.base.Util.ScreenCapture;

import java.util.Calendar;


/**
 * Created by Administrator on 2019/11/28.
 */

public class BaseActivity extends AppCompatActivity {

    private TextView titleTv,titleBigTv;
    private MyScrollView myScrollView;

    private long delayTime = 0;

    protected synchronized void showToast(String str){
        if (Calendar.getInstance().getTimeInMillis()-delayTime>2000) {
            delayTime = Calendar.getInstance().getTimeInMillis();
            Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
        }

    }


    protected void setTitle(String msg){
        titleTv = findViewById(R.id.titleTv);
        titleBigTv = findViewById(R.id.titleBigTv);
        myScrollView = findViewById(R.id.scroll);

        if (titleBigTv!=null)
            titleBigTv.setText(msg);
        if (titleTv!=null)
            titleTv.setText(msg);

        if (titleBigTv==null&&titleTv!=null)
            titleTv.setVisibility(View.VISIBLE);

        if (myScrollView==null||titleTv==null)
            return;

        myScrollView.setOnScrollListener(listener);
    }

    /**
     * 分享长图
     * */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void shareShowLong(ScrollView view){
        Uri str = ScreenCapture.getScollerBitmap
                (this, view);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, str);
        startActivityForResult(Intent.createChooser(intent,getString(R.string.name)),101);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 适配Android字体（随着系统字体改变而改变）
     * */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    /**
     * 修改状态栏字体颜色
     * */
    protected void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected boolean checkPermission(String permission){
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private OnScrollListener listener = new OnScrollListener() {
        @Override
        public void onScroll(int scrollY) {
            if (titleTv!=null){
                if (scrollY>120)
                    titleTv.setVisibility(View.VISIBLE);
                else
                    titleTv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadMost() {
            loadMost();
        }
    };

    protected void loadMost(){}
}
