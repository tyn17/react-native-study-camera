package com.reactnativestudycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Base64;

public class Utils {
  private static final String IMAGE_EXTENSION = ".JPG";
  private static final String THUMB_EXTENSION = "_thumbnail.JPG";
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
   * Save Image Data to File. Return Thumbnail bytes if withThumb is true
   * @param file
   * @param bytes
   * @param withThumb
   * @throws IOException
   */
  public static byte[] save(File file, byte[] bytes, boolean withThumb) throws IOException {
    byte[] result = bytes;
    try (OutputStream output = new FileOutputStream(file)) {
      output.write(bytes);
    }
    if (withThumb) {

      String thumbPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - IMAGE_EXTENSION.length()) + THUMB_EXTENSION;
      File thumbFile = new File(thumbPath);
      if (thumbFile.exists() && thumbFile.delete());
      if (thumbFile.createNewFile()) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        float scale = 512.0f / Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap thumb = resizeBitmap(file.getAbsolutePath(), bitmap, scale);
        ByteArrayOutputStream thumbBytes = new ByteArrayOutputStream();
        if (thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbBytes)) {
          try (OutputStream thumbOs = new FileOutputStream(thumbFile)) {
            result = thumbBytes.toByteArray();
            thumbOs.write(result);
          }
        }
        thumbBytes.close();
        thumb.recycle();
        bitmap.recycle();
      }
    }
    return result;
  }

  /**
   * Delete Cached Image Files
   * @param context
   * @param subFolder
   */
  public static void deleteCaches(Context context, String subFolder) {
    File[] files = getFilesInFolder(context, subFolder);
    if (files != null) {
      for (File file: files) {
        try {
          Log.d("DELETE IMAGE", file.getAbsolutePath());
          file.delete();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static String getCachedImage(Context context, String subFolder, int bodyPart, boolean thumbnail) {
    String imagePath = null;
    if (thumbnail) {
      imagePath = getFilesRootFolder(context) + File.separator + subFolder + File.separator + bodyPart + THUMB_EXTENSION;
      File file = new File(imagePath);
      if (!file.exists()) imagePath = null;
    }
    if (imagePath == null) {
      imagePath = getFilesRootFolder(context) + File.separator + subFolder + File.separator + bodyPart + IMAGE_EXTENSION;
    }
    File file = new File(imagePath);
    Log.d("GET IMAGE", file.getAbsolutePath());
    if (file.exists() && file.isFile()) {
      try {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return base64Image(bytes);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }
    return null;
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

  private static int getExifOrientation(String src) throws IOException {
    ExifInterface exif = new ExifInterface(src);
    return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
  }

  private static Bitmap resizeBitmap(String src, Bitmap originalBitmap, float scale) {
    int orientation = 1;
    try {
      orientation = getExifOrientation(src);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    Matrix matrix = new Matrix();
    switch (orientation) {
      case 2:
        matrix.setScale(-scale, scale);
        break;
      case 3:
        matrix.postScale(scale, scale);
        matrix.postRotate(180);
        break;
      case 4:
        matrix.postScale(-scale, scale);
        matrix.postRotate(180);
        break;
      case 5:
        matrix.postScale(-scale, scale);
        matrix.postRotate(90);
        break;
      case 6:
        matrix.postScale(scale, scale);
        matrix.postRotate(90);
        break;
      case 7:
        matrix.postScale(-scale, scale);
        matrix.postRotate(-90);
        break;
      case 8:
        matrix.postScale(scale, scale);
        matrix.postRotate(-90);
        break;
      default:
        matrix.setScale(scale, scale);
        break;
    }
    return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
  }
}
