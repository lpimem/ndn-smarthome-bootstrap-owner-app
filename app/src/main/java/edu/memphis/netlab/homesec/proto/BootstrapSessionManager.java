package edu.memphis.netlab.homesec.proto;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.proto.bootstrap.AuthenticateOwner;
import edu.memphis.netlab.homesec.proto.bootstrap.BootstrapHelper;
import edu.memphis.netlab.homesec.proto.bootstrap.BootstrapStage;

/**
 * Description:
 * Manages Bootstrap Sessions
 * Date: 2/7/17
 * Author: lei
 */

public enum BootstrapSessionManager {
  INSTANCE;

  BootstrapSessionManager() {
    startCleaner();
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        stopCleaner();
      }
    }));
  }

  public void newSession(String id, BootstrapStage stage) {
    Session s = new Session();
    s.setStage(stage);
    this.m_sessions.put(id, s);
    Log.d(TAG, String.format("bt session %s started", id));
  }

  public void updateSession(String id, BootstrapStage stage) {
    Session s = m_sessions.get(id);
    if (s == null) {
      throw new BootstrapSessionNotFound();
    }
    s.setStage(stage);
  }

  public void removeSession(String id) {
    Session removedSession = this.m_sessions.remove(id);
    Log.d(TAG, String.format("bt session %s removed", id));
    if (!removedSession.success()) {
      BootstrapHelper.sendBtFailedBroadcast(
          id, "Bootstrap failed at step : " + removedSession.getStage().getClass().getSimpleName());
    }
  }

  public BootstrapStage getSession(String id) throws BootstrapSessionNotFound {
    Session s = m_sessions.get(id);
    if (s == null) {
      throw new BootstrapSessionNotFound();
    }
    return s.getStage();
  }

  private static class Session {

    public void setStage(BootstrapStage stage) {
      this.stage = stage;
      this.refresh();
    }

    public BootstrapStage getStage() {
      return this.stage;
    }

    void refresh() {
      this.lastRefresh = System.currentTimeMillis();
    }

    boolean isInvalid() {
      return System.currentTimeMillis() - lastRefresh > Constants.SESSION_TIMEOUT_MS;
    }

    boolean success() {
      return this.stage.getClass() == AuthenticateOwner.class
          && ((AuthenticateOwner) this.stage).success();
    }

    BootstrapStage stage;
    long lastRefresh;
  }

  private void stopCleaner() {
    this.m_cleanerRun = false;
  }

  private void startCleaner() {
    this.m_executor.submit(new Runnable() {

      private void clean() {
        List<String> toRemove = new LinkedList<>();
        for (String id : m_sessions.keySet()) {
          if (m_sessions.get(id).isInvalid()) {
            toRemove.add(id);
          }
        }
        for (String id : toRemove) {
          removeSession(id);
        }
      }

      @Override
      public void run() {
        Thread.currentThread().setName(TAG);
        m_cleanerRun = true;
        while (m_cleanerRun) {
          try {
            TimeUnit.MILLISECONDS.sleep(Constants.SESSION_TIMEOUT_MS);
            clean();
          } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
          }
        }
      }
    });
  }

  private static final String TAG = "BTSessionMgr";
  final ExecutorService m_executor = Executors.newSingleThreadExecutor();
  final Map<String, Session> m_sessions = new ConcurrentHashMap<>();
  private boolean m_cleanerRun = false;
}
