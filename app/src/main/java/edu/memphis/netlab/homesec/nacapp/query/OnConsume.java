package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;

import java.util.Iterator;
import java.util.List;

/**
 * Description:
 * <p>
 * Author: lei
 */

public abstract class OnConsume {
  public abstract void process(Context ctx, final Interest in, Data data, List<OnConsume> following)
      throws ConsumptionError;

  private static OnConsume nextHandler(List<OnConsume> following) {
    if (null == following || following.size() == 0) {
      return null;
    }
    Iterator<OnConsume> iter = following.iterator();
    OnConsume handler = iter.next();
    iter.remove();
    return handler;
  }

  protected static void handleNext(List<OnConsume> following, Context ctx, final Interest in, Data data) {
    OnConsume handler = nextHandler(following);
    if (handler != null) {
      handler.process(ctx, in, data, following);
    }
  }
}
