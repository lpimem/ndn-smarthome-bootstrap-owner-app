package edu.memphis.netlab.homesec.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.nservice.HomeSecNFDClientService;
import edu.memphis.netlab.homesec.R;

/**
 * Description:
 * <p>
 * Date: 3/2/17
 * Author: lei
 */

public class AddDeviceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_device);
		bindViews();
		addEventListeners();
		registerBroadcastReceivers();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, HomeSecNFDClientService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private void addDevice(String did, String pin) {
		if (mBound) {
			Log.i(TAG, "start adding new device: " + did);
			mService.addDevice(did, pin);
			snackMsg("Start bootstrapping " + did + "...");
			clearInputs();
		} else {
			final String msg = "Service not bound. Cannot add device.";
			snackMsg(msg);
			Log.e(TAG, msg);
		}
	}

	private void snackMsg(String msg) {
		View view = findViewById(R.id.addDevice);
		Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
	}

	private void bindViews() {
		mDeviceIdView = (EditText) findViewById(R.id.devId);
		mPinView = (EditText) findViewById(R.id.pin);
		mAddButtonBt = (Button) findViewById(R.id.addDevice);
		mAddButtonBt.setEnabled(false);
	}

	private void addEventListeners() {
		mDeviceIdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				mPinView.requestFocus();
				return false;
			}
		});

		mAddButtonBt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddDeviceButtonClicked();
			}
		});
	}

	private void onAddDeviceButtonClicked() {
		String id = mDeviceIdView.getText().toString();
		String pin = mPinView.getText().toString();
		if (Strings.isNullOrEmpty(id) || Strings.isNullOrEmpty(pin)) {
			snackMsg("Device ID and Pin must not be empty.");
			return;
		}
		addDevice(id, pin);
	}

	private void clearInputs() {
		mDeviceIdView.setText("");
		//    mPinView.setText("");
	}

	/**
	 * Defines callbacks for service binding, passed to bindService()
	 */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			HomeSecNFDClientService.LocalBinder binder =
				(HomeSecNFDClientService.LocalBinder) service;
			mService = binder.getService();
			mAddButtonBt.setEnabled(true);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	private class BTRequestReceiver extends BroadcastReceiver {
		private BTRequestReceiver() {}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mBtDialogOpen) {
				return;
			}
			mBtDialogOpen = true;
			final String devId = intent.getStringExtra(Constants.BOOTSTRAP_DEVICE_ID);
			final String desc = intent.getStringExtra(Constants.BOOTSTRAP_DEVICE_DESCRIPTION);
			String message = "Do you want to add device " + devId + "?";
			if (!Objects.equal(devId, desc) && !Strings.isNullOrEmpty(desc)) {
				message += "\r\n" + desc;
			}
			new AlertDialog.Builder(AddDeviceActivity.this)
				.setTitle("Bootstrap Request")
				.setMessage(message)
				.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mDeviceIdView.setText(devId);
							onAddDeviceButtonClicked();
							mBtDialogOpen = false;
						}
					})
				.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mBtDialogOpen = false;
							// do nothing
						}
					})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
		}
	}

	private class BTResultSuccessReceiver extends BroadcastReceiver {
		private BTResultSuccessReceiver() {}

		@Override
		public void onReceive(Context context, Intent intent) {
			String deviceId = intent.getStringExtra(Constants.BOOTSTRAP_DEVICE_ID);
			snackMsg(String.format("Success: %s added", deviceId));
		}
	}

	private class BTResultFailReceiver extends BroadcastReceiver {
		private BTResultFailReceiver() {}

		@Override
		public void onReceive(Context context, Intent intent) {
			String reason = intent.getStringExtra(Constants.BROADCAST_ACTION_DEVICE_BT_MSG);
			snackMsg(String.format("Fail: %s", reason));
		}
	}

	private void registerBroadcastReceivers() {
		LocalBroadcastManager.getInstance(this).registerReceiver(
			new BTRequestReceiver(), new IntentFilter(Constants.BROADCAST_ACTION_DEVICE_BT));

		LocalBroadcastManager.getInstance(this).registerReceiver(new BTResultSuccessReceiver(),
			new IntentFilter(Constants.BROADCAST_ACTION_DEVICE_BT_SUC));

		LocalBroadcastManager.getInstance(this).registerReceiver(new BTResultFailReceiver(),
			new IntentFilter(Constants.BROADCAST_ACTION_DEVICE_BT_FAIL));
	}

	private static final String TAG = AddDeviceActivity.class.getName();

	HomeSecNFDClientService mService;
	boolean mBound = false;
	private EditText mDeviceIdView;
	private EditText mPinView;
	private Button mAddButtonBt;
	private boolean mBtDialogOpen = false;
}
