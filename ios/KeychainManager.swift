//
//  KeychainManager.swift
//  StudyCamera
//
//  Created by Sea Solutions on 20/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation
class KeyChainManager {
    private let ENCRYPT_ALGORITHM = SecKeyAlgorithm.eciesEncryptionStandardX963SHA512AESGCM
    static let instance = KeyChainManager.init()
    
    private init() {
        
    }
    
    // Get Private Key from KeyChain, Create a Random Key if need
    func getPrivateKey(keyTag: String, createIfNotExist: Bool) throws -> SecKey? {
        let getQuery = [
            kSecClass as String: kSecClassKey,
            kSecAttrKeyType as String: kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeySizeInBits as String: 256,
            kSecAttrApplicationTag as String: keyTag,
            kSecReturnRef as String: true
        ] as CFDictionary
        var result: CFTypeRef?
        let status = SecItemCopyMatching(getQuery, &result)
        if status == errSecSuccess, let privateKey = result {
            return privateKey as! SecKey
        }
        //Create new if not exist
        if createIfNotExist {
            //Generate a Random Private Key
            let addQuery = [
                kSecClass as String: kSecClassKey,
                kSecAttrKeyType as String : kSecAttrKeyTypeECSECPrimeRandom,
                kSecAttrKeySizeInBits as String: 256,
                kSecPrivateKeyAttrs as String: [
                    kSecAttrIsPermanent as String: true
                ],
                kSecAttrApplicationTag as String: keyTag,
                kSecReturnRef as String: true
            ] as CFDictionary
            var error: Unmanaged<CFError>?
            guard let privateKey = SecKeyCreateRandomKey(addQuery, &error) else {
                throw error!.takeRetainedValue() as Error
            }
            return privateKey
        }
        return nil
    }
    
    // Delete Keys
    func deleteKeys(keyTag: String) -> Bool {
        let removeQuery = [
            kSecClass as String: kSecClassKey,
            kSecAttrApplicationTag as String: keyTag
        ] as CFDictionary
        let status = SecItemDelete(removeQuery)
        if status == errSecSuccess {
            return true
        }
        print("Delete Key Error: \(status)")
        return false
    }
    
    // Encrypt data
    func encrypt(keyTag: String, data: Data) throws -> Data {
        if let key = try getPrivateKey(keyTag: keyTag, createIfNotExist: true), let publicKey = SecKeyCopyPublicKey(key) {
            var error: Unmanaged<CFError>?
            guard let encrypted = SecKeyCreateEncryptedData(publicKey, ENCRYPT_ALGORITHM, data as CFData, &error) else {
                throw error!.takeRetainedValue() as Error
            }
            return encrypted as Data
        }
        throw NSError(domain: "Encrypt", code: 0, userInfo: ["Error": "Cannot generate key to encrypt"])
    }
    
    // Decrypt data
    func decrypt(keyTag: String, data: Data) throws -> Data {
        if let key = try getPrivateKey(keyTag: keyTag, createIfNotExist: false) {
            var error: Unmanaged<CFError>?
            guard let plainData = SecKeyCreateDecryptedData(key, ENCRYPT_ALGORITHM, data as CFData, &error) else {
                throw error!.takeRetainedValue() as Error
            }
            return plainData as Data
        }
        throw NSError(domain: "Decrypt", code: 0, userInfo: ["Error": "Cannot get key to decrypt"])
    }
}
