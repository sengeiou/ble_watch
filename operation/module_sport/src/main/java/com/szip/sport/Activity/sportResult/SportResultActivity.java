package com.szip.sport.Activity.sportResult;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.szip.blewatch.base.Interfere.OnScrollListener;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.MyScrollView;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.sport.Adapter.SportDataAdapter;
import com.szip.sport.Model.SportResultDataModel;
import com.szip.sport.R;
import com.szip.sport.View.MyRelativeLayout;
import com.szip.sport.View.SportHeartLevelView;
import com.szip.sport.View.SportSpeedView;
import com.szip.sport.View.SportTableView;

import java.util.List;
import java.util.Locale;

import static com.szip.blewatch.base.Const.RouterPathConst.PATH_ACTIVITY_SPORT_RESULT;


@Route(path = PATH_ACTIVITY_SPORT_RESULT)
public class SportResultActivity extends BaseActivity implements ISportResultView, OnMapReadyCallback {

    private SportData sportData;
    private TextView typeTv,distanceTv,timeTv;
    private RecyclerView sportDataRv;
    private SportDataAdapter sportDataAdapter;
    private ISportResultPresenter iSportResultPresenter;
    private View backgroundView;
    private MyScrollView sportScrollView;
    private MyRelativeLayout mapRl;
    private MapView mapView;
    private com.amap.api.maps.MapView gaodeView;
    private IMapUtil iMapUtil;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.sport_activity_sport_result);
        bundle = savedInstanceState;
        setAndroidNativeLightStatusBar(this,true);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        sportData = (SportData) bundle.getSerializable("sportData");
        ARouter.getInstance().inject(this);
        iSportResultPresenter = new SportResultPresenterImpl(getApplicationContext(),this);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.shareIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermission() {
        /**
         * 获取权限·
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }else {
                shareShowLong((ScrollView)findViewById(R.id.sportScrollView));
            }
        }else {
            shareShowLong((ScrollView) findViewById(R.id.sportScrollView));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (iMapUtil!=null)
            iMapUtil.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iMapUtil!=null)
            iMapUtil.onDestroy();
    }

    private void initData() {
        if (sportData == null)
            return;
        iSportResultPresenter.initSportData(sportData);
    }

    private void initView() {
        typeTv = findViewById(R.id.typeTv);
        distanceTv = findViewById(R.id.distanceTv);
        timeTv = findViewById(R.id.timeTv);
        sportDataRv = findViewById(R.id.sportDataRv);
        sportDataRv.setHasFixedSize(true);
        sportDataRv.setNestedScrollingEnabled(false);
        backgroundView = findViewById(R.id.backgroundView);
        sportScrollView = findViewById(R.id.sportScrollView);
    }

    @Override
    public void updateSportView(String distance, String time, String sportType, List<SportResultDataModel> list) {
        typeTv.setText(sportType);
        distanceTv.setText(Html.fromHtml(distance));
        timeTv.setText(time);
        sportDataAdapter = new SportDataAdapter(list);
        sportDataRv.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        sportDataRv.setAdapter(sportDataAdapter);
    }

    @Override
    public void updateHeartView(int[] heartData, int[] heartLevel) {
        findViewById(R.id.heartLl).setVisibility(View.VISIBLE);
        SportTableView heartTable = findViewById(R.id.heartTable);
        heartTable.setDataList(heartData,sportData.time);
        SportHeartLevelView sportHeartLevelView = findViewById(R.id.sportHeartView);
        sportHeartLevelView.setDatas(heartLevel);
        TextView level1TimeTv,level2TimeTv,level3TimeTv,level4TimeTv,level5TimeTv;
        level1TimeTv = findViewById(R.id.level1TimeTv);
        level2TimeTv = findViewById(R.id.level2TimeTv);
        level3TimeTv = findViewById(R.id.level3TimeTv);
        level4TimeTv = findViewById(R.id.level4TimeTv);
        level5TimeTv = findViewById(R.id.level5TimeTv);

        level1TimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",heartLevel[0]/3600,
                heartLevel[0]%3600/60,heartLevel[0]%3600%60));
        level2TimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",heartLevel[1]/3600,
                heartLevel[1]%3600/60,heartLevel[1]%3600%60));
        level3TimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",heartLevel[2]/3600,
                heartLevel[2]%3600/60,heartLevel[2]%3600%60));
        level4TimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",heartLevel[3]/3600,
                heartLevel[3]%3600/60,heartLevel[3]%3600%60));
        level5TimeTv.setText(String.format(Locale.ENGLISH,"%02d:%02d:%02d",heartLevel[4]/3600,
                heartLevel[4]%3600/60,heartLevel[4]%3600%60));

    }

    @Override
    public void updateSpeedView(String[] speed) {
        findViewById(R.id.speedLl).setVisibility(View.VISIBLE);
        SportSpeedView sportSpeedView = findViewById(R.id.sportSpeed);
        sportSpeedView.addData(speed);
    }

    @Override
    public void updateStrideView(int[] stride) {
        findViewById(R.id.strideLl).setVisibility(View.VISIBLE);
        SportTableView strideTable = findViewById(R.id.strideTable);
        strideTable.setDataList(stride,sportData.time);
    }

    @Override
    public void updateAltitudeView(int[] altitude) {
        findViewById(R.id.altitudeLl).setVisibility(View.VISIBLE);
        SportTableView altitudeTable = findViewById(R.id.altitudeTable);
        altitudeTable.setDataList(altitude,sportData.time);
    }

    @Override
    public void updateLocation() {
        findViewById(R.id.topView).setVisibility(View.VISIBLE);
        backgroundView = findViewById(R.id.backgroundView);
        backgroundView.getBackground().setAlpha(0);
        sportScrollView = findViewById(R.id.sportScrollView);
        mapRl = findViewById(R.id.mapRl);
        mapView = findViewById(R.id.googleMap);
        gaodeView = findViewById(R.id.gaodeMap);
        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
            gaodeView.setVisibility(View.VISIBLE);
            iMapUtil = new MapUtilGaodeImp(gaodeView);
            iMapUtil.onCreate(bundle);
            makeLine();
        } else {
            mapView.setVisibility(View.VISIBLE);
            mapView.onCreate(bundle);
            mapView.onResume();
            try {
                MapsInitializer.initialize(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (ConnectionResult.SUCCESS != errorCode) {
                GooglePlayServicesUtil.getErrorDialog(errorCode, SportResultActivity.this, 0).show();
            } else {
                mapView.getMapAsync(this);
            }
        }
        sportScrollView.setOnScrollListener(listener);
        mapRl.setScrollView(sportScrollView,gaodeView,mapView);
    }

    private void makeLine(){
        String[] lats = sportData.latArray.split(",");
        String[] lngs = sportData.lngArray.split(",");
        iMapUtil.setLatlng(lats,lngs);
        iMapUtil.moveCamera();
        iMapUtil.addMarker();
        iMapUtil.addPolyline();
    }

    private OnScrollListener listener = new OnScrollListener() {
        @Override
        public void onScroll(int scrollY) {
            int alpha = 0;
            alpha = scrollY/(findViewById(R.id.topLl).getTop()/255);
            if (alpha>255)
                alpha = 255;
            backgroundView.getBackground().setAlpha(alpha);
        }

        @Override
        public void onLoadMost() {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        iMapUtil = new MapUtilGoogleImp(googleMap);
        makeLine();
    }
}