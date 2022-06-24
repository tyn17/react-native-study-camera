package com.reactnativestudycamera.posedetection.movenet;

import com.reactnativestudycamera.posedetection.datamodels.KeyPoint;

import java.util.List;

public class TensorUtils {
  private static final float MIN_CROP_KEYPOINT_SCORE = .2f;
  /**
   * Checks whether there are enough torso keyPoints.
   * This function checks whether the model is confident at predicting one of the
   * shoulders/hips which is required to determine a good crop region.
   */
  public static boolean torsoVisible(List<KeyPoint> keyPoints) {
    return ((keyPoints.get(BodyPart.LEFT_HIP.position).getScore() > MIN_CROP_KEYPOINT_SCORE) || keyPoints.get(BodyPart.RIGHT_HIP.position).getScore() > MIN_CROP_KEYPOINT_SCORE)
      && ((keyPoints.get(BodyPart.LEFT_SHOULDER.position).getScore()> MIN_CROP_KEYPOINT_SCORE) || keyPoints.get(BodyPart.RIGHT_SHOULDER.position).getScore() > MIN_CROP_KEYPOINT_SCORE);
  }

  /**
   * Convert List of Floats to Array
   * @param list
   * @return
   */
  public static float[] toArray(List<Float> list) {
    float[] arr = new float[list.size()];
    for (int i = 0; i < list.size(); i++) {
      arr[i] = list.get(i);
    }
    return arr;
  }
}
