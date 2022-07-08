package com.reactnativestudycamera;

import android.os.Build;
import android.util.Log;

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
  public void deleteCaches(String subFolder) {
    if (subFolder != null && !subFolder.trim().isEmpty()) {
      Utils.deleteCaches(getReactApplicationContext(), subFolder);
    } else {
      Utils.deleteAllCaches(getReactApplicationContext());
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @ReactMethod
  public void getCachedFile(String subFolder, int bodyPart, boolean isThumb, Promise promise) {
    try {
      Log.d("CACHE FILE", "Begin get cache file " + bodyPart + " of " + subFolder);
      String imageBase64 = Utils.getCachedImage(getReactApplicationContext(), subFolder, bodyPart, isThumb);
      if (imageBase64 != null) {
        promise.resolve(imageBase64);
        return;
      }
      promise.reject("NOT_FOUND", "File not found");
    } catch (Exception ex) {
      ex.printStackTrace();
      promise.reject("ERROR", ex.getMessage());
    }
  }
}
