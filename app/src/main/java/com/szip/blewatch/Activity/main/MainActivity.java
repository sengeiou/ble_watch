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

import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.tabs.TabLayout;
import com.szip.blewatch.HttpModel.FirmwareBean;
import com.szip.blewatch.HttpModel.UserInfoBean;
import com.szip.blewatch.R;
import com.szip.blewatch.Utils.HttpMessageUtil;
import com.szip.blewatch.View.HostTabView;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Model.FirmwareModel;
import com.szip.blewatch.base.Service.BleService;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.ArrayList;

import okhttp3.Call;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_ABOUT;
import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_SPORT_RESULT;

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
         * ??????????????
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

//        if (MathUtil.newInstance().getToken(getApplicationContext())!=null){
//            HttpMessageUtil.newInstance().getFirmware(LoadDataUtil.newInstance().getConfigId(MathUtil.newInstance().getUserId(getApplicationContext())),
//                    new GenericsCallback<FirmwareBean>(new JsonGenericsSerializator()) {
//                        @Override
//                        public void onError(Call call, Exception e, int id) {
//
//                        }
//
//                        @Override
//                        public void onResponse(FirmwareBean response, int id) {
//                            if(response.getCode()==200&&response.getData()!=null){
//                                MyAlerDialog.getSingle().showAlerDialog("??????????????????", "?????????????????????????????????????????????????????????",
//                                        "???", "???", false, new MyAlerDialog.AlerDialogOnclickListener() {
//                                            @Override
//                                            public void onDialogTouch(boolean flag) {
//                                                if (flag){
//                                                    Bundle bundle = new Bundle();
//                                                    bundle.putSerializable("firmware",response.getData());
//                                                    ARouter.getInstance().build(PATH_ACTIVITY_ABOUT)
//                                                            .withBundle("bundle",bundle)
//                                                            .navigation();
//                                                }
//                                            }
//                                        },MainActivity.this);
//                            }
//
//                        }
//                    });
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String mac = LoadDataUtil.newInstance().getMacAddress(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (null!=mac){
            if (binder==null){//??????????????????
                LogUtil.getInstance().logd("data******","????????????mac = "+mac);
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
     * ????????????????????????
     * */
    private void initPager() {
        //?????????FragmentTabHost??????
        iMainPrisenter.initPager(pager,tabLayout);
    }

    /**
     * ????????????
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
        }else if (intent.getAction().equals(BroadcastConst.UPDATE_WEATHER)){//??????????????????????????????????????????????????????
            iMainPrisenter.initWeather((LocationManager) getSystemService(LOCATION_SERVICE));
        }
    }
}
