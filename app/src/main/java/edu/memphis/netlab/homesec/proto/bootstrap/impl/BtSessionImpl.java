package edu.memphis.netlab.homesec.proto.bootstrap.impl;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.util.Blob;

import java.io.IOException;
import java.util.logging.Logger;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.security.BlockCipher;
import edu.memphis.netlab.homesec.security.SecurityManager;
import edu.memphis.netlab.homesec.util.StringHelper;

/**
 * Description:
 * <p>
 * Date: 3/20/17
 * Author: lei
 */

public class BtSessionImpl {

  public BtSessionImpl() {

  }

  public byte[] encrypt(byte[] message, byte[] key) throws BlockCipher.EncryptionError {
    BlockCipher cipher = new BlockCipher(Constants.KEY_STRENGTH, key);
    return cipher.encrypt(message);
  }

  public void sendMessage(Face f, Name name, byte[] message, long freshPeriod)
      throws SecurityException, IOException {
    Blob blob = new Blob(message, true);
    Data d = new Data();
    d.setName(name);
    d.setContent(blob);
    MetaInfo meta = new MetaInfo();
    meta.setFreshnessPeriod(freshPeriod);
    d.setMetaInfo(meta);
    SecurityManager.INSTANCE.getKeyChain().sign(d);
    _logger.info(String.format("Data [%s]\r\n\tContent: %s",
        d.getName(),
        StringHelper.toHex(d.getContent().getImmutableArray())));
    f.putData(d);
  }

  private final Logger _logger = Logger.getLogger(BtSessionImpl.class.getName());
}
