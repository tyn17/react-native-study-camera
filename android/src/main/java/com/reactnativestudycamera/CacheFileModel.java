package com.reactnativestudycamera;

import com.reactnativestudycamera.posedetection.movenet.BodyPart;

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

  public static String serialize(List<CacheFileModel> list) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) sb.append(",");
      sb.append(list.get(i).toJson());
    }
    sb.append("]");
    return sb.toString();
  }
}
