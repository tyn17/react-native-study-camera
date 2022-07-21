package com.reactnativestudycamera.encrypt;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeystoreManager {
  private static final int IV_SIZE = 16;
  private static final String KEYSTORE_TYPE = "AndroidKeyStore";
  private static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
  private static final String ENCRYPT_DECRYPT_ALGORITHM = "AES/CBC/PKCS7Padding";
  public static final KeystoreManager instance = new KeystoreManager();
  private KeyStore keyStore;
  KeystoreManager() {
    try {
      initialize();
    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize KeyStore
   * @throws KeyStoreException
   * @throws CertificateException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  private void initialize() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
    keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
    keyStore.load(null);
  }

  /**
   * Generate Random IV
   * @return
   */
  private byte[] generateRandomIV() {
    SecureRandom rnd = new SecureRandom();
    byte[] iv = new byte[IV_SIZE]; // AES block size
    rnd.nextBytes(iv);
    return iv;
  }

  /**
   * Get SecretKey, Create New if Not Exist
   * @param keyAlias
   * @param createIfNotExist
   * @return
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws UnrecoverableEntryException
   * @throws KeyStoreException
   * @throws InvalidAlgorithmParameterException
   */
  @RequiresApi(api = Build.VERSION_CODES.M)
  private SecretKey getKeys(String keyAlias, boolean createIfNotExist) throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException {
    if (!keyStore.containsAlias(keyAlias)) {
      if (createIfNotExist) {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_TYPE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
          .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
          .setRandomizedEncryptionRequired(false).build();
        keyGenerator.init(spec);
        return keyGenerator.generateKey();
      }
      return null;
    }
    KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry)keyStore.getEntry(keyAlias, null);
    return secretKeyEntry.getSecretKey();
  }

  /**
   * Delete Keys
   * @param keyAlias
   * @return
   * @throws KeyStoreException
   */
  public boolean deleteKeys(String keyAlias) {
    try {
      if (keyStore.containsAlias(keyAlias)) {
        keyStore.deleteEntry(keyAlias);
      }
      return true;
    } catch (KeyStoreException kse) {
      Log.e("DELETE KEYS", kse.getMessage(), kse);
      return false;
    }
  }
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

  /**
   * Encrypt Data use Public Key
   * @param keyAlias
   * @param data
   * @return
   * @throws InvalidAlgorithmParameterException
   * @throws UnrecoverableEntryException
   * @throws KeyStoreException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws NoSuchPaddingException
   * @throws InvalidKeyException
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   */
  @RequiresApi(api = Build.VERSION_CODES.M)
  public byte[] encrypt(String keyAlias, byte[] data) throws InvalidAlgorithmParameterException, UnrecoverableEntryException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
    // Get Secret Key
    SecretKey secretKey = getKeys(keyAlias, true);
    //Encrypt
    Cipher cipher = Cipher.getInstance(ENCRYPT_DECRYPT_ALGORITHM);
    byte[] iv = generateRandomIV();
    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
    byte[] encrypted = cipher.doFinal(data);
    //Prepend IV to begin of encrypted data
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    output.write(encrypted);
    output.write(iv);
    encrypted = output.toByteArray();
    output.close();
    return encrypted;
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  public byte[] decrypt(String keyAlias, byte[] encryptedData) throws InvalidAlgorithmParameterException, UnrecoverableEntryException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
    //Get Secret Key
    SecretKey secretKey = getKeys(keyAlias, false);
    if (secretKey != null) {
      //Get IV from 16 bytes of encrypted data
      byte[] iv = Arrays.copyOfRange(encryptedData, encryptedData.length - IV_SIZE, encryptedData.length);
      //Update encrypted data
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      output.write(encryptedData, 0, encryptedData.length - IV_SIZE);
      byte[] data = output.toByteArray();
      output.close();

      //Decrypt
      IvParameterSpec ivSpec = new IvParameterSpec(iv);
      Cipher cipher = Cipher.getInstance(ENCRYPT_DECRYPT_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
      return cipher.doFinal(data);
    }
    return encryptedData;
  }
}
