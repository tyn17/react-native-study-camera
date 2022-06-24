package com.reactnativestudycamera.posedetection.intefaces;

import com.reactnativestudycamera.posedetection.datamodels.PoseResult;

public interface ImageProcessorCallback {
  void processFinished(PoseResult pose);
}
