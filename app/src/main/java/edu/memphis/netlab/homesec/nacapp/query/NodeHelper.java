package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 * NodeHelper provides static methods for NDN query shortcuts.
 * Author: lei
 */

public final class NodeHelper {

  private NodeHelper() {
    throw new AssertionError("should not instantiate this class");
  }

  private static List<OnBeforeSendInterest> DEFAULT_INTEREST_PREPROCESSORS = new LinkedList<>();

  private static List<OnConsume> DEFAULT_DATA_PROCESSORS = new LinkedList<>();

  static {
    DEFAULT_INTEREST_PREPROCESSORS.add(new PatchInterestSignature());


    DEFAULT_DATA_PROCESSORS.add(new VerifyDataSignature());
  }

  public static List<OnBeforeSendInterest> getDefaultInterestPreprocessors() {
    return new LinkedList<>(DEFAULT_INTEREST_PREPROCESSORS);
  }

  public static List<OnConsume> getDefaultDataProcessors() {
    return new LinkedList<>(DEFAULT_DATA_PROCESSORS);
  }

  public static void expressInterest(
      final Context ctx,
      Name name,
      long interestTimeout,
      List<OnBeforeSendInterest> preInterstHandlers,
      final List<OnConsume> onConsumeHandlers) throws SecurityException, IOException {
    Interest sendInterest = new Interest(name, interestTimeout);
    if (null == preInterstHandlers) {
      preInterstHandlers = DEFAULT_INTEREST_PREPROCESSORS;
    }
    for (OnBeforeSendInterest process : preInterstHandlers) {
      sendInterest = process.onBeforeSendInterest(ctx, sendInterest);
    }
    ctx.getFace().expressInterest(sendInterest, new OnData() {
      @Override
      public void onData(Interest replyInterest, Data data) {
        List<OnConsume> handlers;
        if (null == onConsumeHandlers) {
          handlers = DEFAULT_DATA_PROCESSORS;
        } else {
          handlers = new LinkedList<>(onConsumeHandlers);
        }
        OnConsume.handleNext(handlers, ctx, replyInterest, data);
      }
    });
  }

  public static class ProcessError extends RuntimeException {
    public ProcessError() {
    }

    public ProcessError(String message) {
      super(message);
    }

    public ProcessError(Throwable e) {
      super(e);
    }

    public ProcessError(String message, Throwable e) {
      super(message, e);
    }
  }


}
