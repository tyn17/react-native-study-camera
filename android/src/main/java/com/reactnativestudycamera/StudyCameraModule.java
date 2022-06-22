package com.reactnativestudycamera;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class StudyCameraModule extends ReactContextBaseJavaModule {
  private StudyCameraViewManager cameraViewManager;
  @NonNull
  @Override
  public String getName() {
    return "StudyCameraModule";
  }

  public StudyCameraModule(ReactApplicationContext reactContext, StudyCameraViewManager camManager) {
    super(reactContext);
    cameraViewManager = camManager;
  }

  @Override
  public boolean canOverrideExistingModule() {
    return true;
  }

  @ReactMethod
  public void resumeCamera() {
    if (cameraViewManager.getCameraPreviewView() != null) {
      cameraViewManager.getCameraPreviewView().resume();
    }
  }

  @ReactMethod
  public void pauseCamera() {
    if (cameraViewManager.getCameraPreviewView() != null) {
      cameraViewManager.getCameraPreviewView().pause();
    }
  }

  @ReactMethod
  public void capturePhoto() {
    if (cameraViewManager.getCameraPreviewView() != null) {
      cameraViewManager.getCameraPreviewView().capturePhoto();
    }
  }
}
