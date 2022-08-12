package com.szip.user.Activity.dial;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.SendFileConst;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.user.Activity.diy.DIYActivity;
import com.szip.user.HttpModel.DialBean;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.util.ArrayList;

import okhttp3.Call;

public class DialSelectActivity extends BaseActivity implements IDialSelectView, MyHandle {

    private ImageView dialIv,changeIv;
    private RecyclerView dialRv;

    private String pictureUrl;
    private boolean isSendPic = false;
    private boolean isCircle = false;
    private SportWatchAppFunctionConfigDTO data;
    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    private IDialSelectPresenter iSelectDialPresenter;

    private ToActivityBroadcast toActivityBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.user_activity_dial_select);
        setAndroidNativeLightStatusBar(this,true);
        data = LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (data==null)
            return;
        isCircle = data.screenType==0;
        getDialList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (toActivityBroadcast == null)
            toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_DIAL_STATE);
        intentFilter.addAction(BroadcastConst.UPDATE_DOWNLOAD_STATE);
        intentFilter.addAction(BroadcastConst.UPDATE_BACKGROUND_STATE);
        toActivityBroadcast.registerReceive(this,getApplicationContext(),intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toActivityBroadcast.unregister(getApplicationContext());
    }

    private void getDialList() {
        HttpMessageUtil.newInstance().getDialList(data.watchPlateGroupId+"",
                new GenericsCallback<DialBean>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        initList(false);
                    }
                    @Override
                    public void onResponse(DialBean response, int id) {
                        if (response.getCode() == 200){
                            dialArrayList = response.getData().getList();
                            initList(true);
                        }else {
                            initList(false);
                        }
                    }
                });

    }

    private void initView() {
        setTitle(getString(R.string.user_dial_market));
        RecyclerView dialRv = findViewById(R.id.dialRv);
        changeIv = findViewById(R.id.changeIv);
        if (isCircle){
            changeIv.setImageResource(R.mipmap.my_watchstyle_circle);
            dialIv = findViewById(R.id.dialIv_c);
        }else {
            changeIv.setImageResource(R.mipmap.my_watchstyle_square);
            dialIv = findViewById(R.id.dialIv_r06);
        }
        if(iSelectDialPresenter!=null)
            iSelectDialPresenter.getViewConfig(dialRv,dialArrayList);
    }

    public void initList(boolean initSuccess) {
        if (!initSuccess)
            showToast(getString(R.string.http_error));

        findViewById(R.id.rightTv).setVisibility(View.VISIBLE);
        if (dialArrayList!=null&&dialArrayList.size()!=0){
            if (isFileDial())
                iSelectDialPresenter = new SelectDialPresenterWithFileImpl(getApplicationContext(),this);
            else
                iSelectDialPresenter = new SelectDialPresenterImpl06(getApplicationContext(),this);
        }else {
            iSelectDialPresenter = new SelectDialPresenterImpl06(getApplicationContext(),this);
        }

        initView();
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.diyLl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<DialBean.Dial> list = new ArrayList<>();
                for (DialBean.Dial dial:dialArrayList){
                    if (dial.getPointerImg()!=null)
                        list.add(dial);
                }
                Intent intent = new Intent(DialSelectActivity.this, DIYActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("list",list);
                intent.putExtra("data",bundle);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.rightTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ProgressHudModel.newInstance().isShow()&&pictureUrl!=null){
                    ProgressHudModel.newInstance().show(DialSelectActivity.this,getString(R.string.loading),false);
                    Intent intent = new Intent(BroadcastConst.DOWNLOAD_FILE);
                    intent.putExtra("fileUrl",pictureUrl);
                    sendBroadcast(intent);
                }
            }
        });
    }

    @Override
    public void setView(String id, String pictureId) {
        this.pictureUrl = pictureId;
        data.dialImg = id;
        Glide.with(this).load(id).into(dialIv);
    }

    @Override
    public void setDialView(String dialId, String pictureId) {
        this.pictureUrl = pictureId;
        data.dialImg = dialId;
        Glide.with(this).load(dialId).into(dialIv);
    }

    @Override
    public void setDialProgress(int max) {
        ProgressHudModel.newInstance().diss();
        ProgressHudModel.newInstance().showWithPie(this,getString(R.string.user_send_dial),max);
    }

    private boolean isFileDial(){
        if (dialArrayList.get(0).getPlateBgUrl().indexOf(".bin")<0)
            return false;
        else
            return true;
    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()){
            case BroadcastConst.UPDATE_DOWNLOAD_STATE:{
                if (intent.getBooleanExtra("state",true)){
                    iSelectDialPresenter.startToSendDial();
                }else {
                    showToast(getString(R.string.http_error));
                }
            }
            break;
            case BroadcastConst.UPDATE_DIAL_STATE:{
                if (iSelectDialPresenter == null)
                    return;
                int command = intent.getIntExtra("command",255);
                if (command == SendFileConst.START_SEND){
                    int address = intent.getIntExtra("address",0);
                    String fileNames[] = pictureUrl.split("/");
                    iSelectDialPresenter.sendDial(fileNames[fileNames.length-1],address);
                }else if (command == SendFileConst.PROGRESS){
                    ProgressHudModel.newInstance().setProgress();
                }else if (command == SendFileConst.ERROR){
                    isSendPic = false;
                    ProgressHudModel.newInstance().diss();
                    showToast(getString(R.string.user_send_fail));
                }else if (command == SendFileConst.FINISH){
                    isSendPic = false;
                    ProgressHudModel.newInstance().diss();
                    showToast(getString(R.string.user_send_success));
                    data.update();
                }
            }
            break;
            case BroadcastConst.UPDATE_BACKGROUND_STATE:{
                int command = intent.getIntExtra("command",255);
                if (command == SendFileConst.PROGRESS){
                    ProgressHudModel.newInstance().setProgress();
                }else if (command == SendFileConst.FINISH){
                    ProgressHudModel.newInstance().diss();
                    showToast(getString(R.string.user_send_success));
                    data.update();
                }else if (command == SendFileConst.ERROR){
                    ProgressHudModel.newInstance().diss();
                    showToast(getString(R.string.user_send_fail));
                }else if (command == SendFileConst.START_SEND){
                    ProgressHudModel.newInstance().diss();
                    String fileNames[] = pictureUrl.split("/");
                    iSelectDialPresenter.sendDial(fileNames[fileNames.length-1],0);
                }
            }
            break;
        }
    }
}