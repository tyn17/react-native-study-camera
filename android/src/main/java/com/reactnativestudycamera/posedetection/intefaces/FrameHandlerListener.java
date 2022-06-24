package com.reactnativestudycamera.posedetection.intefaces;

import com.reactnativestudycamera.posedetection.datamodels.PoseResult;

public interface FrameHandlerListener {
  void onResult(PoseResult result);
}
