import * as React from 'react';

import {
  Button,
  Dimensions,
  Image,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { CameraView, DetectionMode } from 'react-native-study-camera';

export default function App() {
  const subjectId = 'test01';
  const [bodyParts, setBodyParts] = React.useState([
    { image: '', label: 'Upper Front' },
    { image: '', label: 'Upper Back' },
    { image: '', label: 'Lower Front' },
    { image: '', label: 'Lower Back' },
  ]);
  const [bodyPart, setBodyPart] = React.useState(0);
  const [msg, setMsg] = React.useState('Message here');
  const cameraRef = React.useRef<CameraView | null>(null);
  const onCaptured = (imageBase64: String) => {
    bodyParts[bodyPart].image = `data:image/png;base64, ${imageBase64}`;
    setBodyPart((bodyPart + 1) % bodyParts.length);
    setBodyParts(bodyParts);
  };
  const reloadCaches = () => {
    bodyParts.forEach((bp, index) => {
      var count = 0;
      CameraView.getCachedFile(subjectId, index, true)
        .then((base64: String) => {
          console.log(base64);
          count++;
          bp.image = base64 ? `data:image/png;base64, ${base64}` : '';
          if (count === 4) {
            setBodyParts(bodyParts);
            setBodyPart(0);
          }
        })
        .catch((err) => {
          console.log(err);
          bp.image = '';
          count++;
          if (count === 4) {
            setBodyParts(bodyParts);
            setBodyPart(0);
          }
        });
    });
  };

  React.useEffect(() => {
    reloadCaches();
  }, []);

  const captureTop = (Dimensions.get('window').width * 4) / 3 - 150;
  return (
    <View style={styles.container}>
      <CameraView
        onRef={(ref) => (cameraRef.current = ref)}
        style={styles.box}
        bodyPart={bodyPart}
        visualMask={true}
        detectionMode={DetectionMode.POSE}
        subFolder={subjectId}
        onCaptured={(imageBase64) => onCaptured(imageBase64)}
        onPoseVerify={(msgKey) => setMsg(msgKey)}
      />
      <View style={[styles.buttons, { top: captureTop }]}>
        <Button
          color="#00f"
          title="Capture"
          disabled={false}
          onPress={() => {
            cameraRef.current!.capturePhoto();
          }}
        />
      </View>
      <View style={styles.bottomContainer}>
        <Text>{bodyParts[bodyPart].label.toUpperCase()}</Text>
        <Text>{msg}</Text>
        <View style={styles.buttonContainer}>
          {bodyParts.map((bp, index) => {
            return (
              <TouchableOpacity
                key={`bp-${index}`}
                style={
                  bodyPart === index
                    ? [styles.button, styles.selected]
                    : [styles.button, styles.unselected]
                }
                onPress={() => setBodyPart(index)}
              >
                <Text>{bp.label}</Text>
              </TouchableOpacity>
            );
          })}
        </View>
        <View style={styles.imageContainer}>
          {bodyParts.map((bp, index) => {
            return (
              bp.image.length > 0 && (
                <Image
                  key={`img-${index}`}
                  style={styles.imageStyle}
                  source={{ uri: bp.image }}
                />
              )
            );
          })}
        </View>
        <Button
          title="Delete cache"
          onPress={() => {
            CameraView.deleteCachedFiles(subjectId);
            reloadCaches();
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
  bottomContainer: {
    position: 'absolute',
    bottom: 50,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonContainer: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    margin: 10,
  },
  imageContainer: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    margin: 10,
  },
  imageStyle: {
    width: 100,
    height: 100,
  },
  button: {
    borderRadius: 20,
    padding: 10,
  },
  unselected: {
    backgroundColor: '#aaa',
  },
  selected: {
    backgroundColor: 'green',
  },
});
