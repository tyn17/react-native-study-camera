package com.reactnativestudycamera;

import androidx.annotation.Nullable;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nonnull;

public class StudyCameraViewManager extends ViewGroupManager<CameraPreviewView> {
  public static final String REACT_CLASS = "StudyCameraView";

  @Override
  @Nonnull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @Nonnull
  public CameraPreviewView createViewInstance(ThemedReactContext reactContext) {
    return new CameraPreviewView(reactContext);
  }

  @ReactProp(name = "bodyPart")
  public void setBodyPart(CameraPreviewView view, int bodyPart) {
    view.setBodyPart(bodyPart);
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
    ).build();
  }
}
