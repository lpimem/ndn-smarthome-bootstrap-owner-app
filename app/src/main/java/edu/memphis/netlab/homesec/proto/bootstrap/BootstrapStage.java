package edu.memphis.netlab.homesec.proto.bootstrap;

import net.named_data.jndn.Face;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

import edu.memphis.netlab.homesec.proto.BootstrapSessionManager;
import edu.memphis.netlab.homesec.proto.bootstrap.impl.BtSessionImpl;
import edu.memphis.netlab.homesec.util.ThreadHelper;

/**
 * Description:
 * An Immutable object representing a m_stage in bootstrapping process for a specific device.
 * Date: 2/7/17
 * Author: lei
 */

public abstract class BootstrapStage {

  protected BootstrapStage(Face face, String devId, byte[] secret) {
    m_deviceId = devId;
    m_face = face;
    m_secret = secret;
    impl = new BtSessionImpl();
  }

  protected BootstrapStage(Face face, String devId, String pin) {
    this(face, devId, pin.getBytes(StandardCharsets.UTF_8));
  }

  public String getDeviceId() {
    return m_deviceId;
  }

  // check if all conditions are met for running
  protected abstract boolean checkBeforeRun();

  public abstract void run();

  protected void beforeRunNext() {
  }

  protected void runNext() {
    beforeRunNext();
    if (!m_next.checkBeforeRun()) {
      throw new RuntimeException("Programming error: invalid pre-conditions for "
          + getDeviceId() + " @ " + this.getClass().getName());
    }
    m_sessionMgr.updateSession(getDeviceId(), m_next);
    m_next.run();
  }

  protected void setNext(BootstrapStage next) {
    m_next = next;
  }

  protected BootstrapStage getNext() {
    return m_next;
  }

  public static void abortBootstrapping(String devId) {
    BootstrapSessionManager.INSTANCE.removeSession(devId);
  }

  protected boolean isSessionValid() {
    BootstrapStage inSession = m_sessionMgr.getSession(getDeviceId());
    return inSession == this;
  }

  protected byte[] getSecret() {
    return m_secret;
  }

  protected static ExecutorService getJobExecutor() {
    return ThreadHelper.scheduledThreadPool;
  }

  protected final Face m_face;
  protected BtSessionImpl impl;

  private final String m_deviceId;
  private final byte[] m_secret;
  private BootstrapStage m_next;
  private final BootstrapSessionManager m_sessionMgr = BootstrapSessionManager.INSTANCE;
}
