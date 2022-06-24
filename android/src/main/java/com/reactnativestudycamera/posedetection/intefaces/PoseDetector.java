package com.reactnativestudycamera.posedetection.intefaces;

import android.graphics.Bitmap;

import com.reactnativestudycamera.posedetection.datamodels.PoseResult;

public interface PoseDetector extends AutoCloseable {
    void estimatePose(Bitmap bitmap, ImageProcessorCallback callback);
    long lastInferenceTimeNanos();
}
