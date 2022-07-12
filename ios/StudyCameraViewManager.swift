@objc(StudyCameraViewManager)
class StudyCameraViewManager: RCTViewManager {

    override func view() -> (StudyCameraView) {
        return StudyCameraView()
    }
}

class StudyCameraView : UIView {
//    @objc var bodyPart: Int = 1
//    @objc var subFolder: String = ""
//    @objc var visualMask: Bool = true
//    @objc var detectionMode: Int = DetectionMode.NONE.rawValue
//    @objc var usePortraitScene: Bool = false
//    @objc var useBackCamera: Bool = true
//    @objc var onCaptured: RCTBubblingEventBlock? = nil
//    @objc var onDetected: RCTBubblingEventBlock? = nil

//    @objc var color: String = "" {
//        didSet {
//          self.backgroundColor = hexStringToUIColor(hexColor: color)
//        }
//    }
}
