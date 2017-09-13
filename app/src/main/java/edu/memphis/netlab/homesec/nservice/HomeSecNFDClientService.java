package edu.memphis.netlab.homesec.nservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Strings;

import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.proto.bootstrap.BootstrapHelper;
import edu.memphis.netlab.homesec.security.SecurityManager;
import edu.memphis.netlab.homesec.util.ThreadHelper;

public class HomeSecNFDClientService extends HomeSecNFDClientServiceImpl {

  @Override
  public int onStartCommand(Intent intent,
                            int flags,
                            int startId) {
    Log.d(TAG, "onStartCommand");
    String token = intent.getStringExtra(Constants.KEY_LOGIN_TOKEN);
    if (Strings.isNullOrEmpty(token)) {
      Log.e(TAG, HomeSecNFDClientService.class.getName() +
          " service should be provided a valid " + Constants.KEY_LOGIN_TOKEN);
      this.stop();
      return Service.START_NOT_STICKY;
    }
    if (null == m_nfdcDaemon) {
      configDefaultInterestHandlers();
      m_nfdcDaemon = initNFDClientDaemon(m_face, token);
      ThreadHelper.scheduledThreadPool.submit(m_nfdcDaemon);
    } else if (m_nfdcDaemon.shouldKeepAlive() && !m_nfdcDaemon.isRunning()) {
      ThreadHelper.scheduledThreadPool.submit(m_nfdcDaemon);
    }
    return Service.START_REDELIVER_INTENT;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return m_LocalBinder;
  }

  /**
   * Add a device to the home network
   *
   * @param id  identifier string for the device
   * @param pin pin code for pairing the device
   */
  public void addDevice(final String id, final String pin) {
    ThreadHelper.scheduledThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        Log.i(TAG, "start adding new device: " + id);
        BootstrapHelper.bootstrapDevice(HomeSecNFDClientService.this, m_face, id, pin);
      }
    });
    Log.d(TAG, "add device task submitted");
  }

  /**
   * @param name           name of the interest
   * @param onDataCallback called when the interest is satisfied
   */
  public void expressInterest(final Name name, final OnData onDataCallback) {
    Interest interest = new Interest(name, Constants.INTEREST_LIFE_MS);
    try {
      SecurityManager.INSTANCE.getKeyChain().sign(interest);
      this.m_face.expressInterest(interest, onDataCallback);
    } catch (SecurityException | IOException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    if (null != this.m_nfdcDaemon) {
      this.m_nfdcDaemon.stop();
    }
    stopSelf();
  }

  public class LocalBinder extends Binder {
    public HomeSecNFDClientService getService() {
      return HomeSecNFDClientService.this;
    }
  }

  private IBinder m_LocalBinder = new HomeSecNFDClientService.LocalBinder();
}
