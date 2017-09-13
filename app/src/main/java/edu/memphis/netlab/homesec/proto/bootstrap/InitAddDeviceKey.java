package edu.memphis.netlab.homesec.proto.bootstrap;

import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.certificate.PublicKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.nservice.NdnHelper;
import edu.memphis.netlab.homesec.security.BlockCipher;
import edu.memphis.netlab.homesec.security.SecurityManager;
import edu.memphis.netlab.homesec.util.StringHelper;

/**
 * Description:
 * <p>
 * Date: 3/17/17
 * Author: lei
 */

public class InitAddDeviceKey extends BootstrapStage {

  public InitAddDeviceKey(Face f, String devId, String pin) {
    super(f, devId, pin);
  }

  @Override
  protected boolean checkBeforeRun() {
    return isSessionValid();
  }

  @Override
  protected void beforeRunNext() {
    NdnHelper.unregisterPrefix(m_face, generateOwnerPubKeyName().toUri());
  }

  @Override
  public void run() {
    BootstrapStage nextStg = new WaitForDeviceToAcceptOwnerKey(m_face, this.getDeviceId(), getSecret());
    setNext(nextStg);

    NdnHelper.registerPrefixes(
        m_face,
        preparePrefixes(),
        0,
        Constants.MAX_RETRY,
        new OnRegisterFailed() {
          @Override
          public void onRegisterFailed(Name prefix) {
            Log.e(TAG, "Cannot register required prefix for bootstrapping: " + prefix.toUri());
            abortBootstrapping(getDeviceId());
          }
        });
  }

  private String generateRandomHex() {
    byte[] random = new byte[8];
    StringHelper.randomBytes(random);
    return StringHelper.toHex(random);
  }

  private String generateOwnerKeyMessage(String randomHex) {
    PublicKey pk = SecurityManager.INSTANCE.getOwnerPubkey();
    return StringHelper.joinStrings(new String[]{
        pk.getKeyDer().toHex(),
        getDeviceId(),
        randomHex
    }, Constants.DEFAULT_BT_DELIMITER);
  }

  private byte[] encryptMessage(String message) {
    byte[] cipherText = new byte[0];
    try {
      cipherText = impl.encrypt(message.getBytes(StandardCharsets.UTF_8), getSecret());
    } catch (BlockCipher.EncryptionError encryptionError) {
      Log.e(TAG, "cannot encrypt message: " + encryptionError.getMessage());
      abortBootstrapping(getDeviceId());
    }
    return cipherText;
  }

  private void sendEncryptedMessage(final byte[] cipher, Name name) {
    if (null == cipher || cipher.length <= 0) {
      return;
    }
    try {
      impl.sendMessage(m_face, name, cipher, Constants.DATA_FRESHNESS_PERIOD);
    } catch (SecurityException | IOException e) {
      Log.e(TAG, "Cannot send data for [" + name.toUri() + "] : " + e.getMessage());
      abortBootstrapping(getDeviceId());
    }
  }

  private void sendOwnerKey(final String randomHex, Face face, Interest interest) {
    final String message = generateOwnerKeyMessage(randomHex);
    final byte[] cipherText = encryptMessage(message);
    sendEncryptedMessage(cipherText, interest.getName());
  }

  private Map<Name, OnInterestCallback> preparePrefixes() {
    final Name pubkeyName = generateOwnerPubKeyName();
    OnInterestCallback callbk =
        new OnInterestCallback() {
          @Override
          public void onInterest(Name prefix,
                                 Interest interest,
                                 Face face,
                                 long interestFilterId,
                                 InterestFilter filter) {
            Log.d(TAG, "interest: " + interest.toUri());
            if (!isSessionValid()) {
              Log.w(TAG, "ignored Interest as state has transfered for " + getDeviceId());
              return;
            }
            String randomHex = generateRandomHex();
            sendOwnerKey(randomHex, face, interest);
            ((WaitForDeviceToAcceptOwnerKey) getNext()).setR1(randomHex.getBytes(StandardCharsets.UTF_8));
            runNext();
          }
        };
    Map<Name, OnInterestCallback> handlers = new HashMap<>();
    handlers.put(pubkeyName, callbk);
    return handlers;
  }

  private Name generateOwnerPubKeyName() {
    return new Name(Constants.PREFIX_OWNER_BOOTSTRAP +
        Constants.SUFFIX_DEVICE_BOOTSTRAP_PUBKEY + "/for/" + getDeviceId());
  }

  private final static String TAG = "DevBT_1";
}