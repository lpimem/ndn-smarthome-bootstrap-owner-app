package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.security.OnDataValidationFailed;
import net.named_data.jndn.security.OnVerified;
import net.named_data.jndn.security.SecurityException;

import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 * <p>
 * Author: lei
 */

public class VerifyDataSignature extends OnConsume {
  @Override
  public void process(final Context ctx, final Interest in, Data data, final List<OnConsume> following) throws ConsumptionError {
    try {
      ctx.getKeyChain().verifyData(data, new OnVerified() {
        @Override
        public void onVerified(Data data) {
          List<OnConsume> handlers = new LinkedList<>(following);
          handleNext(following, ctx, in, data);
        }
      }, new OnDataValidationFailed() {
        @Override
        public void onDataValidationFailed(Data data, String reason) {
          throw new ConsumptionError("cannot verify data [" + data.getName() + "]: reason");
        }
      });
    } catch (SecurityException e) {
      throw new ConsumptionError(e);
    }
  }
}
