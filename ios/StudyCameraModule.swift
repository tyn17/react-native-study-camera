//
//  StudyCameraModule.swift
//  StudyCamera
//
//  Created by Sea Solutions on 12/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

@objc(StudyCameraModule)
class StudyCameraModule: NSObject {
    @objc(resumeCamera)
    func resumeCamera() -> Void {
        print("Resume Camera")
    }
    
    @objc(pauseCamera)
    func pauseCamera() -> Void {
        print("Pause Camera")
    }
    
    @objc(capturePhoto)
    func capturePhoto() -> Void {
        print("Capture Photo")
    }
    
    @objc(deleteCaches:)
    func deleteCaches(_ subFolder: String) -> Void {
        print("Delete Caches")
    }
    
    @objc(getCachedFile:bodyPart:isThumb:resolver:rejecter:)
    func getCachedFile(_ subFolder: String, _ bodyPart: Int, _ isThumb: Bool, _ resolver: RCTPromiseResolveBlock, _ rejecter: RCTPromiseRejectBlock) {
        resolver("Getted Cached File " + subFolder)
    }
    
    @objc(getCachedFilePath:bodyPart:isThumb:resolver:rejecter:)
    func getCachedFilePath(_ subFolder: String, _ bodyPart: Int, _ isThumb: Bool, _ resolver: RCTPromiseResolveBlock, _ rejecter: RCTPromiseRejectBlock) {
        rejecter("Getted Cached File Path", "Test failed", nil)
    }
    
    @objc(hasCachedFiles:resolver:rejecter:)
    func hasCachedFiles(_ onlyCheckOrigin: Bool, _ resolver: RCTPromiseResolveBlock, _ rejecter: RCTPromiseRejectBlock) -> Void {
        resolver(false)
    }
}
