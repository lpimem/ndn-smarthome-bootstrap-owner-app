package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Face;
import net.named_data.jndn.security.KeyChain;

/**
 * Description:
 * <p>
 * Author: lei
 */

public class Context {

  public Context(KeyChain k, Face f) {
    mKeyChain = k;
    mFace = f;
  }

  public KeyChain getKeyChain() {
    return mKeyChain;
  }

  public Face getFace() {
    return mFace;
  }

  private KeyChain mKeyChain;
  private Face mFace;
}
