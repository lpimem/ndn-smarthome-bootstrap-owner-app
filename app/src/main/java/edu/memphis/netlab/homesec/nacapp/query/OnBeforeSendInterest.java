package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Interest;

/**
 * Description:
 * <p>
 * Author: lei
 */
public interface OnBeforeSendInterest {
  Interest onBeforeSendInterest(Context ctx, Interest i);
}