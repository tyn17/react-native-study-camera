package com.reactnativestudycamera;

import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

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

  @ReactProp(name = "color")
  public void setColor(View view, String color) {
    view.setBackgroundColor(Color.parseColor(color));
  }
}
