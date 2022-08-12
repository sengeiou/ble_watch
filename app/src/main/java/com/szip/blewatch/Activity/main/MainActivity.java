package com.szip.blewatch.Activity.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.loader.content.CursorLoader;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.szip.blewatch.R;
import com.szip.blewatch.View.HostTabView;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Service.BleService;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.LoadDataUtil;

import java.util.ArrayList;

/**
 * @author ddnosh
 * @website http://blog.csdn.net/ddnosh
 */
public class MainActivity extends BaseActivity implements IMainView,MyHandle{

    private ViewPager2 pager;
    private TabLayout tabLayout;

//    private ArrayList<HostTabView> mTableItemList;
//    private RelativeLayout layout;
//    private FragmentTabHost fragmentTabHost;
    private IMainPrisenter iMainPrisenter;

    private long firstTime = 0;


    private Binder binder;

    private ToActivityBroadcast toActivityBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        setContentView(R.layout.activty_main);
        setAndroidNativeLightStatusBar(this,true);
        iMainPrisenter = new MainPresenterImpl(this);
        iMainPrisenter.checkBluetoochState();
        initView();
        initPager();
        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED||
                    checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_CALL_LOG
                                ,Manifest.permission.READ_CONTACTS},
                        100);
            }
        }

        toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UNBIND_SERVICE);
        intentFilter.addAction(BroadcastConst.BIND_SERVICE);
        intentFilter.addAction(BroadcastConst.UPDATE_WEATHER);
        toActivityBroadcast.registerReceive(this,this,intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String mac = LoadDataUtil.newInstance().getMacAddress(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (null!=mac){
            if (binder==null){//后台还没启动
                LogUtil.getInstance().logd("data******","启动后台mac = "+mac);
                bindService(new Intent(this, BleService.class),connection,BIND_AUTO_CREATE);
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (Binder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.getInstance().logd("data******","onServiceDisconnected = "+name.getClassName());
            binder = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
//        layout = findViewById(R.id.layout);
//        fragmentTabHost = findViewById(android.R.id.tabhost);
        pager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);
        MainAdapter mainAdapter = new MainAdapter(this);
        pager.setAdapter(mainAdapter);
    }


    @Override
    public void initHostFinish(ArrayList<HostTabView> hostTabViews) {

    }

    /**
     * 初始化选项卡视图
     * */
    private void initPager() {
        //实例化FragmentTabHost对象
        iMainPrisenter.initPager(pager,tabLayout);
    }

    /**
     * 双击退出
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondtime = System.currentTimeMillis();
            if (secondtime - firstTime > 3000) {
                Toast.makeText(this, getString(R.string.touchAgain),
                        Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onReceive(Intent intent) {
        if (intent.getAction().equals(BroadcastConst.UNBIND_SERVICE)){
            if (binder!=null){
                binder = null;
                unbindService(connection);
                stopService(new Intent(MainActivity.this,BleService.class));
            }
        }else if (intent.getAction().equals(BroadcastConst.BIND_SERVICE)){
            bindService(new Intent(this, BleService.class),connection,BIND_AUTO_CREATE);
        }else if (intent.getAction().equals(BroadcastConst.UPDATE_WEATHER)){//按测试要求，登录成功之后同步一下天气
            iMainPrisenter.initWeather((LocationManager) getSystemService(LOCATION_SERVICE));
        }
    }
}
