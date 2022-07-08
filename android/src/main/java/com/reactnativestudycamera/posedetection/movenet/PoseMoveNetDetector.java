package com.reactnativestudycamera.posedetection.movenet;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import com.reactnativestudycamera.posedetection.datamodels.Device;
import com.reactnativestudycamera.posedetection.datamodels.KeyPoint;
import com.reactnativestudycamera.posedetection.datamodels.ModelType;
import com.reactnativestudycamera.posedetection.datamodels.PoseResult;
import com.reactnativestudycamera.posedetection.intefaces.ImageProcessorCallback;
import com.reactnativestudycamera.posedetection.intefaces.PoseDetector;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PoseMoveNetDetector implements PoseDetector {
  private final int CPU_NUM_THREADS = 4;
  // TFLite file names.
  private final String LIGHTNING_FILENAME = "movenet_lightning.tflite";
  private final String THUNDER_FILENAME = "movenet_thunder.tflite";

  long lastInferenceTimeNanos = -1;

  private Interpreter interpreter;
  private GpuDelegate gpuDelegate = null;
  private int[] outputShape;
  private int inputWidth;
  private int inputHeight;

  public PoseMoveNetDetector(Context context, Device device, ModelType modelType) throws IOException {
    Interpreter.Options options = new Interpreter.Options();
    options.setNumThreads(CPU_NUM_THREADS);
    switch (device) {
      case CPU:
        break;
      case GPU:
        gpuDelegate = new GpuDelegate();
        options.addDelegate(gpuDelegate);
        break;
      case NNAPI:
        options.setUseNNAPI(true);
        break;
    }
    MappedByteBuffer byteBuffer = FileUtil.loadMappedFile(context, modelType == ModelType.Lightning ? LIGHTNING_FILENAME : THUNDER_FILENAME);
    interpreter = new Interpreter(byteBuffer, options);
    outputShape = interpreter.getOutputTensor(0).shape();
    inputWidth = interpreter.getInputTensor(0).shape()[1];
    inputHeight = interpreter.getInputTensor(0).shape()[2];
  }

  @Override
  public void estimatePose(Bitmap detectBitmap, ImageProcessorCallback callback) {
    try {
      long inferenceStartTimeNanos = SystemClock.elapsedRealtimeNanos();
      float totalScore = 0f;

      int numKeyPoints = outputShape[2];
      List<KeyPoint> keyPoints = new ArrayList<>();

      int imageWidth = detectBitmap.getWidth();
      int imageHeight = detectBitmap.getHeight();

      TensorImage inputTensor = processInputImage(detectBitmap, inputWidth, inputHeight);
      TensorBuffer outputTensor = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);
      float widthRatio = 1.0f * imageWidth / inputWidth;
      float heightRatio = 1.0f * imageHeight / inputHeight;

      //Detect Pose
      interpreter.run(inputTensor.getBuffer(), outputTensor.getBuffer().rewind());
      float[] output = outputTensor.getFloatArray();
      List<Float> positions = new ArrayList<Float>();
      for (int idx = 0; idx < numKeyPoints; idx++) {
        float x = output[idx * 3 + 1] * inputWidth * widthRatio;
        float y = output[idx * 3] * inputHeight * heightRatio;

        positions.add(x);
        positions.add(y);
        float score = output[idx * 3 + 2];
        keyPoints.add(new KeyPoint(BodyPart.fromInt(idx), x, y, 0, score));
        totalScore += score;
      }

      //Matrix matrix = new Matrix();
      float[] points = TensorUtils.toArray(positions);
      //matrix.postTranslate(rect.left, rect.top);

      for (int i = 0; i < keyPoints.size(); i++) {
        keyPoints.get(i).setX(points[i * 2] / imageWidth);
        keyPoints.get(i).setY(points[i * 2 + 1] / imageHeight);
      }

      lastInferenceTimeNanos = SystemClock.elapsedRealtimeNanos() - inferenceStartTimeNanos;

      callback.processFinished(new PoseResult(keyPoints, totalScore / numKeyPoints, imageWidth, imageHeight));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public long lastInferenceTimeNanos() {
    return lastInferenceTimeNanos;
  }

  @Override
  public void close() throws Exception {
    if (gpuDelegate != null) {
      gpuDelegate.close();
    }
    interpreter.close();
  }

  /**
   * Prepare input image for detection
   */
  private TensorImage processInputImage(Bitmap bitmap, int inputWidth, int inputHeight) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();

    int size = Math.max(height, width);
    ImageProcessor imageProcessor = new ImageProcessor.Builder()//.add(new ResizeWithCropOrPadOp(size, size))
      .add(new ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR)).build();
    TensorImage tensorImage = new TensorImage(DataType.UINT8);
    tensorImage.load(bitmap);
    return imageProcessor.process(tensorImage);
  }
}
