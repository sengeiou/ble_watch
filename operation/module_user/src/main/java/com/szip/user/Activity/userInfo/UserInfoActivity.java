package com.szip.user.Activity.userInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.FileUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.View.BaseActivity;
import com.szip.blewatch.base.View.CircularImageView;
import com.szip.blewatch.base.View.MyAlerDialog;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.R;
import com.szip.user.View.CharacterPickerWindow;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener ,IUserInfoView{

    private CircularImageView headIv;
    private TextView nameTv, genderTv, birthdayTv, heightTv, weightTv;

    private UserModel userModel;

    private final int IMAGE_CAPTURE = 0;
    private final int IMAGE_MEDIA = 1;

    /**
     * 数据选择框
     */
    private CharacterPickerWindow window;

    private IUserInfoPresenter iUserInfoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_activity_user_info);
        setAndroidNativeLightStatusBar(this, true);
        iUserInfoPresenter = new UserInfoPresenterImpl(getApplicationContext(), this);
        initView();
        initData();
        initEvent();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        iUserInfoPresenter.setViewDestory();
    }


    private void initView() {
        setTitle(getString(R.string.user_info));
        window = new CharacterPickerWindow(UserInfoActivity.this);
        headIv = findViewById(R.id.headIv);
        nameTv = findViewById(R.id.nameTv);
        genderTv = findViewById(R.id.genderTv);
        birthdayTv = findViewById(R.id.birthdayTv);
        heightTv = findViewById(R.id.heightTv);
        weightTv = findViewById(R.id.weightTv);
    }

    /**
     * 初始化事件监听
     */
    private void initEvent() {
        findViewById(R.id.headRl).setOnClickListener(this);
        findViewById(R.id.nameRl).setOnClickListener(this);
        findViewById(R.id.genderRl).setOnClickListener(this);
        findViewById(R.id.birthdayRl).setOnClickListener(this);
        findViewById(R.id.weightRl).setOnClickListener(this);
        findViewById(R.id.heightRl).setOnClickListener(this);
        findViewById(R.id.backIv).setOnClickListener(this);
        findViewById(R.id.saveTv).setOnClickListener(this);
    }

    private void initData() {
        UserModel userModel = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(getApplicationContext()));
        if (userModel == null)
            return;
        this.userModel = userModel;
        nameTv.setText(userModel.userName);
        genderTv.setText(userModel.sex == 1 ? getString(R.string.user_male) : getString(R.string.user_female));
        Glide.with(this).load(userModel.avatar)
                .fallback(R.mipmap.my_head_58)
                .error(R.mipmap.my_head_58)
                .into(headIv);
        if (userModel.unit == 0) {
            heightTv.setText(userModel.height + "cm");
            weightTv.setText(userModel.weight + "kg");
        } else {
            heightTv.setText(userModel.heightBritish + "in");
            weightTv.setText(userModel.weightBritish + "lb");
        }
        birthdayTv.setText(userModel.birthday);
    }

    /**
     * 点击事件监听
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backIv) {
            finish();
        } else if (id == R.id.headRl) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            100);
                } else {
                    iUserInfoPresenter.selectPhoto(new Dialog(UserInfoActivity.this, R.style.user_transparentFrameWindowStyle),
                            getWindowManager().getDefaultDisplay().getHeight());

                }
            } else {
                iUserInfoPresenter.selectPhoto(new Dialog(UserInfoActivity.this, R.style.user_transparentFrameWindowStyle),
                        getWindowManager().getDefaultDisplay().getHeight());
            }
        } else if (id == R.id.nameRl) {
            MyAlerDialog.getSingle().showAlerDialogWithEdit(getString(R.string.user_name), userModel.userName,
                    getString(R.string.user_enter_name), null, null, false,
                    new MyAlerDialog.AlerDialogEditOnclickListener() {
                        @Override
                        public void onDialogEditTouch(String edit1) {
                            if (edit1.length() <= 12) {
                                nameTv.setText(edit1);
                                userModel.userName = edit1;
                            } else {
                                showToast(getString(R.string.user_name_long));
                            }

                        }
                    }, UserInfoActivity.this);
        } else if (id == R.id.genderRl) {
            iUserInfoPresenter.getSex(window, userModel);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        } else if (id == R.id.heightRl) {
            iUserInfoPresenter.getHeight(window, userModel);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        } else if (id == R.id.weightRl) {
            iUserInfoPresenter.getWeight(window, userModel);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        } else if (id == R.id.birthdayRl) {
            iUserInfoPresenter.getBirthday(window, userModel);
            window.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        } else if (id == R.id.saveTv) {
            ProgressHudModel.newInstance().show(UserInfoActivity.this, getString(R.string.waiting),false);
            iUserInfoPresenter.saveUserInfo(userModel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            int code = grantResults[0];
            if (code == PackageManager.PERMISSION_GRANTED){
                iUserInfoPresenter.selectPhoto(new Dialog(UserInfoActivity.this, R.style.user_transparentFrameWindowStyle),
                        getWindowManager().getDefaultDisplay().getHeight());
            }else {
                showToast(getString(R.string.user_permission_error_camera));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UCrop.RESULT_ERROR) {
            showToast(getString(R.string.user_crop_pic_failed));
            return;
        }
        switch (requestCode) {
            case IMAGE_CAPTURE: {// 相机
                if (data == null || data.getData() == null)
                    return;
                File file = new File(getExternalFilesDir(null).getPath() + "/camera");
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(this, "com.szip.blewatch.fileprovider", file);
                    }
                    iUserInfoPresenter.cropPhoto(uri);
                }
            }
            break;
            case IMAGE_MEDIA: {
                if (data == null || data.getData() == null)
                    return;
                if (data != null)
                    FileUtil.getInstance().writeUriSdcardFile(data.getData());
                File file = new File(getExternalFilesDir(null).getPath() + "/camera");
                if (file.exists()) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        uri = Uri.fromFile(file);
                    } else {
                        uri = FileProvider.getUriForFile(this, "com.szip.blewatch.fileprovider", file);
                    }
                    iUserInfoPresenter.cropPhoto(uri);
                }
            }
            break;
            case UCrop.REQUEST_CROP: {
                LogUtil.getInstance().logd("data******","crop success");
                if (data != null) {
                    iUserInfoPresenter.uploadPhoto(new File(getExternalFilesDir(null).getPath() + "/crop"));
                }
            }
            break;
        }
    }

    @Override
    public void setSex(String sexStr, int sex) {
        genderTv.setText(sexStr);
        userModel.sex = sex;
    }

    @Override
    public void setHeight(String heightStr, int height, int unit) {
        heightTv.setText(heightStr);
        if (unit == 0)
            userModel.height = height;
        else
            userModel.heightBritish = height;
    }

    @Override
    public void setWeight(String weightStr, int weight, int unit) {
        weightTv.setText(weightStr);
        if (unit == 0)
            userModel.weight = weight;
        else
            userModel.weightBritish = weight;
    }

    @Override
    public void setBirthday(String birthday) {
        birthdayTv.setText(birthday);
        userModel.birthday = birthday;
    }

    @Override
    public void saveSuccess(boolean isSuccess) {
        ProgressHudModel.newInstance().diss();
        if (isSuccess){
            showToast(getString(R.string.save_success));
            Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
            intent.putExtra("command","setInfo");
            sendBroadcast(intent);
            finish();
        }else {
            showToast(getString(R.string.http_error));
        }
    }

    @Override
    public void getPhotoPath(Intent intent, boolean isCamera) {
        if (isCamera){
            startActivityForResult(intent, IMAGE_CAPTURE);
        }else {
            startActivityForResult(intent, IMAGE_MEDIA);
        }
    }

    @Override
    public void getCropPhoto(UCrop uCrop) {
        LogUtil.getInstance().logd("data******","start crop");
        uCrop.start(this);
    }

    @Override
    public void setPhoto(String pictureUrl) {
        Glide.with(this).load(pictureUrl).into(headIv);
        if (userModel!=null){
            userModel.avatar = pictureUrl;
            userModel.update();
        }
    }
}