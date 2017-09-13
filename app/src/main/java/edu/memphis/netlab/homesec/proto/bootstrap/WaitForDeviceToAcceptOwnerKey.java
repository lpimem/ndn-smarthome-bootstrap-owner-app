package edu.memphis.netlab.homesec.proto.bootstrap;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;

import java.util.concurrent.TimeUnit;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.nservice.NdnHelper;
import edu.memphis.netlab.homesec.security.BlockCipher;
import edu.memphis.netlab.homesec.util.StringHelper;

import static edu.memphis.netlab.homesec.Constants.KEY_STRENGTH;

/**
 * Description:
 * <p>
 * Date: 3/17/17
 * Author: lei
 */

public class WaitForDeviceToAcceptOwnerKey extends BootstrapStage {

  static class AuthenticateDeviceTask
      extends AsyncTask<Void, String, Void>
      implements OnData, NdnHelper.OnFailed {

    AuthenticateDeviceTask(WaitForDeviceToAcceptOwnerKey stateCache,
                           Face face,
                           Name keyName) {
      this.m_stateCache = stateCache;
      this.m_face = face;
      this.m_keyName = keyName;
    }

    private void requestDeviceKey() {
      Name devPubKey = m_keyName;
      NdnHelper.expressInterest(this.m_face, devPubKey, true, this, this);
    }

    @Override
    public void onData(Interest interest, Data data) {
      Log.d(TAG, "got data for : " + interest.toUri());
      if (!m_stateCache.isSessionValid()) {
        Log.w(TAG, "ignored data as session state already transfered for"
            + m_stateCache.getDeviceId());
        return;
      }
      byte[] cipher = data.getContent().getImmutableArray();
      String[] parts = decipher(cipher);
      if (null == parts) return;
      boolean match = checkR1(parts[1]);
      if (match) {
        // create device Identity
        String devicePubKey = parts[0];
        Log.d(TAG, "Device added\r\n" +
            "\tID:\t" + m_stateCache.getDeviceId() + "\r\n" +
            "\tPubKey:\t" + devicePubKey
        );
        // TODO: Sign public key and publish certificate.
        // run next state
        String r2 = parts[2];
        ((AuthenticateOwner) m_stateCache.getNext()).setR2(r2);
        m_stateCache.runNext();
      } else {
        Log.w(TAG, "Device R1 not match\r\n" +
            "Interest:\t" + interest.toUri() + "\r\n" +
            "Data Name:\t" + data.getName().toUri() + "\r\n" +
            "R1 Got:\t" + parts[1] + "\r\n" +
            "R1 Expecting:\t" + new String(m_stateCache.m_r1));
        abortBootstrapping(m_stateCache.getDeviceId());
      }
    }

    private boolean checkR1(String r1Got) {
      return r1Got.equals(new String(m_stateCache.m_r1));
    }

    private String[] decipher(byte[] cipher) {
      Log.d(TAG, "cipher: " + StringHelper.toHex(cipher));
      BlockCipher bc = new BlockCipher(KEY_STRENGTH, m_stateCache.getSecret());
      byte[] plainText = new byte[0];
      try {
        plainText = bc.decrypt(cipher);
      } catch (BlockCipher.DecryptionError decryptionError) {
        Log.e(TAG, "Cannot decrypt received message: " + decryptionError.getMessage());
        abortBootstrapping(m_stateCache.getDeviceId());
        return null;
      }
      String message = new String(plainText);
      Log.d(TAG, "decrypted: " + message);
      return message.split(String.format("\\%s", Constants.DEFAULT_BT_DELIMITER));
    }

    @Override
    public void onFail(String reason) {
      Log.w(TAG, "cannot retrieve device key : " + reason);
      abortBootstrapping(m_stateCache.getDeviceId());
    }

    /**
     * Override this method to run a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(Void... params) {
      startPublishDTask();
      return null;
    }

    public void startPublishDTask() {
      Log.d(TAG, "init delay to request device key for " + m_stateCache.getDeviceId());
      try {
        TimeUnit.MILLISECONDS.sleep(500);
      } catch (InterruptedException ignored) {
      }
      Log.d(TAG, "Start AuthenticateDeviceTask for " + m_stateCache.getDeviceId());
      requestDeviceKey();
    }

    private final WaitForDeviceToAcceptOwnerKey m_stateCache;
    private final Face m_face;
    private final Name m_keyName;
  }

  public WaitForDeviceToAcceptOwnerKey(Face f, String devId, byte[] secret) {
    super(f, devId, secret);
    this.m_face = f;
  }

  protected void setR1(byte[] r1) {
    m_r1 = r1;
  }

  private Name generateKeyName() {
    return new Name(Constants.PREFIX_DEVICE_BOOTSTRAP +
        "/" + getDeviceId() + Constants.SUFFIX_DEVICE_BOOTSTRAP_PUBKEY);
  }

  @Override
  protected void beforeRunNext() {
    NdnHelper.removeAllPendingInterests(m_face, generateKeyName().toUri());
  }

  @Override
  protected boolean checkBeforeRun() {
    return m_r1 != null && m_r1.length > 0;
  }

  @Override
  public void run() {
    BootstrapStage next = new AuthenticateOwner(m_face, getDeviceId(), getSecret());
    setNext(next);
    new AuthenticateDeviceTask(this, m_face, generateKeyName()).execute();
  }

  private final static String TAG = "DevBT_2";
  private byte[] m_r1;
  private final Face m_face;
}