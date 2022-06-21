import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { StudyCameraView } from 'react-native-study-camera';

export default function App() {
  const onCaptured = (imageBase64: String) => {
    console.log(imageBase64);
  };
  return (
    <View style={styles.container}>
      <StudyCameraView
        style={styles.box}
        bodyPart={1}
        onCaptured={(event) => onCaptured(event.nativeEvent.imageBase64)}
      />
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
    width: '100%',
    height: '100%',
  },
});
