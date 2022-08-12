package com.szip.user.Activity.diy;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Broadcast.MyHandle;
import com.szip.blewatch.base.Broadcast.ToActivityBroadcast;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Const.SendFileConst;
import com.szip.blewatch.base.Util.FileUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.user.Activity.dial.DialSelectActivity;
import com.szip.user.HttpModel.DialBean;
import com.szip.user.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DIYActivity extends BaseActivity implements IDiyView, MyHandle {

    private RecyclerView clockRv;
    private CircularImageView backgroundIv;
    private ImageView clockIv,diyIv;
    private boolean isSendPic = false;

    private int clock = -1;
    private Uri resultUri;
    private String pictureUrl;
    private ArrayList<DialBean.Dial> dialArrayList = new ArrayList<>();
    private IDiyPresenter iDiyPresenter;

    private ToActivityBroadcast toActivityBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.user_activity_diy);
        setAndroidNativeLightStatusBar(this,true);
        iDiyPresenter = new DiyPresenterImpl06(getApplicationContext(),this);
        Bundle bundle = getIntent().getBundleExtra("data");
        dialArrayList = (ArrayList<DialBean.Dial>) bundle.getSerializable("list");
        initView();
        initEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (toActivityBroadcast == null)
            toActivityBroadcast = new ToActivityBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConst.UPDATE_DIAL_STATE);
        intentFilter.addAction(BroadcastConst.UPDATE_BACKGROUND_STATE);
        intentFilter.addAction(BroadcastConst.UPDATE_DOWNLOAD_STATE);
        toActivityBroadcast.registerReceive(this,getApplicationContext(),intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toActivityBroadcast.unregister(getApplicationContext());
    }

    private void initView() {
        setTitle(getString(R.string.user_dial_diy));
        clockRv = findViewById(R.id.clockRv);
        clockIv = findViewById(R.id.clockIv);
        diyIv = findViewById(R.id.diyIv);
        iDiyPresenter.getViewConfig(clockRv,dialArrayList);
    }

    private void initEvent() {
        findViewById(R.id.clockIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        findViewById(R.id.backIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.saveTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 保存成功
                if (resultUri==null){
                    showToast(getString(R.string.user_no_background));
                    return;
                }
                if (clock==-1){
                    showToast(getString(R.string.user_no_clock));
                    return;
                }

                if (pictureUrl==null)
                    return;

                if (!ProgressHudModel.newInstance().isShow()){
                    ProgressHudModel.newInstance().show(DIYActivity.this,getString(R.string.loading),false);
                    Intent intent = new Intent(BroadcastConst.DOWNLOAD_FILE);
                    intent.putExtra("fileUrl",pictureUrl);
                    sendBroadcast(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UCrop.RESULT_ERROR) {
            showToast(getString(R.string.user_crop_fail));
            return;
        }
        switch (requestCode) {
            case 1: {
                if (data == null || data.getData() == null)
                    return;
                Log.d("DATA******", "URI1 = " + data.getData());
                FileUtil.getInstance().writeUriSdcardFile(data.getData());
                File file = new File(getExternalFilesDir(null).getPath() + "/camera");
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(this, "com.szip.blewatch.fileprovider", file);
                    }
                    iDiyPresenter.cropPhoto(uri);
                }

            }
            break;
            case UCrop.REQUEST_CROP: {
                if (data != null) {
                    if (!MathUtil.newInstance().toJpgFile(getApplicationContext())){
                        showToast(getString(R.string.user_crop_small));
                        return;
                    }

                    resultUri = UCrop.getOutput(data);
                    try {
                        backgroundIv.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (findViewById(R.id.bottomRl).getVisibility() == View.GONE) {
                        findViewById(R.id.bottomRl).setVisibility(View.VISIBLE);
                    }
                }
            }
            break;
        }
    }

    @Override
    public void setView(boolean isCircle) {
        if (isCircle){
            diyIv.setImageResource(R.mipmap.my_watch_diy_circle);
            backgroundIv = findViewById(R.id.backgroundIv_c);
        }else {
            diyIv.setImageResource(R.mipmap.my_watch_diy_square);
            backgroundIv = findViewById(R.id.backgroundIv_r06);
        }
    }

    @Override
    public void setDialView(String dial, String pictureUrl, int clock) {
        Glide.with(this).load(dial).into(clockIv);
        this.pictureUrl = pictureUrl;
        this.clock = clock;
    }

    @Override
    public void getCropPhoto(UCrop uCrop) {
        uCrop.start(this);
    }

    @Override
    public void setDialProgress(int num, String str) {
        ProgressHudModel.newInstance().showWithPie(DIYActivity.this,str,num);
    }

    @Override
    public void onReceive(Intent intent) {
        switch (intent.getAction()) {
            case BroadcastConst.UPDATE_DOWNLOAD_STATE: {
                if (intent.getBooleanExtra("state", true)) {
                    iDiyPresenter.startToSendBackground();
                } else {
                    showToast(getString(R.string.http_error));
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
                }else if (command == SendFileConst.ERROR){
                    ProgressHudModel.newInstance().diss();
                    showToast(getString(R.string.user_send_fail));
                }else if (command == SendFileConst.START_SEND){
                    ProgressHudModel.newInstance().diss();
                    iDiyPresenter.sendBackground(resultUri.getPath(),clock);
                }else if (command == SendFileConst.SEND_BIN){
                    ProgressHudModel.newInstance().diss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iDiyPresenter.startToSendDial();
                        }
                    },100);
                }
            }
            break;
            case BroadcastConst.UPDATE_DIAL_STATE:{
                if (iDiyPresenter == null)
                    return;
                int command = intent.getIntExtra("command", SendFileConst.ERROR);
                if (command == SendFileConst.START_SEND){
                    int address = intent.getIntExtra("address",0);
                    String fileNames[] = pictureUrl.split("/");
                    iDiyPresenter.sendDial(fileNames[fileNames.length-1],address);
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
                }
            }
            break;
        }
    }
}