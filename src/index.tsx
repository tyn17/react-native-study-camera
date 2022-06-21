import React, { Component } from 'react';
import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  NativeSyntheticEvent,
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

const ComponentName = 'StudyCameraView';
const { StudyCameraModule } = NativeModules;

const StudyCameraView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<StudyCameraProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
// CameraView Wrapper Component
export class CameraView extends Component<StudyCameraProps> {
  render() {
    return <StudyCameraView {...this.props} />;
  }

  componentDidMount() {
    StudyCameraModule.resumeCamera();
  }
  componentWillUnmount() {
    StudyCameraModule.pauseCamera();
  }
}
