@available(iOS 11.0, *)
@objc(StudyCameraViewManager)
class StudyCameraViewManager: RCTViewManager {
    var _view: StudyCameraView!
    override func view() -> (StudyCameraView) {
        _view = StudyCameraView()
        return _view
    }
    
    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc(resumeCamera)
    func resumeCamera() -> Void {
        self._view.resumeCamera()
        print("Manager Resume Camera")
    }
    
    @objc(pauseCamera)
    func pauseCamera() -> Void {
        self._view.pauseCamera()
        print("Manager Pause Camera")
    }
    
    @objc(capturePhoto)
    func capturePhoto() -> Void {
        self._view.takePhoto()
        print("Manager Capture Photo")
    }
}
