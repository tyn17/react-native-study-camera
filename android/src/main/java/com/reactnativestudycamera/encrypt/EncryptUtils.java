package com.reactnativestudycamera.encrypt;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class EncryptUtils {
    private static final int SALT_SIZE = 32;
    private static final int IV_SIZE = 16;
    private static final String SEPARATED = "###SALT_IV###";
    private static final String SECRET_KEY = "%yEuYNhwCwKzx@eMM8m*&wbp5hS%#qvC";
    private static final byte[] SALT = new byte[] {0x69,0x42,0x30,0x5d,0x44,0x4c,0x61,0x2c,0x79,0x31,0x69,0x5e,0x62,0x63,0x37,0x5c,0x50,0x55,0x4f,0x3d,0x57,0x57,0x5c,0x56,0x2e,0x5c,0x76,0x65,0x46,0x3d,0x3f,0x58}; //32 bytes
    private static final byte[] IV = new byte[] {0x43,0x58,0x41,0x7a,0x7e,0x40,0x5f,0x56,0x35,0x34,0x37,0x41,0x4f,0x2c,0x2c,0x22}; //16 bytes

    private static final int PBK_ITERATIONS = 1000;
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String PBE_ALGORITHM = "PBEwithSHA256and128BITAES-CBC-BC";
    /**
     * Convert Bytes to Base64 String
     * @param bytes
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String toBase64(byte[] bytes) {
        byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Encrypt data by AES256
     * @param plainData
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static EncryptedModel encrypt(byte[] plainData) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Generate random Salt and IV
        SecureRandom rnd = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        byte[] iv = new byte[IV_SIZE]; // AES block size
        rnd.nextBytes(salt);
        rnd.nextBytes(iv);
        // Encrypt data
        PBEKeySpec keySpec = new PBEKeySpec(SECRET_KEY.toCharArray(), salt, PBK_ITERATIONS);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        Key key = secretKeyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedData = cipher.doFinal(plainData);
        // Encrypt Keys
        String encryptedKey = encryptKeys(new KeyModel(salt, iv));
        return new EncryptedModel(encryptedData, encryptedKey);
    }

    /**
     * Decrypt data by AES256
     * @param encryptedData
     * @param encryptedKeyBase64
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] decrypt(byte[] encryptedData, String encryptedKeyBase64) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        KeyModel extractedKeys = extractKeys(encryptedKeyBase64);
        return decryptData(encryptedData, extractedKeys.salt, extractedKeys.iv);
    }



    /**
     * Encrypt Keys with static Password
     * @param keyModel
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String encryptKeys(KeyModel keyModel) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String combinedKey = toBase64(keyModel.salt) + SEPARATED + toBase64(keyModel.iv);

        PBEKeySpec keySpec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT, PBK_ITERATIONS);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        Key key = secretKeyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(combinedKey.getBytes(StandardCharsets.UTF_8));
        return toBase64(encrypted);
    }

    /**
     * Extract EncryptedKeyBase64 to Salt and IV
     * @param encryptedBase64
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static KeyModel extractKeys(String encryptedBase64) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] raw = fromBase64(encryptedBase64);
        byte[] plain = decryptData(raw, SALT, IV);
        String combinedKey = new String(plain, StandardCharsets.UTF_8);
        String[] keys = combinedKey.split(SEPARATED);
        return new KeyModel(fromBase64(keys[0]), fromBase64(keys[1]));
    }

    /**
     * Decrypt data with Salt and IV
     * @param encryptedData
     * @param salt
     * @param iv
     * @return
     */
    private static byte[] decryptData(byte[] encryptedData, byte[] salt, byte[] iv) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PBEKeySpec keySpec = new PBEKeySpec(SECRET_KEY.toCharArray(), salt, PBK_ITERATIONS);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        Key key = secretKeyFactory.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(encryptedData);
    }

    private static class KeyModel {
        private byte[] salt;
        private byte[] iv;

        public KeyModel(byte[] salt, byte[] iv) {
            this.salt = salt;
            this.iv = iv;
        }
    }
}
