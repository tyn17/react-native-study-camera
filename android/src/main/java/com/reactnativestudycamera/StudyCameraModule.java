package com.reactnativestudycamera;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.List;

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

  @RequiresApi(api = Build.VERSION_CODES.M)
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

  @ReactMethod
  public void deleteCache(String subFolder) {
    if (cameraViewManager.getCameraPreviewView() != null) {
      cameraViewManager.getCameraPreviewView().deleteCache(subFolder);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @ReactMethod
  public void getCacheFiles(String subFolder, Promise promise) {
    if (cameraViewManager.getCameraPreviewView() != null) {
      List<CacheFileModel> list = cameraViewManager.getCameraPreviewView().getCacheFiles(subFolder);
      promise.resolve(CacheFileModel.serialize(list));
    }
    promise.resolve("[]");
  }
}
