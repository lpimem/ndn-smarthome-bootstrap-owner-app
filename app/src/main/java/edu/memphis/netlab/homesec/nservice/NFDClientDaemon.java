package edu.memphis.netlab.homesec.nservice;

import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.ThreadPoolFace;
import net.named_data.jndn.encoding.EncodingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.memphis.netlab.homesec.Constants;

/**
 * Description:
 * <p>
 * Handles incoming interests.
 * Run as a thread started by {edu.memphis.homesec.HomeSecNFDClientService}.
 * </p>
 * Date: 2/7/17
 * Author: lei
 */

public class NFDClientDaemon implements Runnable {

  public NFDClientDaemon(Face face, String masterToken) {
    this.m_face = face;
    this.m_masterToken = masterToken;
    this.m_handlers = new HashMap<>();
  }

  public void addPrefixHandler(Name n, OnInterestCallback callback) {
    m_handlers.put(n, callback);
  }

  /**
   * If provided face is a ThreadPoolFace instance,
   * this thread will register prefixes and exit.
   * Otherwise, this thread will keep running in a while loop to
   * call processEvents.
   */
  @Override
  public void run() {
    Thread.currentThread().setName("HomeSecNFDClientDaemon");
    m_running.set(true);
    try {
      registerPrefixes();
      processEvents();
    } catch (Exception e) {
      err("[FATAL] NFDClient died: " + e.getMessage());
    } finally {
      m_running.set(false);
    }
  }

  public boolean isRunning() {
    return m_running.get();
  }

  public void stop() {
    m_running.set(false);
  }

  public boolean shouldKeepAlive() {
    return this.m_face.getClass() != ThreadPoolFace.class;
  }

  private void registerPrefixes() {
    NdnHelper.registerPrefixes(
        m_face,
        m_handlers,
        0,
        Constants.MAX_RETRY,
        new OnRegisterFailed() {
          @Override
          public void onRegisterFailed(Name prefix) {
            err("Cannot register root prefixes");
            System.exit(1);
          }
        });
  }

  private void processEvents() throws IOException, EncodingException, InterruptedException {
    while (m_running.get()) {
      synchronized (this.m_face) {
        this.m_face.processEvents();
      }
      TimeUnit.MILLISECONDS.sleep(100);
    }
  }

  private static void log(String msg) {
    Log.d(TAG, msg);
  }

  private static void err(String msg) {
    Log.e(TAG, msg);
  }

  private static String TAG = "NFDClient";

  private String m_masterToken;
  private final Face m_face;
  private Map<Name, OnInterestCallback> m_handlers;
  private AtomicBoolean m_running = new AtomicBoolean();
}
