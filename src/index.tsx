import React, { Component } from 'react';
import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  NativeSyntheticEvent,
  AppState,
  AppStateStatus,
} from 'react-native';
import {
  defaultOptions,
  CaptureBodyPart,
  DetectionMessageKeys,
  DetectionMode,
  PoseDetectOptions,
  PoseResult,
} from './pose_types';
import { verifyPose } from './pose_verify';
export * from './pose_types';

const LINKING_ERROR =
  `The package 'react-native-study-camera' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

type StudyCameraProps = {
  bodyPart: CaptureBodyPart;
  subFolder: String;
  style: ViewStyle;
  detectionMode: DetectionMode;
  visualMask: boolean;
  onCaptured: (event: NativeSyntheticEvent<any>) => void;
  onDetected: (event: NativeSyntheticEvent<any>) => void;
};

type CameraViewProps = {
  bodyPart: CaptureBodyPart;
  subFolder: String;
  detectionMode?: DetectionMode;
  visualMask?: boolean;
  style: ViewStyle;
  poseDetectOptions?: PoseDetectOptions;
  onRef?: (ref: CameraView) => void;
  onCaptured: (imageBase64: string) => void;
  onPoseVerify?: (message: DetectionMessageKeys) => void;
};

const ComponentName = 'StudyCameraView';
const { StudyCameraModule } = NativeModules;

const StudyCameraView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<StudyCameraProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
// CameraView Wrapper Component
export class CameraView extends Component<CameraViewProps> {
  appState = AppState.currentState;
  appStateSubscription: any;

  constructor(props: CameraViewProps | Readonly<CameraViewProps>) {
    super(props);
    this.onPoseDetection = this.onPoseDetection.bind(this);
    this.onPhotoCaptured = this.onPhotoCaptured.bind(this);
  }

  appStateHandler = (nextAppState: AppStateStatus) => {
    if (
      this.appState.match(/inactive|background/) &&
      nextAppState === 'active'
    ) {
      StudyCameraModule.resumeCamera();
    } else if (
      this.appState.match(/active/) &&
      nextAppState.match(/inactive|background/)
    ) {
      StudyCameraModule.pauseCamera();
    }

    this.appState = nextAppState;
  };

  render() {
    return (
      <StudyCameraView
        style={this.props.style}
        bodyPart={this.props.bodyPart}
        subFolder={this.props.subFolder}
        detectionMode={this.props.detectionMode || DetectionMode.NONE}
        visualMask={this.props.visualMask || false}
        onCaptured={this.onPhotoCaptured}
        onDetected={this.onPoseDetection}
      />
    );
  }

  onPoseDetection(event: NativeSyntheticEvent<any>) {
    if (this.props.onPoseVerify) {
      const poseData = event.nativeEvent.pose;
      if (poseData) {
        const pose: PoseResult = JSON.parse(poseData);
        const message = verifyPose(
          pose,
          this.props.bodyPart,
          this.props.poseDetectOptions || defaultOptions
        );
        this.props.onPoseVerify(message);
      }
    }
  }

  onPhotoCaptured(event: NativeSyntheticEvent<any>) {
    const imageBase64 = event.nativeEvent.imageBase64;
    this.props.onCaptured(imageBase64);
  }

  componentDidMount() {
    if (this.props.onRef) {
      this.props.onRef(this);
    }
    StudyCameraModule.resumeCamera();

    this.appStateSubscription = AppState.addEventListener(
      'change',
      this.appStateHandler
    );
  }

  componentWillUnmount() {
    StudyCameraModule.pauseCamera();
    if (this.appStateSubscription && this.appStateSubscription.remove) {
      this.appStateSubscription.remove();
    } else {
      AppState.removeEventListener('change', this.appStateHandler);
    }
  }

  //Call capture photo
  capturePhoto() {
    StudyCameraModule.capturePhoto();
  }

  /**
   * Delete Cached Files
   * @param subFolder
   */
  static deleteCacheFiles(subFolder: String) {
    StudyCameraModule.deleteCache(subFolder);
  }

  static getCacheFiles(subFolder: String) {
    return new Promise<any>((resolve, reject) => {
      StudyCameraModule.getCacheFiles(
        subFolder,
        (err: any) => reject(err),
        (data: any) => resolve(JSON.parse(data))
      );
    });
  }
}
