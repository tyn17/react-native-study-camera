import * as React from 'react';

import { Button, Dimensions, StyleSheet, Text, View } from 'react-native';
import { CameraView, DetectionMode } from 'react-native-study-camera';

export default function App() {
  const [msg, setMsg] = React.useState('Capture');
  const cameraRef = React.useRef<CameraView | null>(null);
  const onCaptured = (imageBase64: String) => {
    console.error('Captured Image with Length: ' + imageBase64.length);
  };
  const captureTop = (Dimensions.get('window').width * 4) / 3 - 50;
  return (
    <View style={styles.container}>
      <CameraView
        onRef={(ref) => (cameraRef.current = ref)}
        style={styles.box}
        bodyPart={0}
        visualMask={true}
        detectionMode={DetectionMode.POSE}
        onCaptured={(imageBase64) => onCaptured(imageBase64)}
        onPoseVerify={(msgKey) => setMsg(msgKey)}
      />
      <View style={[styles.buttons, { top: captureTop }]}>
        <Button
          color="#00f"
          title="Take photo"
          disabled={false}
          onPress={() => {
            cameraRef.current!.capturePhoto();
          }}
        />
      </View>
      <Text style={styles.messages}>{msg}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    alignSelf: 'baseline',
    width: '100%',
    height: '100%',
  },
  buttons: {
    position: 'absolute',
    zIndex: 999,
  },
  messages: {
    position: 'absolute',
    bottom: 50,
  },
});
