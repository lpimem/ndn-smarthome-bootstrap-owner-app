package edu.memphis.netlab.homesec.nservice;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.ThreadPoolFace;
import net.named_data.jndn.transport.TcpTransport;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.proto.BootstrapSessionManager;
import edu.memphis.netlab.homesec.proto.BootstrapSessionNotFound;
import edu.memphis.netlab.homesec.proto.bootstrap.BootstrapStage;
import edu.memphis.netlab.homesec.security.SecurityManager;
import edu.memphis.netlab.homesec.util.ThreadHelper;

/**
 * Description:
 * <p>
 * Date: 3/21/17
 * Author: lei
 */

public abstract class HomeSecNFDClientServiceImpl extends Service {
  public HomeSecNFDClientServiceImpl() {
    m_face = new Face();
    m_securityMgr.init(m_face);
  }

  protected NFDClientDaemon initNFDClientDaemon(Face face, String token) {
    Log.d("TAG", "generating new nfd client daemon...");
    NFDClientDaemon d = new NFDClientDaemon(face, token);
    d.addPrefixHandler(
        new Name(Constants.PREFIX_LOCAL_HOME + Constants.PREFIX_OWNER),
        DefaultNDNInterestHandler.instance
    );

    d.addPrefixHandler(
        new Name(Constants.PREFIX_OWNER_BOOTSTRAP),
        DefaultNDNInterestHandler.instance
    );
    return d;
  }

  protected void configDefaultInterestHandlers() {
    final Service _this = this;
    DefaultNDNInterestHandler.instance.setOnBootstrapRequestHandler(
        new DefaultNDNInterestHandler.OnBootstrapRequest() {
          @Override
          public void onRequest(Face face, Name instName, String deviceId, String description) {
            try {
              BootstrapSessionManager.INSTANCE.getSession(deviceId);
              return;
            } catch (BootstrapSessionNotFound expected) {
            }
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION_DEVICE_BT)
                .putExtra(Constants.BOOTSTRAP_DEVICE_ID, deviceId)
                .putExtra(Constants.BOOTSTRAP_DEVICE_DESCRIPTION, description);
            LocalBroadcastManager.getInstance(_this).sendBroadcast(localIntent);
          }
        });
  }

  protected final Face m_face;
  protected final static String TAG = "NFDCService";
  protected SecurityManager m_securityMgr = SecurityManager.INSTANCE;
  protected NFDClientDaemon m_nfdcDaemon = null;
}
