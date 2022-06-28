# react-native-study-camera

The package to open Camera for Belle Study application

## Installation

```sh
npm install react-native-study-camera
```
Or in package.json
```js
"dependencies": {
	...
	"react-native-study-camera": "git+https://github.com/tyn17/react-native-study-camera.git#move-net-pose-estimate"
}
```

## Android
Add this to AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-feature android:name="android.hardware.camera2.full"/>
```

## Usage

```js
import { CameraView } from 'react-native-study-camera';

// ...
const handleCapturedPhoto = (imageBase64: String) => {
    // ...
};
<CameraView
        onRef={(ref) => ...}
        style={...}
        bodyPart={0}
        visualMask={true}
        detectionMode={DetectionMode.POSE}
        onCaptured={(imageBase64) => onCaptured(imageBase64)}
        onPoseVerify={(msgKey) => console.log(msgKey)}
      />
```
**bodyPart**: 0, 1, 2, 3  
**visualMask**: true - display Pose Lines; false - hide Pose Lines  
**detectionMode**: POSE/NONE  
**onPoseVerify**: Message Key of validation POSE position. Only DetectionMode = POSE  

## Call Native Methods
```js
CameraView.deleteCacheFiles("subFolderName")

CameraView.getCacheFiles("subFolderName").then((data) => ...)
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
