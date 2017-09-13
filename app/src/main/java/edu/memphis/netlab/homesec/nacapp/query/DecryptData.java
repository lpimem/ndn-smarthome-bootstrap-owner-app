package edu.memphis.netlab.homesec.nacapp.query;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.util.Blob;

import java.util.List;

import edu.memphis.netlab.homesec.security.BlockCipher;

/**
 * Description:
 * <p>
 * Author: lei
 */

public abstract class DecryptData extends OnConsume {

  DecryptData newAESDecryptor(final Blob key) {
    return new DecryptData() {
      @Override
      public void process(Context ctx, Interest in, Data data, List<OnConsume> following) throws ConsumptionError {
        Data d = decrypt(ctx, in, data);
        DecryptData.handleNext(following, ctx, in, data);
      }

      private Data decrypt(Context ctx, Interest in, Data data) {
        try {
          byte[] plainText = mCipher.decrypt(data.getContent().getImmutableArray());
          Data decrypted = new Data(data.getName());
          decrypted.setContent(new Blob(plainText));
          decrypted.setMetaInfo(data.getMetaInfo());
          // signature is removed.
          return decrypted;
        } catch (BlockCipher.DecryptionError decryptionError) {
          throw new ConsumptionError(decryptionError);
        }
      }

      private BlockCipher mCipher = new BlockCipher(key.size(), key.buf().array());
    };
  }

  public static DecryptData newRSADecryptor() {
    throw new RuntimeException("Not Implemented");
  }

  public static DecryptData newECDSADecryptor() {
    throw new RuntimeException("Not Implemented");
  }

}
