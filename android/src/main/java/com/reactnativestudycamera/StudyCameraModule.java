package com.reactnativestudycamera;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class StudyCameraModule extends ReactContextBaseJavaModule {
  private CameraPreviewView cameraPreviewView;
  @NonNull
  @Override
  public String getName() {
    return "StudyCameraModule";
  }

  public StudyCameraModule(ReactApplicationContext reactContext, StudyCameraViewManager camManager) {
    super(reactContext);
    if (camManager != null) {
      cameraPreviewView = camManager.getCameraPreviewView();
    }
  }

  @Override
  public boolean canOverrideExistingModule() {
    return true;
  }

  @ReactMethod
  public void resumeCamera() {
    System.out.println("CALL Resume Camera");
    //cameraPreviewView.resume();
  }

  @ReactMethod
  public void pauseCamera() {
    System.out.println("CALL Pause Camera");
    //cameraPreviewView.pause();
  }
}
