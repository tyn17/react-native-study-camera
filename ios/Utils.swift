//
//  Utils.swift
//  StudyCamera
//
//  Created by Sea Solutions on 15/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

class Utils {
    // MARK: Constants
    private static let ENCRYPT_IMAGE = true
    private static let KEY_TAG = "com.belle.torus.study"
    private static let IMAGE_EXTENSION = ENCRYPT_IMAGE ? ".EJPG" : ".JPG"
    private static let THUMB_EXTENSION = ENCRYPT_IMAGE ? "_thumbnail.EJPG" : "_thumbnail.JPG"
    
    // MARK: Primate Methods
    // Get Root Folder
    private static func getRootFolder() throws -> URL {
        let docURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("files", isDirectory: true)
        var isDir: ObjCBool = false
        if !FileManager.default.fileExists(atPath: docURL.path, isDirectory: &isDir) || !isDir.boolValue {
            try FileManager.default.createDirectory(at: docURL, withIntermediateDirectories: true, attributes: nil)
        }
        return docURL
    }
    
    // Get File URL
    private static func getFileURL(subFolder: String, fileName: String, deleteIfExist: Bool) throws -> URL {
        let root = try getRootFolder()
        // Create SubFolder
        let folderURL = root.appendingPathComponent(subFolder, isDirectory: true)
        var isDir: ObjCBool = false
        if !FileManager.default.fileExists(atPath: folderURL.path, isDirectory: &isDir) || !isDir.boolValue {
            try FileManager.default.createDirectory(at: folderURL, withIntermediateDirectories: true, attributes: nil)
        }
        let fileURL = folderURL.appendingPathComponent(fileName)
        // Delete File if exists
        if deleteIfExist && FileManager.default.fileExists(atPath: fileURL.path, isDirectory: &isDir) && !isDir.boolValue {
            try FileManager.default.removeItem(at: fileURL)
        }
        print(fileURL)
        return fileURL
    }
    
    // Delete Recursive Folder content
    private static func deleteDirector(folderURL: URL, deleteItSelf: Bool) throws {
        let files = try FileManager.default.contentsOfDirectory(atPath: folderURL.path)
        if !files.isEmpty {
            for file in files {
                let fileURL = folderURL.appendingPathComponent(file)
                var isDir: ObjCBool = false
                if FileManager.default.fileExists(atPath: fileURL.path, isDirectory: &isDir) {
                    if isDir.boolValue {
                        try deleteDirector(folderURL: fileURL, deleteItSelf: true)
                    } else {
                        try FileManager.default.removeItem(atPath: fileURL.path)
                    }
                }
            }
        }
        if deleteItSelf {
            try FileManager.default.removeItem(atPath: folderURL.path)
        }
    }
    
    // MARK: Public Methods
    // Save data to file
    static func save(subFolder: String, fileName: String, data: Data, withThumb: Bool) throws -> Data {
        var result = data
        let encryptedData = ENCRYPT_IMAGE ? try KeyChainManager.instance.encrypt(keyTag: KEY_TAG, data: data) : data
        let imageName = "\(fileName)\(IMAGE_EXTENSION)"
        let imageURL = try getFileURL(subFolder: subFolder, fileName: imageName, deleteIfExist: true)
        try encryptedData.write(to: imageURL)
        // Create Thumb if need
        if withThumb {
            if let thumbData = data.resizeImage(target: 512.0) {
                let thumbName = "\(fileName)\(THUMB_EXTENSION)"
                let thumbURL = try getFileURL(subFolder: subFolder, fileName: thumbName, deleteIfExist: true)
                let encryptedThumbData = ENCRYPT_IMAGE ? try KeyChainManager.instance.encrypt(keyTag: KEY_TAG, data: thumbData) : thumbData
                try encryptedThumbData.write(to: thumbURL)
                result = thumbData
            }
        }
        return result
    }
    
    // Delete Caches in Folder
    static func deleteCaches(_ subFolder: String) throws -> Void {
        let folderURL = try getRootFolder().appendingPathComponent(subFolder, isDirectory: true)
        var isDir: ObjCBool = false
        if FileManager.default.fileExists(atPath: folderURL.path, isDirectory: &isDir) && isDir.boolValue {
            try deleteDirector(folderURL: folderURL, deleteItSelf: false)
        }
    }
    
    // Delete All Caches
    static func deleteAllCaches() throws -> Void {
        let rootURL = try getRootFolder()
        try deleteDirector(folderURL: rootURL, deleteItSelf: false)
        _ = KeyChainManager.instance.deleteKeys(keyTag: KEY_TAG)
    }
    
    // Get Cached File. Return String Base64
    static func getCachedFile(_ subFolder: String, _ bodyPart: Int, _ isThumb: Bool) throws -> String? {
        if let filePath = try getCachedFilePath(subFolder, bodyPart, isThumb) {
            print(filePath)
            if let data = FileManager.default.contents(atPath: filePath) {
                if ENCRYPT_IMAGE {
                    let decryptedData = try KeyChainManager.instance.decrypt(keyTag: KEY_TAG, data: data)
                    return decryptedData.base64EncodedString()
                } else {
                    return data.base64EncodedString()
                }
            }
        }
        return nil
    }
    
    // Get Cached File Path. Return File Path
    static func getCachedFilePath(_ subFolder: String, _ bodyPart: Int, _ isThumb: Bool) throws -> String? {
        let rootURL = try getRootFolder()
        let ext = isThumb ? THUMB_EXTENSION : IMAGE_EXTENSION
        let filePath = rootURL.appendingPathComponent(subFolder, isDirectory: true)
            .appendingPathComponent("\(bodyPart)\(ext)").path
        var isDir: ObjCBool = false
        if FileManager.default.fileExists(atPath: filePath, isDirectory: &isDir) && !isDir.boolValue {
            return filePath
        }
        return nil
    }
    
    // Check Has Cached Files
    static func hasCachedFiles(_ onlyCheckOrigin: Bool) throws -> Bool {
        let rootURL = try getRootFolder()
        let rootPath = rootURL.path
        // List subFolder
        let subFolders = try FileManager.default.contentsOfDirectory(atPath: rootPath)
        if !subFolders.isEmpty {
            for subFolder in subFolders {
                let subFolderURL = rootURL.appendingPathComponent(subFolder, isDirectory: true)
                var isSubFolder: ObjCBool = false
                if FileManager.default.fileExists(atPath: subFolderURL.path, isDirectory: &isSubFolder) && isSubFolder.boolValue {
                    let files = try FileManager.default.contentsOfDirectory(atPath: subFolderURL.path)
                    for file in files {
                        let filePath = subFolderURL.appendingPathComponent(file).path
                        var isDir: ObjCBool = false
                        if FileManager.default.fileExists(atPath: filePath, isDirectory: &isDir) && !isDir.boolValue {
                            if !onlyCheckOrigin && file.hasSuffix(IMAGE_EXTENSION) {
                                return true
                            }
                            if onlyCheckOrigin && file.hasSuffix(IMAGE_EXTENSION) && !file.hasSuffix(THUMB_EXTENSION) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}


// MARK: Extension for Resizing Image
extension Data {
    func resizeImage(target: CGFloat) -> Data? {
        if let image = UIImage(data: self) {
            let scale = target / Swift.min(image.size.width, image.size.height)
            let targetWidth = image.size.width * scale
            let targetHeight = image.size.height * scale
            let rect = CGRect(x: .zero, y: .zero, width: targetWidth, height: targetHeight)
            
            // Resize
            UIGraphicsBeginImageContextWithOptions(rect.size, false, 1.0)
            image.draw(in: rect)
            let newImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            // Convert to Data
            return newImage?.jpegData(compressionQuality: 1)
        }
        return nil
    }
}
