package com.reactnativestudycamera;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CameraPreviewView extends LinearLayout {
  private Context context;
  public CameraPreviewView(Context context) {
    super(context);
    this.context = context;
    initialize();
  }

  private void initialize() {
    // set padding and background color
    this.setPadding(16,16,16,16);
    this.setBackgroundColor(Color.parseColor("#5FD3F3"));

    // add default text view
    TextView text = new TextView(context);
    text.setText("Welcome to Android Fragments with React Native.");
    this.addView(text);
  }
}
