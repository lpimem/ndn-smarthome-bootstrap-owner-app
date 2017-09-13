package edu.memphis.netlab.homesec.nacapp;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.List;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.nacapp.query.Context;
import edu.memphis.netlab.homesec.nacapp.query.NodeHelper;
import edu.memphis.netlab.homesec.nacapp.query.OnBeforeSendInterest;
import edu.memphis.netlab.homesec.nacapp.query.OnConsume;
import edu.memphis.netlab.homesec.nacapp.query.OnError;

/**
 * Description:
 * <p>
 * Author: lei
 */

public class TemperatureReader {
  public TemperatureReader(Context ctx, String prefix, String path) {
    mPrefix = new Name(prefix);
    mPrefix.append(path);
    mCtx = ctx;
  }

  public void getCurrentTemperature(OnConsume onData, OnError onError) {
    List<OnBeforeSendInterest> preprocessors = NodeHelper.getDefaultInterestPreprocessors();
    List<OnConsume> callbacks = NodeHelper.getDefaultDataProcessors();
    callbacks.add(onData);
    try {
      NodeHelper.expressInterest(mCtx, this.mPrefix, Constants.SESSION_TIMEOUT_MS, preprocessors, callbacks);
    } catch (SecurityException | IOException e) {
      onError.onError(e.getMessage());
    }
  }

  private final Name mPrefix;
  private final Context mCtx;

}
