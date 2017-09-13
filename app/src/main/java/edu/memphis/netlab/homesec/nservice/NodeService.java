package edu.memphis.netlab.homesec.nservice;

import android.content.Context;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import net.named_data.jndn.Name;

import edu.memphis.cs.netlab.nacapp.NACNode;

public class NodeService extends Service {
	////////////////////////////////////////////////////////
	// Android Service LifeCycle API
	////////////////////////////////////////////////////////
	@Override
	public void onCreate() {
		m_node = new NACNode();
	}

	public static Intent newIntent(Context ctx, String appPrefix) {
		Intent intent = new Intent(ctx, NodeService.class);
		intent.putExtra(INTENT_KEY_APP_PREFIX, appPrefix);
		return intent;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String appPrefix = intent.getStringExtra(INTENT_KEY_APP_PREFIX);
		if (appPrefix == null || appPrefix.trim() == "") {
			throw new RuntimeException("Cannot start NodeService for empty app prefix");
		}
		m_node.init(new Name(appPrefix));
		m_node.startFaceProcessing();
		return START_STICKY;
	}

	////////////////////////////////////////////////////////

	@Override
	public IBinder onBind(Intent intent) {
		return m_localBinder;
	}

	public class LocalBinder extends Binder {
		public NodeService getService() {
			return NodeService.this;
		}
	}

	private static final String INTENT_KEY_APP_PREFIX = "APP_PREFIX";
	private final static String TAG = NodeService.class.getName();

	protected NACNode m_node;

	private LocalBinder m_localBinder = new LocalBinder();
}