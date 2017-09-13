package edu.memphis.netlab.homesec.security;

import android.util.Log;

import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.certificate.Certificate;
import net.named_data.jndn.security.certificate.PublicKey;
import net.named_data.jndn.util.Blob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Description:
 * Provides a singleton to create, list, and query identities.
 * Date: 5/24/17
 * Author: lei
 */

public enum IdentityManager {
  INSTANCE;
  private final static String TAG = "IdentityManager";

  IdentityManager() {
  }

  public Identity create(Name n, Certificate cert) {
    Identity id = new Identity(n, cert);
    mStore.put(n, id);
    return id;
  }

  public Identity create(Name n, byte[] pubkey, KeyChain keyChain) throws SecurityException {
    PublicKey pk = new PublicKey(new Blob(pubkey));
    Certificate c = new Certificate();
    c.setPublicKeyInfo(pk);
    long now = new Date().getTime();
    c.setNotBefore(now);
    c.setNotAfter(now + CERT_PERIOD);
    keyChain.sign(c);
    return create(n, c);
  }

  public Identity get(Name n) {
    return mStore.get(n);
  }

  /**
   * Always replace existing indentiy in store.
   *
   * @param i a valid identity
   */
  public void put(Identity i) {
    checkAndThrow(i);
    this.mStore.put(i.Name(), i);
  }

  /**
   * Throw a runtime exception if the given identity is no more valid
   *
   * @param i an identity object
   */
  public static void checkAndThrow(Identity i) {
    if (!i.isValid()) {
      throw new RuntimeException("Certificate for " + i.Name().toUri() + " expired");
    }
  }

  /**
   * Put into store if there is no existing identity for the same name.
   * Upon collision, replace existing identity with the new one only if the former is expired.
   *
   * @param i a valid identity
   * @return identity in store after the function call.
   */
  public Identity putOrGet(Identity i) {
    checkAndThrow(i);
    Identity instore = null;
    synchronized (this) {
      instore = mStore.putIfAbsent(i.Name(), i);
      if (!instore.isValid()) {
        mStore.put(i.Name(), i);
      }
    }
    return instore;
  }

  /**
   * @return a list of the names of all identities in alphabetic order
   */
  public List<Name> listNames() {
    List<Name> all = new LinkedList<>();
    all.addAll(mStore.keySet());
    //noinspection unchecked
    Collections.sort(all);
    return all;
  }

  public List<Identity> list() {
    List<Name> names = listNames();
    List<Identity> ids = new ArrayList<>(names.size());
    for (Name n : names) {
      Log.d(TAG, "listing identity: " + n.toUri());
      Identity id = get(n);
      if (null != id) {
        ids.add(id);
      }
    }
    return ids;
  }

  /**
   * @param prefix a name prefix to match
   * @return a list of names with the same prefix as given query, sorted in alphabetic order
   */
  public List<Name> queryByPrefix(Name prefix) {
    List<Name> all = new ArrayList<>(listNames());
    List<Name> filtered = new LinkedList<>();
    int index = Collections.binarySearch(all, prefix, new Comparator<Name>() {
      @Override
      public int compare(Name lhs, Name rhs) {
        if (rhs.isPrefixOf(lhs)) {
          return 0;
        }
        return lhs.compareTo(rhs);
      }
    });
    //noinspection StatementWithEmptyBody
    while (prefix.isPrefixOf(all.get(--index))) {
    }
    while (prefix.isPrefixOf(all.get(++index))) {
      filtered.add(all.get(index));
    }
    return filtered;
  }

  private ConcurrentMap<Name, Identity> mStore = new ConcurrentHashMap<>();
  private static final double CERT_PERIOD = 3.154e+7; // one year
}
