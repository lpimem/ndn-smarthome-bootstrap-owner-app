package edu.memphis.netlab.homesec.security;

import android.support.annotation.NonNull;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.EcdsaKeyParams;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.UnrecognizedKeyFormatException;
import net.named_data.jndn.security.certificate.IdentityCertificate;
import net.named_data.jndn.security.certificate.PublicKey;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.IdentityStorage;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;

import edu.memphis.netlab.homesec.Constants;

/**
 * Description:
 * Manage and verify identities.
 * Date: 2/9/17
 * Author: lei
 */
public enum SecurityManager {
  // thread-safe singleton
  INSTANCE;

  SecurityManager() {
    ownerName = new Name(Constants.PREFIX_LOCAL_HOME + Constants.PREFIX_OWNER);
    // see: void init(Face face) function.
  }

  /**
   * Initialize IdentiyManager, KeyRing, and default identity, key and certificate.
   *
   * @param face face to use
   */

  public void init(final Face face) {
    Log.d(TAG, "Initing... ");
    this.face = face;

    IdentityStorage identityStorage = new MemoryIdentityStorage();
    identityManager = new IdentityManager(
        identityStorage, new MemoryPrivateKeyStorage());
    this.kskName = getOwnerKskName(identityManager);
    this.dskName = generateDsk();
    try {
      keyChain = new KeyChain(
          identityManager,
          new SelfVerifyPolicyManager(identityStorage));
      // set face for fetching required certficates
      keyChain.setFace(face);
      generateSelfIdentity(identityManager);
      this.face.setCommandSigningInfo(
          keyChain, identityManager.getDefaultCertificateName());
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    }
    Log.d(TAG, "inited.");
  }

  public KeyChain getKeyChain() {
    return keyChain;
  }

  public Name getDskName() {
    return dskName;
  }

  public Name getKskName() {
    return kskName;
  }

  private void generateSelfIdentity(IdentityManager identityManager) {
    try {
      identityManager.createIdentityAndCertificate(
          ownerName, new EcdsaKeyParams(Constants.DEFAULT_PUBLIC_KEY_SIZE));
      identityManager.setDefaultIdentity(ownerName);
      IdentityCertificate ic = identityManager.getDefaultCertificate();
      edu.memphis.netlab.homesec.security.IdentityManager.INSTANCE
          .create(ownerName, ic);
    } catch (SecurityException e) {
      Log.e(TAG, "Cannot create owner Identity: " + e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @NonNull
  private Name getOwnerKskName(IdentityManager identityManager) {
    //noinspection ConstantConditions
    assert null != ownerName;
    Name kname = null;
    try {
      kname = identityManager.getDefaultKeyNameForIdentity(ownerName);
      Log.d(TAG, "KsK for owner found: " + kname.toUri());
    } catch (SecurityException ignored) {
      Log.d(TAG, "no existing KsK for owner found, creating new one.");
    }
    PublicKey k = null;
    try {
      if (kname != null) {
        try {
          // Notes: if kname is not found in storage, API will use
          // an empty Blog object to generate a key.
          // Questions: (TODO)
          //   1. Will that raise an UnrecognizedKeyFormatException?
          //   2. If not, how can we know if the key exists or not ?
          k = identityManager.getPublicKey(kname);
          Log.d(TAG, "loaded key : " + kname.toUri());
        } catch (UnrecognizedKeyFormatException e) {
          Log.w(TAG, e.getMessage() + " (This error is ignored)");
        }
      }
      if (kname == null || k == null) {
        kname = identityManager.generateEcdsaKeyPairAsDefault(
            ownerName, true, Constants.DEFAULT_PUBLIC_KEY_SIZE);
        identityManager.selfSign(kname);
        Log.d(TAG, "Generated key pair for " + ownerName.toUri() + ": " + kname.toUri());
      }
    } catch (SecurityException e) {
      Log.e(TAG, "Cannot get / generate owner Ksk", e);
      throw new RuntimeException(e);
    }
    return kname;
  }

  @NonNull
  private Name generateDsk() {
    try {
      Name kname = identityManager.generateEcdsaKeyPair(
          ownerName, false, Constants.DEFAULT_PUBLIC_KEY_SIZE);
      Log.d(TAG, "generated dsk for " + ownerName.toUri() + " : " + kname.toUri());
      return kname;
    } catch (SecurityException e) {
      Log.e(TAG, "Cannot generate Dsk for owner", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Create an identity by creating a pair of Key-Signing-Key (KSK) for this
   * identity and a self-signed certificate of the KSK. If a key pair or
   * certificate for the identity already exists, use it.
   *
   * @param name: identity name
   * @return name of the certificate for given identity's key
   */
  public Name createIdentityForName(Name name) {
    try {
      // try to create new identity.
      return identityManager.createIdentityAndCertificate(
          name, new EcdsaKeyParams(Constants.DEFAULT_PUBLIC_KEY_SIZE));
    } catch (SecurityException e) {
      try {
        // an identity is found in the storage, we will just return its default k's default cert.
        return identityManager.getDefaultCertificateNameForIdentity(name);
      } catch (SecurityException ignore) {
        // the certificate is deleted. return null.
        return null;
      }
    }
  }

  public PublicKey getOwnerPubkey() {
    try {
      Name kname = identityManager.getDefaultKeyNameForIdentity(ownerName);
      return identityManager.getPublicKey(kname);
    } catch (SecurityException e) {
      Log.e(TAG, "Cannot get owner public key: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private final static String TAG = SecurityManager.class.getName();
  private final Name ownerName;
  private Name kskName;
  private Name dskName;
  private KeyChain keyChain;
  private Face face;
  private IdentityManager identityManager = null;
}
