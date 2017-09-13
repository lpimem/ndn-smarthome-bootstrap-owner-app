package edu.memphis.netlab.homesec.proto.bootstrap;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.nservice.NdnHelper;
import edu.memphis.netlab.homesec.proto.bootstrap.impl.BtSessionImpl;
import edu.memphis.netlab.homesec.security.BlockCipher;
import edu.memphis.netlab.homesec.util.StringHelper;

/**
 * Description:
 * The 3rd step of bootstrappin.
 * Date: 3/17/17
 * Author: lei
 */

public class AuthenticateOwner extends BootstrapStage {
  public AuthenticateOwner(Face f, String devId, byte[] secret) {
    super(f, devId, secret);
    this.impl = new BtSessionImpl();
  }

  @Override
  protected void beforeRunNext() {
    NdnHelper.unregisterPrefix(m_face, generateAuthName().toUri());
  }

  @Override
  protected boolean checkBeforeRun() {
    return !Strings.isNullOrEmpty(m_r2);
  }

  @Override
  public void run() {
    Log.d(TAG, "stage start... ");
    NdnHelper.registerPrefixes(
        m_face, preparePrefixes(), 0, Constants.MAX_RETRY, new OnRegisterFailed() {
          @Override
          public void onRegisterFailed(Name prefix) {
            Log.e(TAG,
                "Cannot register required prefix for bootstrapping: " + prefix.toUri());
            abortBootstrapping(getDeviceId());
          }
        });
  }

  public boolean success() {
    return m_suc;
  }

  private Name generateAuthName() {
    return new Name(Constants.PREFIX_OWNER_BOOTSTRAP + "/auth/for/" + getDeviceId());
  }

  private Map<Name, OnInterestCallback> preparePrefixes() {
    final Name dAuthOwner = generateAuthName();
    OnInterestCallback callback = new OnInterestCallback() {
      @Override
      public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId,
                             InterestFilter filter) {
        processInterestForR2(prefix, interest, face, interestFilterId, filter);
      }
    };
    Map<Name, OnInterestCallback> handlers = new HashMap<>();
    handlers.put(dAuthOwner, callback);
    return handlers;
  }

  private void processInterestForR2(
      Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {
    if (!isSessionValid()) {
      Log.w(TAG, "ignored interest as session state already transfered for " + getDeviceId());
      return;
    }
    byte[] nonce = new byte[8];
    StringHelper.randomBytes(nonce);
    String message = StringHelper.joinStrings(
        new String[]{m_r2, StringHelper.toHex(nonce)}, Constants.DEFAULT_BT_DELIMITER);
    Log.i(TAG, "plain text: " + message);
    try {
      byte[] cipher = impl.encrypt(message.getBytes(Charsets.UTF_8), getSecret());
      Log.i(TAG, "cipher (hex): " + StringHelper.toHex(cipher));
      impl.sendMessage(face, interest.getName(), cipher, Constants.DATA_FRESHNESS_PERIOD);
      try {
        m_face.processEvents();
      } catch (EncodingException e) {
        Log.e(TAG, "Cannot send R2", e);
      }
      onSuccess();
      Log.i(TAG, "Owner bootstrap finished for " + getDeviceId());
    } catch (BlockCipher.EncryptionError | SecurityException | IOException e) {
      Log.e(TAG, "Cannot send data " + prefix.toUri(), e);
      return;
    } finally {
      abortBootstrapping(getDeviceId());
    }
  }

  private void notifyResult(boolean success, String message) {
    final Service service = BootstrapHelper.getThreadLocalService();
    if (null == service) {
      Log.e(TAG, "Cannot get service instance to notify bootstrap result.");
      return;
    }
    Intent notice = null;
    if (success) {
      notice = new Intent(Constants.BROADCAST_ACTION_DEVICE_BT_SUC);
    } else {
      notice = new Intent(Constants.BROADCAST_ACTION_DEVICE_BT_FAIL);
      notice.putExtra(Constants.BROADCAST_ACTION_DEVICE_BT_MSG, message);
    }
    notice.putExtra(Constants.BOOTSTRAP_DEVICE_ID, getDeviceId());
    service.sendBroadcast(notice);
    LocalBroadcastManager.getInstance(service.getApplicationContext()).sendBroadcast(notice);
  }

  private void onSuccess() {
    m_suc = true;
    notifyResult(true, null);
  }

  protected void setR2(String r2) {
    m_r2 = r2;
  }

  private boolean m_suc = false;

  private final String TAG = "DevBT_3";
  private String m_r2;
  private BtSessionImpl impl;
}