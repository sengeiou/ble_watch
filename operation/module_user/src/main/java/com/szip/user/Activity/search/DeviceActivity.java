package com.szip.user.Activity.search;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.szip.blewatch.base.Interfere.OnItemClickListener;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.Adapter.ProductAdapter;
import com.szip.user.R;
import com.szip.user.HttpModel.DeviceConfigBean;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.ArrayList;

import okhttp3.Call;

public class DeviceActivity extends BaseActivity {


    private RecyclerView deviceList;
    private ProductAdapter productAdapter;
    private ArrayList<SportWatchAppFunctionConfigDTO> dtoArrayList;
    private DialogFragment pairFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_device);
        setAndroidNativeLightStatusBar(this,true);
        checkPermission();
        initView();
        initData();
    }

    private void initData() {
        HttpMessageUtil.newInstance().getDeviceConfig(new GenericsCallback<DeviceConfigBean>(new JsonGenericsSerializator()) {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(DeviceConfigBean response, int id) {
                if(response.getCode()==200){
                    dtoArrayList = response.getData();
                    productAdapter.setDataList(dtoArrayList);
                }
            }
        });
    }

    private void initView() {
        setTitle(getString(R.string.user_add_device));
        deviceList = findViewById(R.id.deviceList);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setHasFixedSize(true);
        deviceList.setNestedScrollingEnabled(false);
        productAdapter = new ProductAdapter(getApplicationContext());
        deviceList.setAdapter(productAdapter);

        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        productAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                final Fragment prev = fragmentManager.findFragmentByTag("PAIR");
                if (prev != null){
                    ft.remove(prev).commit();
                    ft = fragmentManager.beginTransaction();
                }
                ft.addToBackStack(null);
                pairFragment = new PairFragment(dtoArrayList.get(position));
                pairFragment.show(ft, "PAIR");
            }
        });
    }

    private void checkPermission() {

        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueAdapter.isEnabled()) {
            Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(bleIntent);
        }

        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        100);
            }
        }
    }
}