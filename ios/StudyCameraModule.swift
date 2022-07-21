//
//  StudyCameraModule.swift
//  StudyCamera
//
//  Created by Sea Solutions on 12/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

@objc(StudyCameraModule)
class StudyCameraModule: NSObject {
    
    @objc(deleteCaches:)
    func deleteCaches(_ subFolder: String?) -> Void {
        do {
            if let folder = subFolder {
                try Utils.deleteCaches(folder)
            } else {
                try Utils.deleteAllCaches()
            }
        } catch let err {
            print(err)
        }
    }
    
    @objc(getCachedFile:bodyPart:isThumb:resolver:rejecter:)
    func getCachedFile(_ subFolder: String, _ bodyPart: Int, _ isThumb: Bool, _ resolver: RCTPromiseResolveBlock, _ rejecter: RCTPromiseRejectBlock) {
        do {
            if let result = try Utils.getCachedFile(subFolder, bodyPart, isThumb) {
                resolver(result)
            } else {
                rejecter("getCachedFile", "File Not Found", nil)
            }
        } catch let err {
            rejecter("getCachedFile", "\(err)", nil)
        }
    }
    
    @objc(getCachedFilePath:bodyPart:isThumb:resolver:rejecter:)
    func getCachedFilePath(_ subFolder: String, _ bodyPart: Int, _ isThumb: Bool, _ resolver: RCTPromiseResolveBlock, _ rejecter: RCTPromiseRejectBlock) {
        do {
            if let result = try Utils.getCachedFilePath(subFolder, bodyPart, isThumb) {
                resolver(result)
            } else {
                rejecter("getCachedFilePath", "File Not Found", nil)
            }
        } catch let err {
            rejecter("getCachedFilePath", "\(err)", nil)
        }
    }
    
    @objc(hasCachedFiles:resolver:rejecter:)
    func hasCachedFiles(_ onlyCheckOrigin: Bool, _ resolver: RCTPromiseResolveBlock, _ rejecter: RCTPromiseRejectBlock) -> Void {
        resolver(false)
        do {
            let result = try Utils.hasCachedFiles(onlyCheckOrigin)
            resolver(result)
        } catch let err {
            rejecter("hasCachedFiles", "\(err)", nil)
        }
    }
    
    @objc class func requiresMainQueueSetup() -> Bool {
        return true
    }
}
