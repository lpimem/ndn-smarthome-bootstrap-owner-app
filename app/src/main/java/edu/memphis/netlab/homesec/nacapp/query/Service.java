package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.memphis.netlab.homesec.Constants;

/**
 * Description:
 * <p>
 * Author: lei
 */

public final class Service {

  private Service() {
  }

  private final static Name QUERY = new Name("SERVICE/list");

  public interface OnData {
    void onData(ServiceInfoList serviceList);
  }

  private static class OnDataWrapper extends OnConsume {

    public OnDataWrapper(OnData handler) {
      mHandler = handler;
    }

    @Override
    public void process(Context ctx, Interest in, Data data, List<OnConsume> following) throws ConsumptionError {
      if (following.size() != 0) {
        throw new ConsumptionError(String.format(Locale.ENGLISH,
            "A %s handler should be the last in the OnConsume Callback Chain",
            OnData.class.getCanonicalName()));
      }
      final String json = data.getContent().toString();
      ServiceInfoList list = JsonDecoder.Instance.getGson()
          .fromJson(json, ServiceInfoList.class);
      mHandler.onData(list);
    }

    private OnData mHandler;
  }

  public void queryServices(Context ctx, final Name prefix, OnData onData) {
    Name interest = new Name(prefix);
    interest.append(QUERY);
    try {
      List<OnConsume> dataHandlers = NodeHelper.getDefaultDataProcessors();
      dataHandlers.add(new OnDataWrapper(onData));
      NodeHelper.expressInterest(
          ctx,
          interest,
          Constants.SESSION_TIMEOUT_MS,
          null,
          dataHandlers);
    } catch (SecurityException |
        IOException e) {
      throw new ConsumptionError();
    }
  }
}
