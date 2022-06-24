package com.reactnativestudycamera.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import com.reactnativestudycamera.posedetection.datamodels.KeyPoint;
import com.reactnativestudycamera.posedetection.datamodels.PoseResult;
import com.reactnativestudycamera.posedetection.movenet.BodyPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoseCanvasView extends View {
  /** Pair of keyPoints to draw lines between.  */
  private List<Pair<BodyPart, BodyPart>> bodyJoints = Arrays.asList(
    new Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
    new Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
    new Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
    new Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
    new Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
    new Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
    new Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
    new Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
    new Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
    new Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
    new Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
    new Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
    new Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
    new Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
    new Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
    new Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
    new Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
    new Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
  );

  private PoseResult pose = null;
  private Paint paint = null;

  public PoseCanvasView(Context context) {
    super(context);
  }

  public PoseCanvasView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public PoseCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public PoseCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  private void initPaint() {
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.BLUE);
    paint.setStrokeWidth(10);
  }

  public void drawPose(PoseResult pose) {
    this.pose = pose;
    if (paint == null) initPaint();
    this.invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (paint != null && pose != null) {
      float w = 1.0f * getWidth();
      float h = 1.0f * getHeight();

      //Draw Points
      for (KeyPoint point : pose.getKeyPoints()) {
        canvas.drawCircle(point.getX() * w, point.getY() * h, 5, paint);
      }

      //Draw lines
      for(Pair<BodyPart, BodyPart> pair : bodyJoints) {
        KeyPoint start = pose.getKeyPoints().get(pair.first.position);
        KeyPoint stop = pose.getKeyPoints().get(pair.second.position);
        canvas.drawLine(start.getX() * w, start.getY() * h, stop.getX() * w, stop.getY() * h, paint);
      }
    }
  }
}
