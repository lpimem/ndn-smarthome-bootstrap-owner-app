package edu.memphis.netlab.homesec.util;

/**
 * Description:
 *   Make JUL work on Android
 *   Took from http://stackoverflow.com/a/9047282
 * Date: 3/8/17
 * Author: lei
 */

import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.ThreadPoolFace;

import java.util.logging.*;

import edu.memphis.netlab.homesec.security.BlockCipher;

/**
 * Make JUL work on Android.
 */
public class AndroidLoggingHandler extends Handler {

  private static boolean reset = false;

  /**
   * Call resetAll in onCreate of an application
   */
  public static void resetAll(){
    if (reset){
      return;
    }
    reset = true;
    AndroidLoggingHandler.reset(new AndroidLoggingHandler());
    // config jNDN loggers
    java.util.logging.Logger.getLogger(Face.class.getName()).setLevel(Level.INFO);
    java.util.logging.Logger.getLogger(ThreadPoolFace.class.getName()).setLevel(Level.INFO);
    java.util.logging.Logger.getLogger(BlockCipher.class.getName()).setLevel(Level.INFO);
  }

  public static void reset(Handler rootHandler) {
    Logger rootLogger = LogManager.getLogManager().getLogger("");
    Handler[] handlers = rootLogger.getHandlers();
    for (Handler handler : handlers) {
      rootLogger.removeHandler(handler);
    }
    rootLogger.addHandler(rootHandler);
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  @SuppressWarnings("WrongConstant")
  @Override
  public void publish(LogRecord record) {
    if (!super.isLoggable(record))
      return;

    String name = record.getLoggerName();
    int maxLength = 30;
    String tag = name.length() > maxLength ? name.substring(name.length() - maxLength) : name;

    try {
      final int level = getAndroidLevel(record.getLevel());
      Log.println(level, tag, record.getMessage());
      //noinspection ThrowableResultOfMethodCallIgnored
      if (record.getThrown() != null) {
        Log.println(level, tag, Log.getStackTraceString(record.getThrown()));
      }
    } catch (RuntimeException e) {
      Log.e("AndroidLoggingHandler", "Error logging message.", e);
    }
  }

  static int getAndroidLevel(Level level) {
    int value = level.intValue();
    if (value >= 1000) {
      return Log.ERROR;
    } else if (value >= 900) {
      return Log.WARN;
    } else if (value >= 800) {
      return Log.INFO;
    } else {
      return Log.DEBUG;
    }
  }
}