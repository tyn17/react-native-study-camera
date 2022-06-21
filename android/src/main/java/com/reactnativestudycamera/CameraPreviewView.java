package com.reactnativestudycamera;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class CameraPreviewView extends LinearLayout {
  private ThemedReactContext context;
  private int bodyPart;
  public CameraPreviewView(ThemedReactContext context) {
    super(context);
    this.context = context;
    initialize();
  }

  private void initialize() {
    inflate(this.context, R.layout.camera_preview, this);
    ImageButton clickButton = findViewById(R.id.btnCapture);
    final Context _context = context;
    clickButton.setOnClickListener(v -> {
      WritableMap event = Arguments.createMap();
      event.putString("imageBase64", Constants.demoImage);
      context.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "capturedPhotoEvent", event);
    });
  }

  public void setBodyPart(int bodyPart) {
    this.bodyPart = bodyPart;
  }
}
