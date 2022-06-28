package com.reactnativestudycamera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Utils {
  private static final String IMAGE_EXTENSION = ".JPG";
  /**
   * Get Root Folder
   * @param context
   * @return
   */
  public static String getFilesRootFolder(Context context) {
    return context.getFilesDir().getAbsolutePath();
  }
  /**
   * Create New Image File
   * @return
   */
  public static File createNewImageFile(Context context, String subFolder, String fileName) {
    String dirPath = getFilesRootFolder(context) + File.separator + subFolder;
    File dir = new File(dirPath);
    if (!dir.exists() && dir.mkdir());
    File newFile = new File(dir, fileName + IMAGE_EXTENSION);
    if (newFile.exists() && newFile.delete());
    return newFile;
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
   * Delete Cached Image Files
   * @param context
   * @param subFolder
   */
  public static void deleteCache(Context context, String subFolder) {
    File[] files = getFilesInFolder(context, subFolder);
    if (files != null) {
      for (File file: files) {
        try {
          file.delete();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static List<CacheFileModel> getCacheFiles(Context context, String subFolder) {
    File[] files = getFilesInFolder(context, subFolder);
    if (files == null) return null;
    List<CacheFileModel> result = new ArrayList<>();
    int[] bodyParts = new int[] {0, 1, 2, 3};
    for (int bp: bodyParts) {
      for (File file: files) {
        if (file.getName().equalsIgnoreCase("" + bp + IMAGE_EXTENSION)) {
          try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = base64Image(bytes);
            result.add(new CacheFileModel(bp, base64));
            break;
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return result;
  }

  private static File[] getFilesInFolder(Context context, String subFolder) {
    String dirPath = getFilesRootFolder(context) + File.separator + subFolder;
    File dir = new File(dirPath);
    if (dir.exists()) {
      return dir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(IMAGE_EXTENSION));
    }
    return null;
  }

  /**
   * Encode Image data to Base64
   * @param bytes
   * @return
   */
  @RequiresApi(api = Build.VERSION_CODES.O)
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
  @RequiresApi(api = Build.VERSION_CODES.M)
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
