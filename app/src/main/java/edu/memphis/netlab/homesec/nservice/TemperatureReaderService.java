package edu.memphis.netlab.homesec.nservice;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;

import net.named_data.jndn.Name;
import edu.memphis.cs.netlab.jnacconsumer.TemperatureReader;
import edu.memphis.cs.netlab.nacapp.AndroidConsumerSQLiteDBSource;

import edu.memphis.netlab.homesec.util.ThreadHelper;

public class TemperatureReaderService extends NodeService {
	private static final String TAG = TemperatureReaderService.class.getName();

	public TemperatureReaderService() {
		super();
	}

	public static Intent newIntent(Context context, String group, String name, String dbPath) {
		Intent intent = new Intent(context, TemperatureReaderService.class);
		intent.putExtra(INTENT_KEY_GROUP, group);
		intent.putExtra(INTENT_KEY_NAME, name);
		intent.putExtra(INTENT_KEY_DB_PATH, dbPath);
		return intent;
	}

	public void registerIdentity(Runnable onSuccess) {
		m_temperatureReader.registerIdentity(onSuccess);
	}

	public void requestGrantPermission(String location, Runnable onSuccess, Runnable onFail) {
		m_temperatureReader.requestGrantPermission(location, onSuccess, onFail);
	}

	public void fetchTemperature(String location, final TemperatureReader.OnDataCallback callback)
		throws IOException {
		ThreadHelper.scheduledThreadPool.submit(new FetchTempTask(new Name(location), callback));
	}

	private class FetchTempTask implements Runnable {
		private final Name m_location;
		private final TemperatureReader.OnDataCallback m_callback;

		FetchTempTask(Name location, TemperatureReader.OnDataCallback callback) {
			m_location = location;
			m_callback = callback;
		}

		@Override
		public void run() {
			m_temperatureReader.read(m_location, m_callback);
		}
	}

	////////////////////////////////////////////////////////

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (m_temperatureReader == null) {
			Name group = new Name(intent.getStringExtra(INTENT_KEY_GROUP));
			Name name = new Name(intent.getStringExtra(INTENT_KEY_NAME));
			String dbPath = intent.getStringExtra(INTENT_KEY_DB_PATH);

			m_temperatureReader = new TemperatureReader(group);
			m_temperatureReader.init(name, new AndroidConsumerSQLiteDBSource(dbPath));
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (!m_is_running) {
			startService(intent);
			m_temperatureReader.startFaceProcessing();
		}
		m_is_running = true;
		return m_localBinder;
	}

	public class LocalBinder extends Binder {
		public TemperatureReaderService getService() {
			return TemperatureReaderService.this;
		}
	}

	////////////////////////////////////////////////////////

	private static final String INTENT_KEY_GROUP = "GROUP";
	private static final String INTENT_KEY_NAME = "NAME";
	private static final String INTENT_KEY_DB_PATH = "DB_PATH";

	private static boolean m_is_running = false;

	private IBinder m_localBinder = new LocalBinder();

	private TemperatureReader m_temperatureReader;
}
