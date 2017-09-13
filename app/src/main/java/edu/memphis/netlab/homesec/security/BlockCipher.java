package edu.memphis.netlab.homesec.security;

import com.google.common.base.Joiner;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import edu.memphis.netlab.homesec.util.StringHelper;

/**
 * Description:
 * Wrapper class for symmetric cryptography using AES + CBC
 * Instance is _not_ thread-safe.
 * Date: 3/8/17
 * Author: lei
 */

public class BlockCipher {

  public static class EncryptionError extends Exception {

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public EncryptionError(String detailMessage) {
      super(detailMessage);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public EncryptionError(Throwable throwable) {
      super(throwable);
    }

    /**
     * Constructs a new {@code RuntimeException} that includes the current stack
     * trace.
     */
    public EncryptionError() {
      super();
    }
  }

  public static class DecryptionError extends Exception {
    /**
     * Constructs a new {@code RuntimeException} that includes the current stack
     * trace.
     */
    public DecryptionError() {
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public DecryptionError(String detailMessage) {
      super(detailMessage);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public DecryptionError(Throwable throwable) {
      super(throwable);
    }
  }

  /**
   * @param keySize Desired key strength
   * @param key     Initial key. If key length is less than desired
   *                key strength, the key will be transformed in the
   *                following way.
   *                1) key is hashed into 32 bytes.
   *                2) use 0: keySize-1 of the hash as the new key.
   */
  public BlockCipher(final int keySize, byte[] key) {
    if (keySize != KEY_SIZE_128 &&
        keySize != KEY_SIZE_172 &&
        keySize != KEY_SIZE_256) {
      throw new RuntimeException(BlockCipher.class.getName() +
          ": keySize can only be " +
          Joiner.on(',').join(new Integer[]{
              KEY_SIZE_128,
              KEY_SIZE_172,
              KEY_SIZE_256
          }) + " got: " + keySize);
    }

    key = matchKeySize(key, keySize);
    this.m_keySize = keySize;
    this.m_key = new SecretKeySpec(key, "AES");
    this.m_zeroIv = new IvParameterSpec(new byte[keySize]);
    initCipher();
  }

  public byte[] encrypt(byte[] plainText) throws EncryptionError {
    IvParameterSpec iv = generateIv();
    byte[] cipherText = encrypt(plainText, iv);
    byte[] result = new byte[cipherText.length + iv.getIV().length];
    System.arraycopy(cipherText, 0, result, 0, cipherText.length);
    System.arraycopy(iv.getIV(), 0, result, cipherText.length, iv.getIV().length);
    return result;
  }

  public byte[] encrypt(byte[] plainText, byte[] iv) throws EncryptionError {
    return encrypt(plainText, new IvParameterSpec(iv));
  }

  public byte[] encrypt(byte[] plainText, IvParameterSpec iv) throws EncryptionError {
    if (iv.getIV().length != m_keySize) {
      throw new EncryptionError(String.format(Locale.ENGLISH,
          "IV size does not match key size. %d : %d", iv.getIV().length, m_keySize));
    }
    try {
      m_cipher.init(Cipher.ENCRYPT_MODE, m_key, iv);
      _logger.info("encryption key: " + StringHelper.toHex(m_key.getEncoded())
          + " iv: " + StringHelper.toHex(iv.getIV()));
    } catch (InvalidKeyException
        | InvalidAlgorithmParameterException ignore) {
      throw new EncryptionError(ignore);
    }
    try {
      return m_cipher.doFinal(plainText);
    } catch (IllegalBlockSizeException
        | BadPaddingException e) {
      throw new EncryptionError(e);
    }
  }

  public byte[] decrypt(byte[] cipherText) throws DecryptionError {
    IvParameterSpec iv = extractIv(cipherText);
    byte[] cipher = Arrays.copyOfRange(cipherText, 0, cipherText.length - m_keySize);
    return decrypt(cipher, iv);
  }

  public byte[] decrypt(byte[] cipherText, byte[] iv) throws DecryptionError {
    return decrypt(cipherText, new IvParameterSpec(iv));
  }

  public byte[] decrypt(byte[] cipherText, IvParameterSpec iv) throws DecryptionError {
    try {
      m_cipher.init(Cipher.DECRYPT_MODE, m_key, iv);
    } catch (InvalidKeyException | InvalidAlgorithmParameterException ignore) {
      throw new DecryptionError(ignore);
    }
    try {
      return m_cipher.doFinal(cipherText);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      e.printStackTrace();
      throw new DecryptionError(e);
    }
  }

  public KeySpec getKey() {
    return m_key;
  }

  private static byte[] matchKeySize(byte[] key, int size) {
    byte[] k = StringHelper.sha256(key);
    return Arrays.copyOf(k, size);
  }

  private void initCipher() {
    try {
      m_cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException ignore) {
      throw new RuntimeException(ignore);
    }
  }

  private IvParameterSpec generateIv() throws EncryptionError {
    try {
      m_cipher.init(Cipher.ENCRYPT_MODE, m_key, m_zeroIv);
      byte[] r = new byte[m_cipher.getBlockSize()];
      new SecureRandom().nextBytes(r);
      return new IvParameterSpec(r, 0, m_keySize);
    } catch (InvalidKeyException
        | InvalidAlgorithmParameterException e) {
      throw new EncryptionError(e);
    }
  }

  private IvParameterSpec extractIv(byte[] cipherText) {
    byte[] ivBuf = new byte[m_keySize];
    System.arraycopy(cipherText, cipherText.length - m_keySize, ivBuf, 0, m_keySize);
    return new IvParameterSpec(ivBuf);
  }

  private static final int BYTE_SIZE = 8;

  public static final int KEY_SIZE_128 = 128 / BYTE_SIZE;
  public static final int KEY_SIZE_172 = 172 / BYTE_SIZE;
  public static final int KEY_SIZE_256 = 256 / BYTE_SIZE;

  private final IvParameterSpec m_zeroIv;
  private final int m_keySize;
  private SecretKeySpec m_key;
  private Cipher m_cipher;
  private Logger _logger = Logger.getLogger(BlockCipher.class.getName());
}
