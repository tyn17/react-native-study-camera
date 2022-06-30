package com.reactnativestudycamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactnativestudycamera.consts.DetectionMode;
import com.reactnativestudycamera.drawing.PoseCanvasView;
import com.reactnativestudycamera.posedetection.FrameHandler;
import com.reactnativestudycamera.posedetection.datamodels.PoseResult;
import com.reactnativestudycamera.posedetection.intefaces.FrameHandlerListener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraPreviewView extends LinearLayout implements TextureView.SurfaceTextureListener, FrameHandlerListener {
  static int REQUEST_CAMERA_PERMISSION = 200;
  static final SparseIntArray ORIENTATIONS = new SparseIntArray();
  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }
  static float ASPECT_RATIO = 4.0f/3;
  private static final int STATE_PREVIEW = 0;
  private static final int STATE_WAIT_LOCK = 1;
  private int state = STATE_PREVIEW;
//  private int maxAFRegions = 1;
//  private Rect sensorArraySize = null;
//  private final MeteringRectangle[] focusAreas = new MeteringRectangle[] {
//    new MeteringRectangle(1991, 839, 300, 300, 999),
//    new MeteringRectangle(2221, 2716, 300, 300, 999),
//    new MeteringRectangle(1014, 1748, 300, 300, 999)
//  };

  private ThemedReactContext context;
  private String subFolder;
  private int bodyPart;
  private boolean visualMask;
  private int detectionMode;

  private TextureView textureView;
  private Size previewSize;
  private String cameraId;
  private CameraDevice cameraDevice;
  private CameraCaptureSession cameraCaptureSession;
  private CaptureRequest.Builder previewCRBuilder;
  private ImageReader imageReader;

  private HandlerThread backgroundThread;
  private Handler backgroundHandler;
  private FrameHandler frameHandler;
  private PoseCanvasView maskView;

  public CameraPreviewView(ThemedReactContext context) {
    super(context);
    this.context = context;
    initialize();
  }

  private void initialize() {
    inflate(this.context, R.layout.camera_preview, this);
    textureView = (TextureView) findViewById(R.id.textureView);
    textureView.setSurfaceTextureListener(this);

    maskView = findViewById(R.id.maskView);
    startBackgroundThread();
  }

  public void setBodyPart(int bodyPart) {
    this.bodyPart = bodyPart;
  }

  public void setSubFolder(String subFolder) {
    this.subFolder = subFolder;
  }

  public void setVisualMask(boolean visualMask) {
    this.visualMask = visualMask;
    maskView.setVisibility(visualMask ? VISIBLE : GONE);
  }

  public void setDetectionMode(int detectionMode) {
    this.detectionMode = detectionMode;
    if (detectionMode == DetectionMode.NONE) {
      disableDetection();
    } else {
      enableDetection();
    }
  }

  //Setup Camera
  @RequiresApi(api = Build.VERSION_CODES.M)
  void setupCamera() {
    CameraManager cameraManager = (CameraManager) context.getCurrentActivity().getSystemService(Context.CAMERA_SERVICE);
    try {
      Size imageSize = new Size(640, 480);
      for (String id : cameraManager.getCameraIdList()) {
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
        if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size mSize = Utils.getSizeByRatio(map, ASPECT_RATIO, false);
        if (imageSize.getWidth() < mSize.getWidth()) {
          previewSize = Utils.getSizeByRatio(map, ASPECT_RATIO, true);
          cameraId = id;

          imageSize = mSize;

//          maxAFRegions = cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
//          sensorArraySize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//
//          Log.d("AFRegion", "MaxRegions: " + maxAFRegions);
//          Log.d("AFRegion", "Size: " + sensorArraySize.left + ", " + sensorArraySize.top + ", " + sensorArraySize.right + ", " + sensorArraySize.bottom);
        }
      }

      imageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), ImageFormat.JPEG,1);
      imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  //Open Camera
  void openCamera() {
    final Activity currentActivity = context.getCurrentActivity();
    CameraManager cameraManager = (CameraManager) currentActivity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (ActivityCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        return;
      }
      cameraManager.openCamera(cameraId, cameraStateCallback, backgroundHandler);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  //Close Camera
  private void closeCamera(){
    if (cameraDevice != null) {
      cameraDevice.close();
      cameraDevice = null;
    }
    if (cameraCaptureSession != null) {
      cameraCaptureSession.close();
      cameraCaptureSession = null;
    }
    if (imageReader != null) {
      imageReader.close();
      imageReader = null;
    }
  }

  //Create Camera Preview Session
  void createCameraPreviewSession() {
    try {
      SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
      surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
      Surface previewSurface = new Surface(surfaceTexture);

      previewCRBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      previewCRBuilder.addTarget(previewSurface);

      cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
          if (cameraDevice == null) return;
          try {
            CaptureRequest previewRequest = previewCRBuilder.build();
            cameraCaptureSession = session;
            cameraCaptureSession.setRepeatingRequest(previewRequest, cameraSessionCaptureCallback,null);
          } catch (CameraAccessException e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
          Toast.makeText(context, "Create camera session fail", Toast.LENGTH_SHORT).show();
        }
      }, null);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  final private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
      cameraDevice = camera;
      //Create Camera Preview Session
      createCameraPreviewSession();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
      closeCamera();
    }

    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
      closeCamera();
    }
  };

  final private CameraCaptureSession.CaptureCallback cameraSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
    @Override
    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
      super.onCaptureStarted(session, request, timestamp, frameNumber);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
      super.onCaptureCompleted(session, request, result);
      processBeforeTakePicture(result);
    }

    @Override
    public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
      super.onCaptureFailed(session, request, failure);
    }

    private void processBeforeTakePicture(CaptureResult result) {
      switch (state) {
        case STATE_WAIT_LOCK:
          Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
          Integer flashState = result.get(CaptureResult.FLASH_STATE);
          if (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED && flashState == CaptureRequest.FLASH_STATE_FIRED) {
            takePicture();
            unlockFocus();
          }
          break;
        default:
          break;
      }
    }
  };

  final private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onImageAvailable(ImageReader reader) {
      File file = Utils.createNewImageFile(context, subFolder, "" + bodyPart);
      try (Image image = reader.acquireLatestImage()) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Utils.save(file, bytes);

        //Return Image to React-Native
        WritableMap event = Arguments.createMap();
        event.putString("imageBase64", Utils.base64Image(bytes));
        sendEvent("capturedPhotoEvent", event);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  };

  /**
   * Trigger Take Picture
   */
  private void takePicture() {
    try
    {
      final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
      captureBuilder.addTarget(imageReader.getSurface());
//      if (focusAreas != null && focusAreas.length > 0) {
//        captureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, focusAreas);
//      }
//      captureBuilder.set(CaptureRequest.EDGE_MODE, CameraMetadata.EDGE_MODE_FAST);
      captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
      captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
      captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);
      captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);

      // Orientation
      int rotation = context.getCurrentActivity().getWindowManager().getDefaultDisplay().getRotation();
      captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

      CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
          super.onCaptureCompleted(session, request, result);
          Toast.makeText(context, "Captured a photo", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
          super.onCaptureFailed(session, request, failure);
          Toast.makeText(context, "Failed. " + failure.toString(), Toast.LENGTH_SHORT).show();
        }
      };
      cameraCaptureSession.capture(captureBuilder.build(), captureCallback, null);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Lock Focus before get Image from Camera
   */
  private void lockFocus() {
    try {
      state = STATE_WAIT_LOCK;
//      if (focusAreas != null && focusAreas.length > 0) {
//        previewCRBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, focusAreas);
//      }
      previewCRBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
      previewCRBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
      cameraCaptureSession.capture(previewCRBuilder.build(), cameraSessionCaptureCallback, backgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Unlock Focus after captured from Camera
   */
  private void unlockFocus() {
    try {
      state = STATE_PREVIEW;
      previewCRBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
      previewCRBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
      cameraCaptureSession.capture(previewCRBuilder.build(), cameraSessionCaptureCallback, backgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  //Enable Detection
  private void enableDetection() {
    if (frameHandler == null) frameHandler = new FrameHandler(this);
    try {
      frameHandler.startDetector(context.getApplicationContext());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //Disable Detection
  private void disableDetection() {
    if (frameHandler != null) {
      try {
        frameHandler.stopDetector();
        frameHandler = null;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  //Start Background Thread
  private void startBackgroundThread() {
    Log.d("STUDY-CAMERA", "Start Background Thread");
    if (backgroundThread == null || !backgroundThread.isAlive()) {
      backgroundThread = new HandlerThread("BelleCameraBackground");
      backgroundThread.start();
      backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    enableDetection();
  }

  //Stop Background Thread
  private void stopBackgroundThread() {
    Log.d("STUDY-CAMERA", "Stop Background Thread");
    if (backgroundThread != null) {
      backgroundThread.quitSafely();
      try {
        backgroundThread.join();
        backgroundThread = null;
        backgroundHandler = null;
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    disableDetection();
  }

  //Send Event to Js
  private void sendEvent(String eventName, WritableMap event) {
    context.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), eventName, event);
  }
  //----PUBLIC METHODS------
  // Call Take Picture
  public void capturePhoto() {
    Log.d("STUDY-CAMERA", "Call Capture Photo");
    if (this.bodyPart < 0) {
      return;
    }
    //The photo will be captured on Lock Focus Callback
    lockFocus();
  }

  // Theses should be called when change React-Native Activity Lifecycle
  @RequiresApi(api = Build.VERSION_CODES.M)
  public void resume() {
    startBackgroundThread();
    textureView.setSurfaceTextureListener(this);
    if (textureView.isAvailable()) {
      onSurfaceTextureAvailable(textureView.getSurfaceTexture(), textureView.getWidth(), textureView.getHeight());
    }
  }

  public void pause() {
    closeCamera();
    stopBackgroundThread();
  }
  //----END PUBLIC METHODS------

  //-------TextureView.SurfaceTextureListener--------
  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    setupCamera();
    openCamera();
  }

  @Override
  public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

  }

  @Override
  public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
    return false;
  }

  @Override
  public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    if (detectionMode == DetectionMode.NONE || frameHandler == null || frameHandler.isProcessing()) return;
    Bitmap photo = textureView.getBitmap();
    frameHandler.processBitmap(photo);
  }
  //-----End TextureView.SurfaceTextureListener------
  //-----FrameHandleListener------
  @Override
  public void onResult(PoseResult result) {
    //Log.d("POSING", result.toString());
    WritableMap event = Arguments.createMap();
    event.putString("pose", result.serialize());
    sendEvent("detectionEvent", event);
    if (maskView != null && visualMask) {
      maskView.drawPose(result);
    }
  }
  //-----End FrameHandleListener------


//  @Override
//  public boolean onTouchEvent(MotionEvent event) {
//    final int actionMasked = event.getActionMasked();
//    if (actionMasked != MotionEvent.ACTION_DOWN) {
//      return false;
//    }
//
//    //TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
//    final int y = (int)((event.getX() / (float)this.getWidth())  * (float)sensorArraySize.height());
//    final int x = (int)((event.getY() / (float)this.getHeight()) * (float)sensorArraySize.width());
//    final int halfTouchWidth  = 150; //(int)motionEvent.getTouchMajor(); //TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
//    final int halfTouchHeight = 150; //(int)motionEvent.getTouchMinor();
//    MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth,  0),
//      Math.max(y - halfTouchHeight, 0),
//      halfTouchWidth  * 2,
//      halfTouchHeight * 2,
//      MeteringRectangle.METERING_WEIGHT_MAX - 1);
//    Log.d("FocusArea", focusAreaTouch.getX() + ", " + focusAreaTouch.getY() + ", " + focusAreaTouch.getMeteringWeight());
//    return true;
//  }
}
