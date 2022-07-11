package com.reactnativestudycamera.encrypt;

public class EncryptedModel {
    private String encryptedKeyBase64;
    private byte[] encryptedData;

    public EncryptedModel(byte[] encryptedData, String encryptedKeyBase64) {
        this.encryptedKeyBase64 = encryptedKeyBase64;
        this.encryptedData = encryptedData;
    }

    public String getEncryptedKeyBase64() {
        return encryptedKeyBase64;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }
}
