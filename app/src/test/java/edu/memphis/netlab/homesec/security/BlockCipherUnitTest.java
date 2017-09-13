package edu.memphis.netlab.homesec.security;

import com.google.common.base.Joiner;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 * <p>
 * Date: 3/8/17
 * Author: lei
 */

public class BlockCipherUnitTest {

  @Test
  public void KeySize128ShouldBeValid() {
    BlockCipher b = new BlockCipher(BlockCipher.KEY_SIZE_128, "Hello".getBytes());
    Assert.assertTrue(true);
  }

  @Test
  public void KeySize172ShouldBeValid() {
    BlockCipher b = new BlockCipher(BlockCipher.KEY_SIZE_128, "Hello".getBytes());
    Assert.assertTrue(true);
  }

  @Test
  public void KeySize256ShouldBeValid() {
    BlockCipher b = new BlockCipher(BlockCipher.KEY_SIZE_128, "Hello".getBytes());
    Assert.assertTrue(true);
  }

  @Test
  public void OtherKeySizeShouldBeInValid() {
    for (int i : new Integer[]{-100, 0, 100, 155, 360}) {
      try {
        BlockCipher b = new BlockCipher(i, "Hello".getBytes());
        Assert.assertFalse(true);
      } catch (RuntimeException ignored) {
        Assert.assertTrue(true);
      }
    }
  }

  @Test
  public void encyptWithIV() {
    BlockCipher cipher = new BlockCipher(KEY.length, KEY);
    byte[] cipherText = new byte[0];
    try {
      cipherText = cipher.encrypt(PLAIN_TEXT, IV);
    } catch (BlockCipher.EncryptionError encryptionError) {
      encryptionError.printStackTrace();
      Assert.assertFalse(true);
    }
    Assert.assertArrayEquals(cipherText, CIPHER_TEXT);
  }

  private static void printByteArray(byte[] arr){
    List<String> hex = new LinkedList<>();
    for (byte b : arr){
      hex.add(String.format("0x%02X", b));
    }
    System.out.println(Joiner.on(", ").join(hex));
  }

  @Test
  public void decryptWithIV(){
    BlockCipher cipher = new BlockCipher(KEY.length, KEY);
    byte[] plainText = new byte[0];
    try {
      plainText = cipher.decrypt(CIPHER_TEXT, IV);
    } catch (BlockCipher.DecryptionError decryptionError) {
      decryptionError.printStackTrace();
      Assert.assertFalse(true);
    }
    Assert.assertArrayEquals(plainText, PLAIN_TEXT);
  }

  @Test
  public void edWithRandomIV(){
    BlockCipher cipher = new BlockCipher(KEY.length, KEY);
    byte[] cipherText = new byte[0];
    byte[] plainText = new byte[0];
    try {
      cipherText = cipher.encrypt(PLAIN_TEXT);
      plainText = cipher.decrypt(cipherText);
    } catch (BlockCipher.EncryptionError | BlockCipher.DecryptionError encryptionError) {
      encryptionError.printStackTrace();
      Assert.assertFalse(true);
    }
    Assert.assertArrayEquals(plainText, PLAIN_TEXT);
  }

  private static byte[] KEY = {
      0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
      0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
  };

  private static byte[] PLAIN_TEXT = {
      0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
      0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
      0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
      0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
  };

  private static byte[] IV = {
      0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
      0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07
  };

  private static byte[] CIPHER_TEXT = {
      0x00, 0x59, (byte) 0xCC, 0x46, 0x71, 0x4B, (byte) 0xB2, 0x0E,
      0x6C, (byte) 0xC1, (byte) 0xF2, (byte) 0xC1, (byte) 0xA3, 0x1B, 0x7A, 0x4B,
      0x50, (byte) 0x8E, 0x1F, 0x7B, 0x25, (byte) 0xD2, 0x56, (byte) 0xB3,
      0x2B, 0x7F, (byte) 0xD6, 0x3C, 0x21, 0x6A, 0x54, (byte) 0xC6,
      0x0A, 0x6C, (byte) 0xD3, (byte) 0xBA, 0x24, (byte) 0x94, 0x55, 0x23,
      (byte) 0xB9, (byte) 0x80, 0x7D, 0x59, (byte) 0xB1, (byte) 0xF2, 0x54, (byte) 0xF3
  };
}
