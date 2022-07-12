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

import com.reactnativestudycamera.encrypt.EncryptUtils;
import com.reactnativestudycamera.encrypt.EncryptedModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Utils {
  private static final boolean ENCRYPT_IMAGE = true;
  private static final String IMAGE_EXTENSION = ENCRYPT_IMAGE ? ".EJPG" : ".JPG";
  private static final String THUMB_EXTENSION = ENCRYPT_IMAGE ? "_thumbnail.EJPG" : "_thumbnail.JPG";
  private static final String KEY_FILE_EXTENSION = ".enc";

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
  @RequiresApi(api = Build.VERSION_CODES.O)
  public static byte[] save(File file, byte[] bytes, boolean withThumb) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
    byte[] result = bytes;
    try (OutputStream output = new FileOutputStream(file)) {
      if (ENCRYPT_IMAGE) {
        EncryptedModel encrypted = EncryptUtils.encrypt(bytes);
        output.write(encrypted.getEncryptedData());
        //Write key
        writeKeyFile(file.getAbsolutePath(), encrypted.getEncryptedKeyBase64());
      } else {
        output.write(bytes);
      }
    }
    if (withThumb) {

      String thumbPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - IMAGE_EXTENSION.length()) + THUMB_EXTENSION;
      File thumbFile = new File(thumbPath);
      if (thumbFile.exists() && thumbFile.delete());
      if (thumbFile.createNewFile()) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        float scale = 512.0f / Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap thumb = resizeBitmap(bytes, bitmap, scale);
        ByteArrayOutputStream thumbBytes = new ByteArrayOutputStream();
        if (thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbBytes)) {
          try (OutputStream thumbOs = new FileOutputStream(thumbFile)) {
            result = thumbBytes.toByteArray();
            if (ENCRYPT_IMAGE) {
              EncryptedModel encryptedThumb = EncryptUtils.encrypt(result);
              thumbOs.write(encryptedThumb.getEncryptedData());
              //Write key
              writeKeyFile(thumbPath, encryptedThumb.getEncryptedKeyBase64());
            } else {
              thumbOs.write(result);
            }
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

  /**
   * Delete all Cached Image Files
   * @param context
   */
  public static void deleteAllCaches(Context context) {
    String rootPath = getFilesRootFolder(context);
    File dir = new File(rootPath);
    deleteDirectory(dir, false);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static String getCachedImage(Context context, String subFolder, int bodyPart, boolean thumbnail, boolean returnFilePath) {
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
      if (returnFilePath) return file.getAbsolutePath();
      try {
        byte[] bytes = Files.readAllBytes(file.toPath());
        if (ENCRYPT_IMAGE) {
          String keyBase64 = readKeyFile(imagePath);
          bytes = EncryptUtils.decrypt(bytes, keyBase64);
        }
        return EncryptUtils.toBase64(bytes);
      } catch (IOException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
        e.printStackTrace();
        return null;
      }
    }
    return null;
  }

  public static boolean hasCachedFiles(Context context, boolean onlyCheckOrigin) {
    String dirPath = getFilesRootFolder(context);
    File dir = new File(dirPath);
    if (dir.exists()) {
      File[] subFolders = dir.listFiles(File::isDirectory);
      if (subFolders != null && subFolders.length > 0) {
        for (File subFolder : subFolders) {
          File[] files = null;
          if (onlyCheckOrigin) {
            files = subFolder.listFiles(pathname -> pathname.isFile() && (pathname.getName().endsWith(IMAGE_EXTENSION)));
          } else {
            files = subFolder.listFiles(pathname -> pathname.isFile() && (pathname.getName().endsWith(IMAGE_EXTENSION) || pathname.getName().endsWith(THUMB_EXTENSION)));
          }
          if (files != null && files.length > 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static File[] getFilesInFolder(Context context, String subFolder) {
    String dirPath = getFilesRootFolder(context) + File.separator + subFolder;
    File dir = new File(dirPath);
    if (dir.exists()) {
      return dir.listFiles(pathname -> pathname.isFile() && (pathname.getName().endsWith(IMAGE_EXTENSION) || pathname.getName().endsWith(KEY_FILE_EXTENSION)));
    }
    return null;
  }

  /**
   * Recursive Delete Directory and all content
   * @param dir
   */
  private static void deleteDirectory(File dir, boolean deleteItSelf) {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (File file : files) {
        if (file.isFile()) {
          file.delete();
        } else {
          deleteDirectory(file, true);
        }
      }
      if (deleteItSelf) dir.delete();
    }
  }

  /**
   * Write Key File
   * @param imageFilePath
   * @param key
   * @throws IOException
   */
  private static void writeKeyFile(String imageFilePath, String key) throws IOException {
    File keyFile = new File(imageFilePath + KEY_FILE_EXTENSION);
    if (keyFile.exists() && keyFile.delete());
    if (keyFile.createNewFile()) {
      try (FileWriter writer = new FileWriter(keyFile)) {
          writer.write(key);
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private static String readKeyFile(String imageFilePath) throws IOException {
    File keyFile = new File(imageFilePath + KEY_FILE_EXTENSION);
    if (keyFile.exists()) {
      byte[] bytes = Files.readAllBytes(keyFile.toPath());
      return new String(bytes, StandardCharsets.UTF_8);
    }
    return null;
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

  @RequiresApi(api = Build.VERSION_CODES.N)
  private static int getExifOrientation(byte[] bytes) throws IOException {
    InputStream istream = new ByteArrayInputStream(bytes);
    ExifInterface exif = new ExifInterface(istream);
    istream.close();
    return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private static Bitmap resizeBitmap(byte[] bytes, Bitmap originalBitmap, float scale) {
    int orientation = 1;
    try {
      orientation = getExifOrientation(bytes);
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
