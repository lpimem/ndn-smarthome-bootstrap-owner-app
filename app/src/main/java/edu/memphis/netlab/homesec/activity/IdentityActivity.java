package edu.memphis.netlab.homesec.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.R;
import edu.memphis.netlab.homesec.nservice.HomeSecNFDClientService;


public class IdentityActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_identity);
    bindView();
  }

  @Override
  protected void onStart() {
    super.onStart();
    this.bindHomeSecNdnService();
  }

  @Override
  protected void onStop() {
    super.onStop();
    this.unBindHomeSecNdnService();
  }

  private void onAsAlice(View v) {
    Intent intent = new Intent(IdentityActivity.this, UserPanelActivity.class);
    intent.putExtra("name", "Alice");
    intent.putExtra("temp", mSwTemp.isChecked());
//    intent.putExtra("light", mSwLight.isChecked());
    startActivity(intent);
  }

  private void onTempChecked() {
    Name name = new Name(Constants.PREFIX_LOCAL_HOME + "/group-manager/auth/alice/allow");
    mService.expressInterest(name, new OnData() {
      @Override
      public void onData(Interest interest, Data data) {
        snackMsg("Success");
      }
    });
  }

  private void snackMsg(String msg) {
    View view = findViewById(R.id.addDevice);
    Snackbar.make(view,
        msg,
        Snackbar.LENGTH_LONG)
        .setAction("Action", null)
        .show();
  }

  private void onTempUnchecked() {
  }

//  private void onLightChecked(){
//  }
//
//  private void onLightUnChecked(){
//  }

  private void bindView() {
    mBtAsAlice = (Button) findViewById(R.id.bt_as_alice);
    mBtAsAlice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onAsAlice(v);
      }
    });
    mTvId = (TextView) findViewById(R.id.tv_alice_id);
    mTvId.setText("042bef24e665aba9e5aac6a2a1df7000dc5329edfae91b747a2768c98840d0631170bdbb449770818ac054c41df12d20496b57c8ebd5ed309d6d79b1bc42f7e14b");
    mSwTemp = (Switch) findViewById(R.id.sw_temp);
    mSwTemp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          onTempChecked();
        } else {
          onTempUnchecked();
        }
      }
    });

//    mSwLight = (Switch) findViewById(R.id.sw_light);
//    mSwLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//      @Override
//      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if(isChecked){
//          onLightChecked();
//        } else {
//          onLightUnChecked();
//        }
//      }
//    });
  }

  /* **********  Service Bind ********** */
  private void bindHomeSecNdnService() {
    Intent intent = new Intent(this, HomeSecNFDClientService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  private void unBindHomeSecNdnService() {
    if (mBound) {
      unbindService(mConnection);
      mBound = false;
    }
  }

  /* **** Service Bind **** */
  private HomeSecNFDClientService mService;
  private boolean mBound = false;
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      HomeSecNFDClientService.LocalBinder binder = (HomeSecNFDClientService.LocalBinder) service;
      mService = binder.getService();
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  private Button mBtAsAlice;
  private TextView mTvId;
  private Switch mSwTemp;
}
