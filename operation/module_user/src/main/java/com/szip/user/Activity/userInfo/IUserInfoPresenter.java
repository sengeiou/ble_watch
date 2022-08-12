package com.szip.user.Activity.userInfo;

import android.app.Dialog;
import android.net.Uri;

import com.szip.blewatch.base.db.dbModel.UserModel;
import com.szip.user.View.CharacterPickerWindow;

import java.io.File;

public interface IUserInfoPresenter {
    void getSex(CharacterPickerWindow window, UserModel userModel);
    void getHeight(CharacterPickerWindow window, UserModel userModel);
    void getWeight(CharacterPickerWindow window, UserModel userModel);
    void getBirthday(CharacterPickerWindow window, UserModel userModel);
    void saveUserInfo(UserModel userModel);
    void selectPhoto(Dialog dialog, int y);
    void cropPhoto(Uri uri);
    void uploadPhoto(File file);
    void setViewDestory();
}
