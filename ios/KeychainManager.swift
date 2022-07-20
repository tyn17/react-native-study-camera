//
//  KeychainManager.swift
//  StudyCamera
//
//  Created by Sea Solutions on 20/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation
class KeyChainManager {
    static let instance = KeyChainManager.init()
    
    private init() {
        
    }
    
    // Encrypt data
    func encrypt(data: Data) -> Data {
        // TODO: Encrypt data
        return data
    }
    
    // Decrypt data
    func decrypt(data: Data) -> Data {
        // TODO: Decrypt data
        return data
    }
}
