package com.reactnativestudycamera;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StudyCameraPackage implements ReactPackage {
  private StudyCameraViewManager cameraViewManager;
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
      if (cameraViewManager == null) {
        cameraViewManager = new StudyCameraViewManager();
      }
      return Arrays.<NativeModule>asList(
        new StudyCameraModule(reactContext, cameraViewManager)
      );
    }
  public List<Class<? extends JavaScriptModule>> createJSModules() {
    return Collections.emptyList();
  }

  @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
      if (cameraViewManager == null) {
        cameraViewManager = new StudyCameraViewManager();
      }
      return Arrays.<ViewManager>asList(cameraViewManager);
    }
}
