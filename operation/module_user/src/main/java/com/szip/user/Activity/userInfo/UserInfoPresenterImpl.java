package com.szip.user.Activity.userInfo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.View.character.OnOptionChangedListener;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.HttpModel.AvatarBean;
import com.szip.user.R;
import com.szip.user.Utils.HttpMessageUtil;
import com.szip.user.View.CharacterPickerWindow;
import com.yalantis.ucrop.UCrop;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;

public class UserInfoPresenterImpl implements IUserInfoPresenter{
    private Context context;
    private Handler handler;
    private IUserInfoView iUserInfoView;

    public UserInfoPresenterImpl(Context context, IUserInfoView iUserInfoView) {
        this.context = context;
        this.iUserInfoView = iUserInfoView;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void getSex(final CharacterPickerWindow window, final UserModel userModel) {
        final List<String> sexList =new ArrayList<>(Arrays.asList(context.getString(R.string.user_female),context.getString(R.string.user_male)));
        window.setTitleTv(context.getString(R.string.user_sex));
        window.getPickerView().setText("","");
        //初始化选项数据
        window.getPickerView().setPicker(sexList);
        //设置默认选中的三级项目
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                window.setCurrentPositions(userModel.sex, 0, 0);
            }
        },100);

        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                if (iUserInfoView!=null)
                    iUserInfoView.setSex(sexList.get(option1),option1);
            }
        });
    }

    @Override
    public void getHeight(final CharacterPickerWindow window, final UserModel userModel) {
        window.setTitleTv(context.getString(R.string.user_height));
        if (userModel.unit==0){
            final ArrayList<String> list = DateUtil.getStature();
            window.getPickerView().setText("cm","");
            //初始化选项数据
            window.getPickerView().setPicker(list);
            //设置默认选中的三级项目
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.setCurrentPositions(userModel.height-50, 0, 0);
                }
            },100);

            //监听确定选择按钮
            window.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    if (iUserInfoView!=null)
                        iUserInfoView.setHeight(list.get(option1)+"cm",Integer.valueOf(list.get(option1)),0);
                }
            });
        }else {
            final ArrayList<String> list = DateUtil.getStatureWithBritish();
            window.getPickerView().setText("in","");
            //初始化选项数据
            window.getPickerView().setPicker(list);
            //设置默认选中的三级项目
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.setCurrentPositions(userModel.heightBritish-20, 0, 0);
                }
            },100);

            //监听确定选择按钮
            window.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    if (iUserInfoView!=null)
                        iUserInfoView.setHeight(list.get(option1)+"in",Integer.valueOf(list.get(option1)),1);
                }
            });
        }
    }

    @Override
    public void getWeight(final CharacterPickerWindow window, final UserModel userModel) {
        window.setTitleTv(context.getString(R.string.user_weight));
        //初始化选项数据
        if (userModel.unit==0){
            window.getPickerView().setText("kg","");
            final ArrayList<String> list = DateUtil.getWeight();
            window.getPickerView().setPicker(list);
            //设置默认选中的三级项目
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.setCurrentPositions(userModel.weight-30, 0, 0);
                }
            },100);


            //监听确定选择按钮
            window.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    if (iUserInfoView!=null)
                        iUserInfoView.setWeight(list.get(option1)+"kg",Integer.valueOf(list.get(option1)),0);
                }
            });
        }else {
            window.getPickerView().setText("lb","");
            final ArrayList<String> list = DateUtil.getWeightWithBritish();
            window.getPickerView().setPicker(list);
            //设置默认选中的三级项目
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.setCurrentPositions(userModel.weightBritish-67, 0, 0);
                }
            },100);

            //监听确定选择按钮
            window.setOnoptionsSelectListener(new OnOptionChangedListener() {
                @Override
                public void onOptionChanged(int option1, int option2, int option3) {
                    if (iUserInfoView!=null)
                        iUserInfoView.setWeight(list.get(option1)+"lb",Integer.valueOf(list.get(option1)),1);
                }
            });
        }
    }

    @Override
    public void getBirthday(final CharacterPickerWindow window, final UserModel userModel) {
        window.setTitleTv(context.getString(R.string.user_birthday));
        final ArrayList<String> list1 = DateUtil.getYearList();
        window.getPickerView().setText(null,null);
        //初始化选项数据
        window.getPickerView().setPickerForDate(list1);
        //设置默认选中的三级项目
        if (userModel.birthday!=null){
            final String [] strs = userModel.birthday.split("-");
            if(strs.length!=3)
                return;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.setCurrentPositions(Integer.valueOf(strs[0])-1930, Integer.valueOf(strs[1])-1, Integer.valueOf(strs[2])-1);
                }
            },100);

        }else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.setCurrentPositions(list1.size()/2, 0, 0);
                }
            },100);

        }

        //监听确定选择按钮
        window.setOnoptionsSelectListener(new OnOptionChangedListener() {
            @Override
            public void onOptionChanged(int option1, int option2, int option3) {
                if (iUserInfoView!=null)
                    iUserInfoView.setBirthday(String.format(Locale.ENGLISH,"%4d-%02d-%02d",(1930+option1),(option2+1),(option3+1)));
            }
        });
    }

    @Override
    public void saveUserInfo(final UserModel userModel) {
            HttpMessageUtil.newInstance().postForSetUserInfo(userModel.userName, userModel.sex + "",
                    userModel.birthday, userModel.height + "", userModel.weight + "",
                    userModel.heightBritish + "", userModel.weightBritish + "",
                    new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (iUserInfoView!=null)
                                iUserInfoView.saveSuccess(false);
                        }

                        @Override
                        public void onResponse(BaseApi response, int id) {
                            if (response.getCode()==200){
                                SaveDataUtil.newInstance().saveUserInfo(userModel);
                                if (iUserInfoView!=null)
                                    iUserInfoView.saveSuccess(true);
                            }
                        }
                    });
    }

    @Override
    public void selectPhoto(final Dialog dialog, int y) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_dialog_choose_photo, null);

        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.user_anim_style);
        final WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = y;
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        TextView btn_album;
        TextView btn_camera;
        TextView btn_cancel;
        btn_album = view.findViewById(R.id.btn_album);
        btn_camera = view.findViewById(R.id.btn_camera);
        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                if (iUserInfoView!=null)
                    iUserInfoView.getPhotoPath(intent,false);
                dialog.cancel();
            }
        });
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoName = context.getExternalFilesDir(null).getPath()+"/camera";
                File file = new File(photoName);
                Uri photoURI = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(context,"com.szip.blewatch.fileprovider", file);
                } else {
                    photoURI = Uri.fromFile(file);
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 下面这句指定调用相机拍照后的照片存储的路径
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (iUserInfoView!=null)
                    iUserInfoView.getPhotoPath(intent,true);
                dialog.cancel();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                // 设置显示位置
                dialog.onWindowAttributesChanged(wl);
                // 设置点击外围解散
                dialog.setCanceledOnTouchOutside(true);
            }
        });
        dialog.show();
    }

    @Override
    public void cropPhoto(Uri uri) {
        try {
            Uri path = uri;
            //临时用一个名字用来保存裁剪后的图片
            String fileName = context.getExternalFilesDir(null).getPath()+"/crop";
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            Uri target = Uri.fromFile(file);
            UCrop.Options options = new UCrop.Options();
            options.setToolbarColor(context.getResources().getColor(R.color.rayblue));
            options.setStatusBarColor(context.getResources().getColor(R.color.rayblue));
            options.setActiveWidgetColor(context.getResources().getColor(R.color.rayblue));
            UCrop uCrop = UCrop.of(path, target)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(200, 200)
                    .withOptions(options);
            if (iUserInfoView!=null){
                iUserInfoView.getCropPhoto(uCrop);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void uploadPhoto(File file) {
        HttpMessageUtil.newInstance().postUploadAvatar(file, new GenericsCallback<AvatarBean>(new JsonGenericsSerializator()) {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(AvatarBean response, int id) {
                if (response.getCode()==200){
                    //上传头像
                    if (iUserInfoView!=null)
                        iUserInfoView.setPhoto(response.getData().getUrl());
                    //裁剪成功之后，删掉之前拍的照片
                    new File(context.getExternalFilesDir(null).getPath()+"/camera").delete();
                    new File(context.getExternalFilesDir(null).getPath()+"/crop").delete();
                }
            }
        });
    }

    @Override
    public void setViewDestory() {
        iUserInfoView = null;
    }
}
