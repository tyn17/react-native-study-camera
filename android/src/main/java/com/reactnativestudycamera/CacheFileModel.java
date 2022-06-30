package com.reactnativestudycamera;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.reactnativestudycamera.posedetection.movenet.BodyPart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

public class CacheFileModel {
  private int bodyPart;
  private String base64Image;

  public CacheFileModel(int bodyPart, String base64Image) {
    this.bodyPart = bodyPart;
    this.base64Image = base64Image;
  }

  public int getBodyPart() {
    return bodyPart;
  }

  public void setBodyPart(int bodyPart) {
    this.bodyPart = bodyPart;
  }

  public String getBase64Image() {
    return base64Image;
  }

  public void setBase64Image(String base64Image) {
    this.base64Image = base64Image;
  }

  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{")
      .append("\"bodyPart\":").append(bodyPart).append(",")
      .append("\"base64Image\":\"").append(base64Image).append("\"")
      .append("}");
    return  sb.toString();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static String serialize(List<CacheFileModel> list) {
    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + System.currentTimeMillis() + ".tmp";
    File tmpFile = new File(path);
    try {
      tmpFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (PrintWriter writer = new PrintWriter(tmpFile)) {
      writer.append("[");
      if (list != null) {
        for (int i = 0; i < list.size(); i++) {
          if (i > 0) writer.append(",");
          writer.append(list.get(i).toJson());
        }
      }
      writer.append("]");
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Read
    try {
      String string = new String(Files.readAllBytes(tmpFile.toPath()));
      tmpFile.delete();
      return string;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;

//    StringBuilder sb = new StringBuilder();
//    sb.append("[");
//    if (list != null) {
//      for (int i = 0; i < list.size(); i++) {
//        if (i > 0) sb.append(",");
//        sb.append(list.get(i).toJson());
//      }
//    }
//    sb.append("]");
//    return sb.toString();
  }
}
