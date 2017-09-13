package edu.memphis.netlab.homesec.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.R;
import edu.memphis.netlab.homesec.nservice.HomeSecNFDClientService;
import edu.memphis.netlab.homesec.util.StringHelper;

public class UserPanelActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_panel);
    initView();
  }

  @Override
  protected void onStart(){
    super.onStart();
    this.bindHomeSecNdnService();
  }

  @Override
  protected void onStop(){
    super.onStop();
    this.unBindHomeSecNdnService();
  }

  private void initView(){
    m_getTemp = (Button) findViewById(R.id.bt_up_temp);
    m_getTemp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Name name = new Name(Constants.PREFIX_LOCAL_HOME + "/client/temp/get");
        appendMessage("out: " + name.toUri());
        mService.expressInterest(name, new OnData() {
          @Override
          public void onData(Interest interest, Data data) {
            final String cipher = data.getContent().toString();
            appendMessage("getTemp: received cipher: " + StringHelper.toHex(cipher.getBytes()));
            Name kdkName = new Name(Constants.PREFIX_LOCAL_HOME + "/group-manager/kdk/" + m_id);
            appendMessage("requesting c-key: " + kdkName.toUri());
            mService.expressInterest(kdkName, new OnData() {
              @Override
              public void onData(Interest interest, Data data) {
                appendMessage("kdk : " + data.getContent());
                if (data.getContent().toString().length() > 0){
                  appendMessage("decrypted cipher : " + decrypt(cipher));
                } else {
                  appendMessage("Cannot fetch kdk");
                }
              }
            });
          }
        });
      }
    });
    m_switchLight = (Button) findViewById(R.id.bt_up_light);
    m_switchLight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String trigger = "0";
        if (m_light_status == 0){
          trigger = "1";
        }
        Name name = new Name(Constants.PREFIX_LOCAL_HOME + "/client/led/set/" + trigger);
        mService.expressInterest(name, new OnData() {
          @Override
          public void onData(Interest interest, Data data) {
            appendMessage(data.getContent().toString());
            try {
              m_light_status = Integer.parseInt(data.getContent().toString());
            } catch (Exception e){
              appendMessage("CMD: [switching light] failed.");
            }
          }
        });
      }
    });
  }

  private void appendMessage(String message){
    TextView textView = (TextView) findViewById(R.id.user_panel_log);
    assert null != textView;
    textView.append(message);
    if (!message.endsWith("\r\n")) {
      textView.append("\r\n");
    }
  }

  /* **********  Service Bind ********** */
  private void bindHomeSecNdnService(){
    Intent intent = new Intent(this, HomeSecNFDClientService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  private void unBindHomeSecNdnService(){
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

  private String decrypt(String c)  {
    String[] parts = c.split("|");
    if (parts.length < 1){
      return "";
    }
    String nonce = parts[1];
    return "76|" + nonce;
  }

  /* ********** Private Members ********** */
  private Button m_getTemp;
  private Button m_switchLight;
  private boolean  enabled =  false;
  private int m_light_status = 0;
  private String m_id = "042bef24e665aba9e5aac6a2a1df7000dc5329edfae91b747a2768c98840d0631170bdbb449770818ac054c41df12d20496b57c8ebd5ed309d6d79b1bc42f7e14b";
}
