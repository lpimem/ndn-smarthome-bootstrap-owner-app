package edu.memphis.netlab.homesec.nservice;

import android.util.Log;

import net.named_data.jndn.Name;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnRegisterSuccess;

/**
 * Description:
 * <p>
 * Default handler for failed prefix registrations.
 * </p>
 * Date: 2/9/17
 * Author: lei
 */

public class DefaultPrefixRegisterHandlers {

  public static OnSucHandler onSuccess(String tag) {
    return new OnSucHandler(tag);
  }

  public static OnFailHandler onFail(String tag) {
    return new OnFailHandler(tag);
  }

  public static class OnSucHandler extends Handler implements OnRegisterSuccess {

    OnSucHandler(String tag) {
      super(tag);
    }

    /**
     * Face.registerPrefix calls onRegisterSuccess when it receives a success
     * message from the forwarder.
     *
     * @param prefix             The prefix given to registerPrefix. NOTE: You must not change
     *                           the prefix object - if you need to change it then make a copy.
     * @param registeredPrefixId The registered prefix ID which was also returned
     */
    @Override
    public void onRegisterSuccess(Name prefix, long registeredPrefixId) {
      info("Registered prefix: " + prefix.toUri() + " with id " + registeredPrefixId);
      NdnHelper.putRegisteredPrefixId(prefix.toUri(), registeredPrefixId);
    }
  }

  public static class OnFailHandler extends Handler implements OnRegisterFailed {

    OnFailHandler(String tag) {
      super(tag);
    }

    /**
     * If failed to retrieve the connected hub's ID or failed to register the
     * prefix, onRegisterFailed is called.
     *
     * @param prefix The prefix given to registerPrefix.
     */
    @Override
    public void onRegisterFailed(Name prefix) {
      error("Failed to register prefix: " + prefix.toUri());
    }
  }

  private static class Handler {
    private final String tag;

    Handler(String tag) {
      this.tag = tag;
    }

    void info(String msg) {
      Log.i(tag, msg);
    }

    void error(String msg) {
      Log.e(tag, msg);
    }
  }
}