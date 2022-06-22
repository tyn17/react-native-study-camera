import React, { Component } from 'react';
import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  NativeSyntheticEvent,
  AppState,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-study-camera' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

type StudyCameraProps = {
  bodyPart: number;
  style: ViewStyle;
  onCaptured: (event: NativeSyntheticEvent<any>) => void;
};

type CameraViewProps = {
  bodyPart: number;
  style: ViewStyle;
  onRef?: (ref: CameraView) => void;
  onCaptured: (event: NativeSyntheticEvent<any>) => void;
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

  render() {
    return (
      <StudyCameraView
        style={this.props.style}
        bodyPart={this.props.bodyPart}
        onCaptured={this.props.onCaptured}
      />
    );
  }

  componentDidMount() {
    if (this.props.onRef) {
      this.props.onRef(this);
    }
    StudyCameraModule.resumeCamera();

    this.appStateSubscription = AppState.addEventListener(
      'change',
      (nextAppState) => {
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
      }
    );
  }
  componentWillUnmount() {
    StudyCameraModule.pauseCamera();
    this.appStateSubscription.remove();
  }

  //Call capture photo
  capturePhoto() {
    StudyCameraModule.capturePhoto();
  }
}
