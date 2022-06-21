package com.reactnativestudycamera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Environment;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

public class Utils {
  /**
   * Create New Image File
   * @return
   */
  public static File createNewImageFile() {
    String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "camera-2";
    File dir = new File(dirPath);
    if (!dir.exists() && dir.mkdir());
    return new File(dir, String.valueOf(System.currentTimeMillis()) + ".JPG");
  }

  /**
   * Save Image Data to File
   * @param file
   * @param bytes
   * @throws IOException
   */
  public static void save(File file, byte[] bytes) throws IOException {
    try (OutputStream output = new FileOutputStream(file)) {
      output.write(bytes);
    }
  }

  /**
   * Encode Image data to Base64
   * @param bytes
   * @return
   */
  public static String base64Image(byte[] bytes) {
    byte[] encoded = Base64.getEncoder().encode(bytes);
    return new String(encoded);
  }

  /**
   * Check Size is fit by Ratio
   * @param size
   * @param ratio
   * @return
   */
  public static boolean isFitRatio(Size size, float ratio) {
    float rat = 1.0f * size.getWidth() / size.getHeight();
    return Math.abs(ratio - rat) < 0.02;
  }

  /**
   * Get Expected Image Size by Ratio
   * @param map
   * @param ratio
   * @param isPreview
   * @return
   */
  public static Size getSizeByRatio(StreamConfigurationMap map, float ratio, boolean isPreview) {
    if (isPreview) {
      Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
      for (Size size : sizes) {
        if (isFitRatio(size, ratio)) return size;
      }
      return sizes[0];
    } else {
      Size resultSize = null;
      Size[] hSizes = map.getHighResolutionOutputSizes(ImageFormat.JPEG);
      if (hSizes != null) {
        for (Size size : hSizes) {
          if (isFitRatio(size, ratio) && (resultSize == null || resultSize.getWidth() < size.getWidth())) {
            resultSize = size;
          }
        }
      }

      Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
      if (sizes != null) {
        for (Size size : sizes) {
          if (Utils.isFitRatio(size, ratio) && (resultSize == null || resultSize.getWidth() < size.getWidth())) {
            resultSize = size;
          }
        }
      }
      if (resultSize == null) resultSize = hSizes != null && hSizes.length > 0 ? hSizes[0] : sizes[0];
      return  resultSize;
    }
  }

}
