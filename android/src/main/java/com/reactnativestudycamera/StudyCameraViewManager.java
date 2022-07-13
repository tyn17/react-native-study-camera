package com.reactnativestudycamera;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nonnull;

public class StudyCameraViewManager extends ViewGroupManager<CameraPreviewView> {
  public static final String REACT_CLASS = "StudyCameraView";
  private CameraPreviewView cameraPreviewView;

  @Override
  @Nonnull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @Nonnull
  public CameraPreviewView createViewInstance(ThemedReactContext reactContext) {
    this.cameraPreviewView = new CameraPreviewView(reactContext);
    return this.cameraPreviewView;
  }

  public CameraPreviewView getCameraPreviewView() {
    return cameraPreviewView;
  }

  @ReactProp(name = "bodyPart")
  public void setBodyPart(CameraPreviewView view, int bodyPart) {
    view.setBodyPart(bodyPart);
  }

  @ReactProp(name = "subFolder")
  public void setSubFolder(CameraPreviewView view, String subFolder) {
    view.setSubFolder(subFolder);
  }

  @ReactProp(name = "visualMask")
  public void setVisualMask(CameraPreviewView view, boolean visualMask) {
    view.setVisualMask(visualMask);
  }

  @ReactProp(name = "detectionMode")
  public void setDetectionMode(CameraPreviewView view, int detectionMode) {
    view.setDetectionMode(detectionMode);
  }

  @ReactProp(name = "usePortraitScene")
  public void setUsePortraitScene(CameraPreviewView view, boolean usePortraitScene) {
    view.setUsePortraitScene(usePortraitScene);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @ReactProp(name = "useBackCamera")
  public void setUseBackCamera(CameraPreviewView view, boolean useBackCamera) {
    view.setUseBackCamera(useBackCamera);
  }

  @Nullable
  @Override
  public Map<String, Object> getExportedCustomBubblingEventTypeConstants() {
    MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
    return  builder.put(
      "capturedPhotoEvent",
      MapBuilder.of(
        "phasedRegistrationNames",
        MapBuilder.of("bubbled", "onCaptured")
      )
    ).put(
      "detectionEvent",
      MapBuilder.of(
        "phasedRegistrationNames",
        MapBuilder.of("bubbled", "onDetected")
      )
    ).build();
  }
}
