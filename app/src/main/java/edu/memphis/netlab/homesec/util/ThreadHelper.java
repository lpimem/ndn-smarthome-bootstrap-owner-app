package edu.memphis.netlab.homesec.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.memphis.netlab.homesec.Constants;

/**
 * Description:
 * <p>
 * Date: 3/19/17
 * Author: lei
 */

public class ThreadHelper {

  public static void runLater(Runnable task, long timeout) {
    try {
      TimeUnit.MICROSECONDS.sleep(timeout);
      task.run();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

//    scheduledThreadPool.schedule(task, timeout, TimeUnit.MILLISECONDS);
//    Handler handler = new Handler();
//    handler.postDelayed(task, timeout);
  }

  public final static ScheduledExecutorService scheduledThreadPool =
      Executors.newScheduledThreadPool(Constants.THREAD_POOL_SIZE);
}

