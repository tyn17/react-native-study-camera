package com.reactnativestudycamera.posedetection.movenet;

import java.util.List;

public class TensorUtils {
  public static final float MIN_KEYPOINT_SCORE = .3f;


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
