package com.reactnativestudycamera.posedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.reactnativestudycamera.posedetection.datamodels.Device;
import com.reactnativestudycamera.posedetection.datamodels.ModelType;
import com.reactnativestudycamera.posedetection.intefaces.FrameHandlerListener;
import com.reactnativestudycamera.posedetection.intefaces.PoseDetector;
import com.reactnativestudycamera.posedetection.movenet.PoseMoveNetDetector;

import java.io.IOException;

public class FrameHandler {
  private boolean blnProcessing;
  public boolean isProcessing() {
    return blnProcessing;
  }

  private FrameHandlerListener listener;

  private PoseDetector detector;

  public FrameHandler(FrameHandlerListener listener) {
    this.listener = listener;
    blnProcessing = false;
  }

  /**
   * Start Detector
   */
  public void startDetector(Context context) throws IOException {
    if (detector == null) {
      blnProcessing = false;
      detector = new PoseMoveNetDetector(context, Device.GPU, ModelType.Lightning);
    }
  }

  /**
   * Stop and Close Detector
   * @throws Exception
   */
  public void stopDetector() throws Exception {
    if (detector != null) {
      detector.close();
      detector = null;
      blnProcessing = false;
    }
  }

  /**
   * Process Image and Trigger Listener for Result
   * @param bitmap
   */
  public void processBitmap(Bitmap bitmap) {
    //Log.d("POSING", "Start process Image");
    //Log.d("POSING", "Processing ? " + blnProcessing);
    if (detector != null && !blnProcessing) {
      //Log.d("POSING", "Has Detector and not processing");
      blnProcessing = true;
      detector.estimatePose(bitmap, pose -> {
        blnProcessing = false;
        //Log.d("POSING", "Result process Image");
        if (listener != null) {
          listener.onResult(pose);
        }
      });
    }
  }
}
