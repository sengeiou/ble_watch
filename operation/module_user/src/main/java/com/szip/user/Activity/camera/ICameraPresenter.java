package com.szip.user.Activity.camera;

import android.widget.FrameLayout;

import java.io.IOException;

public interface ICameraPresenter {
    void initCamera(FrameLayout layout);
    void registerSensor();
    void unRegisterSensor();
    void removeCamera();
    void changeCamera() throws IOException;
    void takePicture();
}
