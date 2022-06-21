# react-native-study-camera

The package to open Camera for Belle Study application

## Installation

```sh
npm install react-native-study-camera
```
Or in package.json
```js
"devDependencies": {
	...
	"react-native-study-camera": "git+https://github.com/tyn17/react-native-study-camera.git"
}
```
## Usage

```js
import { StudyCameraView } from "react-native-study-camera";

// ...
const handleCapturedPhoto = (imageBase64: String) => {
    // ...
};
<StudyCameraView style={...} bodyPart={2} onCaptured={(event) => handleCapturedPhoto(event.nativeEvent.imageBase64) />
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
