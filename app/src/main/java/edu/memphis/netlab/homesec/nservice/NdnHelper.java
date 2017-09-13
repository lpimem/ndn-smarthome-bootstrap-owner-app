package edu.memphis.netlab.homesec.nservice;

import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.NetworkNack;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnNetworkNack;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.encoding.WireFormat;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.util.Blob;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.security.SecurityManager;
import edu.memphis.netlab.homesec.util.ThreadHelper;

/**
 * Description:
 * <p>
 * Date: 3/8/17
 * Author: lei
 */

public class NdnHelper {
  public interface OnFailed {
    void onFail(String reason);
  }

  public static void registerPrefixes(final Face face,
                                      final Map<Name, OnInterestCallback> handlers,
                                      final int count,
                                      final int retryMax,
                                      final OnRegisterFailed onFail
  ) {
    if (count > retryMax) {
      Name n = handlers.keySet().iterator().next();
      onFail.onRegisterFailed(n);
      return;
    }

    if (count > 0) {
      _logger.info(String.format(Locale.ENGLISH, "retrying registerPrefixes %d/%d", count, retryMax));
    }

    for (final Map.Entry<Name, OnInterestCallback> config : handlers.entrySet()) {
      try {
        Log.d(NdnHelper.class.getName(), "Registering prefix " + config.getKey().toUri());
        face.registerPrefix(
            config.getKey(),
            config.getValue(),
            new OnRegisterFailed() {
              @Override
              public void onRegisterFailed(Name prefix) {
                Log.w(NdnHelper.class.getName(), "Prefix register failed: " + prefix.toUri());
                OnInterestCallback cb = handlers.get(prefix);
                if (cb == null) {
                  return;
                }
                try {
                  TimeUnit.MILLISECONDS.sleep(Constants.INTEREST_LIFE_MS * 2);
                  Log.d(NdnHelper.class.getName(), "retry registering prefix: " + prefix.toUri());
                  Map<Name, OnInterestCallback> handlerContainer = new HashMap<>();
                  handlerContainer.put(prefix, cb);
                  registerPrefixes(face, handlerContainer, count + 1, retryMax, onFail);
                } catch (InterruptedException ignore) {
                }
              }
            },
            DefaultPrefixRegisterHandlers.onSuccess(NdnHelper.class.getName()),
            WireFormat.getDefaultWireFormat());
      } catch (Exception | Error e) {
        Log.e(NdnHelper.class.getName(), "Error registering name " + config.getKey() + " : " + e.getMessage());
      }
    }
  }

  public static void unregisterPrefix(final Face face, String prefix) {
    long id = getRegisteredPrefixId(prefix);
    if (id > 0) {
      face.removeRegisteredPrefix(id);
    }
  }

  public static void removeAllPendingInterests(final Face f, String name) {
    ConcurrentLinkedQueue<Long> pending = getPendingInterests(name);
    if (null == pending) {
      return;
    }
    for (Long id : pending) {
      if (id != null) {
        f.removePendingInterest(id);
      }
    }
  }

  public static void expressInterest(final Face face,
                                     final Interest interest,
                                     final OnData onData,
                                     final OnFailed onFailed) {
    expressInterest(face, interest, 0, Constants.MAX_RETRY, onData, onFailed,
        null, Constants.DEFAULT_RETRY_INTERVAL_MS);
  }

  public static void expressInterest(final Face face,
                                     final Name name,
                                     final boolean mustBefresh,
                                     final OnData onData,
                                     final OnFailed onFailed) {
    Interest interest = new Interest(name);
    interest.setMustBeFresh(mustBefresh);
    interest.setInterestLifetimeMilliseconds(Constants.INTEREST_LIFE_MS);
    expressInterest(face, interest, onData, onFailed);
  }

  public static void nfdCmdAddRoute(final Name prefix, final String faceURI) {
    throw new RuntimeException("Not implemented.");
  }

  private static void expressInterest(final Face face,
                                      final Interest interest,
                                      final int retryCount,
                                      final int retryMax,
                                      final OnData onData,
                                      final OnFailed onMaxRetryFailed,
                                      String lastFailReason,
                                      final long retryTimeoutMs
  ) {
    if (retryCount > retryMax) {
      _logger.info(String.format(Locale.ENGLISH,
          "Max retry reached for expressing interest [%s]", interest.toUri()));
      onMaxRetryFailed.onFail(lastFailReason);
      return;
    }
    if (retryCount > 0) {
      _logger.info(String.format(Locale.ENGLISH,
          "(%d/%d %s) retrying express Interest [%s]",
          retryCount, retryMax, lastFailReason, interest.toUri()));
    }
    final double interestLife = interest.getInterestLifetimeMilliseconds();
    // make sure resending interest after the previous interest expired,
    // only if the reason is NACK or IOException.c
    final long[] finalRetryTimeoutMsHolder = new long[1];
    if (retryTimeoutMs < interestLife) {
      finalRetryTimeoutMsHolder[0] = Double.doubleToLongBits(Math.ceil(interestLife));
    } else {
      finalRetryTimeoutMsHolder[0] = retryTimeoutMs;
    }

    OnTimeout onTimeout = new OnTimeout() {
      @Override
      public void onTimeout(final Interest interest) {
        Log.d(NdnHelper.class.getName(), "Interest timeout: " + interest.toUri());
        final String lastFailReason = "timeout";
        ThreadHelper.runLater(new Runnable() {
          @Override
          public void run() {
            expressInterest(face, interest, retryCount + 1, retryMax,
                onData, onMaxRetryFailed, lastFailReason, retryTimeoutMs);
          }
        }, retryTimeoutMs);
      }
    };

    OnNetworkNack onNack = new OnNetworkNack() {
      @Override
      public void onNetworkNack(final Interest interest, NetworkNack networkNack) {
        final String lastFailReason = networkNack.getReason().name();
        Log.d(NdnHelper.class.getName(), "NACK " + lastFailReason);
        ThreadHelper.runLater(new Runnable() {
          @Override
          public void run() {
            expressInterest(face, interest, retryCount + 1, retryMax,
                onData, onMaxRetryFailed, lastFailReason, retryTimeoutMs);
          }
        }, finalRetryTimeoutMsHolder[0]);
      }
    };

    try {
      face.expressInterest(interest, onData, onTimeout, onNack);
    } catch (IOException e) {
      final String finalLastFailReason = e.getMessage();
      ThreadHelper.runLater(new Runnable() {
        @Override
        public void run() {
          expressInterest(face, interest, retryCount + 1, retryMax,
              onData, onMaxRetryFailed, finalLastFailReason, retryTimeoutMs);
        }
      }, finalRetryTimeoutMsHolder[0]);
    }
  }

  public static long getRegisteredPrefixId(String prefix) {
    Long ret = registeredPrefixes.get(prefix);
    return ret == null ? -1 : ret;
  }

  public static long putRegisteredPrefixId(String prefix, long id) {
    Long existingId = registeredPrefixes.put(prefix, id);
    if (null == existingId) {
      return -1;
    }
    return existingId;
  }

  public static ConcurrentLinkedQueue<Long> getPendingInterests(String name) {
    return pendingInterests.get(name);
  }

  public static void addPendingInterest(String name, Long id) {
    ConcurrentLinkedQueue<Long> pids;
    synchronized (pitLock) {
      pids = pendingInterests.get(name);
      if (null == pids) {
        pids = new ConcurrentLinkedQueue<>();
        pendingInterests.put(name, pids);
      }
    }
    pids.add(id);
  }

  public static Data genereateTextData(Name name, String content) throws SecurityException {
    Data d = new Data();
    d.setName(name);
    d.setContent(new Blob(content));
    MetaInfo meta = new MetaInfo();
    meta.setFreshnessPeriod(Constants.DATA_FRESHNESS_PERIOD);
    d.setMetaInfo(meta);
    SecurityManager.INSTANCE.getKeyChain().sign(d);
    return d;
  }

  private final static Logger _logger = Logger.getLogger(NdnHelper.class.getName());
  private static ConcurrentHashMap<String, Long> registeredPrefixes = new ConcurrentHashMap<>();
  private static ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> pendingInterests = new ConcurrentHashMap<>();
  private static final Object pitLock = new Object();
  private static final Object rptLock = new Object();
}
