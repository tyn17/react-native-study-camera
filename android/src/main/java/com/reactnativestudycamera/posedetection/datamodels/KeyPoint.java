package com.reactnativestudycamera.posedetection.datamodels;

import com.reactnativestudycamera.posedetection.movenet.BodyPart;

public class KeyPoint {
  private BodyPart bodyPart;
  private float x;
  private float y;
  private float z;
  private float score;

  public KeyPoint() {
    this.bodyPart = BodyPart.NOSE;
    this.x = 0;
    this.y = 0;
    this.z = 0;
    this.score = 0;
  }

  public KeyPoint(BodyPart bodyPart, float x, float y, float z, float score) {
    this.bodyPart = bodyPart;
    this.x = x;
    this.y = y;
    this.z = z;
    this.score = score;
  }

  public BodyPart getBodyPart() {
    return bodyPart;
  }

  public void setBodyPart(BodyPart bodyPart) {
    this.bodyPart = bodyPart;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getZ() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float getScore() {
    return score;
  }

  public void setScore(float score) {
    this.score = score;
  }

  @Override
  public String toString() {
    return "KeyPoint{" +
      "bodyPart=" + bodyPart +
      ", x=" + x +
      ", y=" + y +
      ", z=" + z +
      ", score=" + score +
      '}';
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"bodyPart\":").append(bodyPart.position).append(",")
      .append("\"x\":").append(x).append(",")
      .append("\"y\":").append(y).append(",")
      .append("\"z\":").append(z).append(",")
      .append("\"score\":").append(score).append("}");
    return sb.toString();
  }
}
