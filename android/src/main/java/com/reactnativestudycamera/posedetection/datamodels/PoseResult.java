package com.reactnativestudycamera.posedetection.datamodels;

import java.util.List;

public class PoseResult {
  private int imageWidth;
  private int imageHeight;
  private List<KeyPoint> keyPoints;
  private float score;

  public PoseResult(List<KeyPoint> keyPoints, float score, int imageWidth, int imageHeight) {
    this.keyPoints = keyPoints;
    this.score = score;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public List<KeyPoint> getKeyPoints() {
    return keyPoints;
  }

  public void setKeyPoints(List<KeyPoint> keyPoints) {
    this.keyPoints = keyPoints;
  }

  public float getScore() {
    return score;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public void setImageWidth(int imageWidth) {
    this.imageWidth = imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  public void setImageHeight(int imageHeight) {
    this.imageHeight = imageHeight;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("PoseResult{")
      .append("score=").append(score)
        .append(",keyPoints=[");
    for(int i = 0; i < keyPoints.size(); i++) {
      sb.append(keyPoints.get(i).toString()).append(",");
    }
    sb.append("]").append("}");
    return sb.toString();
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"imageWidth\":").append(imageWidth).append(",")
      .append("\"imageHeight\":").append(imageHeight).append(",");
    if (keyPoints != null) {
      sb.append("\"keyPoints\":[");
      for (int i = 0; i < keyPoints.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append(keyPoints.get(i).serialize());
      }
      sb.append("],");
    }
    sb.append("\"score\":").append(score).append("}");
    return sb.toString();
  }
}
