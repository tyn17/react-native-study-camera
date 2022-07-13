import SwiftUI
@objc(StudyCameraViewManager)
class StudyCameraViewManager: RCTViewManager {

    override func view() -> (StudyCameraView) {
        return StudyCameraView()
    }
}
