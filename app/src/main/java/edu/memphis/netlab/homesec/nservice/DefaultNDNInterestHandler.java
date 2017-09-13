package edu.memphis.netlab.homesec.nservice;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Objects;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.logging.Logger;

import edu.memphis.netlab.homesec.Constants;

/**
 * Created by lei on 1/31/17.
 */

public enum DefaultNDNInterestHandler implements OnInterestCallback, Constants {
  instance;

  /**
   * When an interest is received which matches the interest filter, onInterest
   * is called.
   *
   * @param prefix           The Name prefix given to registerPrefix or setInterestFilter
   *                         (or directly to the InterestFilter constructor). NOTE: You must not change
   *                         the prefix object - if you need to change it then make a copy.
   * @param interest         The received interest.
   * @param face             You should call face.putData to supply a Data packet which
   *                         satisfies the Interest.
   * @param interestFilterId The interest filter ID which can be used with
   *                         Face.unsetInterestFilter.
   * @param filter           The InterestFilter given to registerPrefix or
   *                         setInterestFilter, or the InterestFilter created from the Name prefix. The
   *                         first argument, prefix, is provided for convenience and is the same as
   *                         filter.getPrefix(). NOTE: You must not change the filter object - if you
   */
  @Override
  public void onInterest(final Name prefix, Interest interest, Face face,
                         long interestFilterId, InterestFilter filter) {
    Log.i("NFDC", "Received interest: " + interest.toUri());
    dispatchInterests(prefix, interest, face);
  }

  public interface OnBootstrapRequest {
    void onRequest(Face face, Name instName, String deviceId, String description);
  }

  public void setOnBootstrapRequestHandler(OnBootstrapRequest handler) {
    m_onBootstrapRequest = handler;
  }

  private void dispatchInterests(Name prefix, Interest inst, Face face) {
    final Name instName = inst.getName();
    final String root = instName.get(NAME_COMPONENT_ROOT).toEscapedString();

    if (Objects.equal(root, OWNER)) {
      dispatchOwnerInterests(instName, NAME_COMPONENT_ROOT + 1, inst, face);

    } else if (Objects.equal(root, BOOTSTRAP)) {
      if (!Objects.equal(instName.get(NAME_COMPONENT_ROOT + 1).toEscapedString(), OWNER)) {
        throw new AssertionError("Expecting prefix '" + PREFIX_OWNER_BOOTSTRAP
            + "', got: " + instName.toUri());
      }
      dispatchBootstrapInterests(instName, NAME_COMPONENT_ROOT + 2, inst, face);

    } else {
      reject(prefix, face, NOT_FOUND);
    }
  }

  private void dispatchOwnerInterests(Name instName, int baseVerbIdx, Interest inst, Face face) {
    reject(instName, face, NOT_FOUND);
  }

  private void reject(Name name, Face face, String reason) {
    Data d;
    try {
      d = NdnHelper.genereateTextData(name, reason);
    } catch (SecurityException e) {
      _logger.info("Cannot generate reject message, " + e.getMessage());
      return;
    }
    try {
      _logger.info("Rejecting: " + name.toUri() + " [" + reason + "]");
      // face.putData(d);
    } catch (Exception e) {
      _logger.info("Cannot sent reject message, " + e.getMessage());
      return;
    }
  }

  private void dispatchBootstrapInterests(Name instName, int baseVerbIdx, Interest inst, Face face) {
    final String bootstrapStage = instName.get(baseVerbIdx).toEscapedString();
    if (PUBKEY.equalsIgnoreCase(bootstrapStage)) {
      // /homesec/bootstrap/owner/for/devid[/description]
      String devId = instName.get(5).toEscapedString();
      String devDesc = instName.get(-1).toEscapedString();
      onInitBootstrapRequest(face, instName, devId, devDesc);
    } else {
      _logger.warning("Rejected: " + instName.toUri());
      reject(instName, face, INVALID_STATE);
    }
  }

  private void onInitBootstrapRequest(Face face, Name instName,
                                      @NonNull final String devId, final String description) {
    if (m_onBootstrapRequest != null) {
      m_onBootstrapRequest.onRequest(face, instName, devId, description);
    }
  }

  private OnBootstrapRequest m_onBootstrapRequest;

  private final Logger _logger = Logger.getLogger(DefaultNDNInterestHandler.class.getName());
}
