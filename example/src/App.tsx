import * as React from 'react';

import { Button, Dimensions, StyleSheet, View } from 'react-native';
import { CameraView } from 'react-native-study-camera';

export default function App() {
  const cameraRef = React.useRef<CameraView>(null);
  const onCaptured = (imageBase64: String) => {
    console.error(imageBase64);
  };
  const captureTop = (Dimensions.get('window').width * 4) / 3 - 50;
  return (
    <View style={styles.container}>
      <CameraView
        onRef={(ref) => (cameraRef.current = ref)}
        style={styles.box}
        bodyPart={-1}
        onCaptured={(event) => onCaptured(event.nativeEvent.imageBase64)}
      />
      <View style={[styles.buttons, { top: captureTop }]}>
        <Button
          color="#00f"
          title="Capture"
          disabled={false}
          onPress={() => {
            cameraRef.current.capturePhoto();
          }}
        />
      </View>
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
});
