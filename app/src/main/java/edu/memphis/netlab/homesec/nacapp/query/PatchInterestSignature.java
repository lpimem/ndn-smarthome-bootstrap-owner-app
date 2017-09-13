package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Interest;
import net.named_data.jndn.security.SecurityException;

/**
 * Description:
 * Make sure an interest is signed before sending out.
 * Author: lei
 */

public class PatchInterestSignature implements OnBeforeSendInterest {
  @Override
  public Interest onBeforeSendInterest(Context ctx, Interest i) {

    // TODO: Check if the intereste is already signed. But for now, there is no easy API to do so.
    // See also :
    //       1. IdentityManager.signInterestByCertificate https://github.com/named-data/jndn/blob/4ff0db66af39fd3126aa8366b977da2984e69b2b/src/net/named_data/jndn/security/identity/IdentityManager.java#L961
    //       2. KeyChain.verifyInterest https://github.com/named-data/jndn/blob/00ee252e7d549a900817bfd18f11825b2ad06484/src/net/named_data/jndn/security/KeyChain.java#L825
    try {
      ctx.getKeyChain().sign(i);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
    return i;
  }
}
