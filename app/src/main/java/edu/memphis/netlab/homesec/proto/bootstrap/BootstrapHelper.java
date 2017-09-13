package edu.memphis.netlab.homesec.proto.bootstrap;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import net.named_data.jndn.Face;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.proto.BootstrapSessionManager;

/**
 * Description:
 * <p>
 * Date: 3/20/17
 * Author: lei
 */

public class BootstrapHelper {
  public static void bootstrapDevice(Service service, Face face, String deviceId, String pin) {
    new InitState(service, face, deviceId, pin).run();
  }

  public static Service getThreadLocalService() {
    // return m_serviceHolder.get();
    return m_service_ins;
  }

  public static void sendBtFailedBroadcast(String deviceId, String reason) {
    final Service s = getThreadLocalService();
    Intent notice = new Intent(Constants.BROADCAST_ACTION_DEVICE_BT_FAIL)
        .putExtra(Constants.BOOTSTRAP_DEVICE_ID, deviceId)
        .putExtra(Constants.BROADCAST_ACTION_DEVICE_BT_MSG, reason);
    s.sendBroadcast(notice);
    LocalBroadcastManager.getInstance(s.getApplicationContext()).sendBroadcast(notice);
  }

  private static class InitState extends BootstrapStage {
    InitState(Service service, Face f, String devId, String pin) {
      super(f, devId, pin);
      m_pin = pin;
      m_service = service;
    }

    @Override
    protected boolean checkBeforeRun() {
      return true;
    }

    @Override
    public void run() {
      // m_serviceHolder.set(m_service);
      m_service_ins = m_service;
      InitAddDeviceKey stage = new InitAddDeviceKey(m_face, getDeviceId(), m_pin);
      setNext(stage);
      BootstrapSessionManager.INSTANCE.newSession(getDeviceId(), stage);
      runNext();
    }

    private final String m_pin;
    private Service m_service;
  }

  // private final static ThreadLocal<Service> m_serviceHolder = new ThreadLocal<>();
  private static Service m_service_ins = null;
}
