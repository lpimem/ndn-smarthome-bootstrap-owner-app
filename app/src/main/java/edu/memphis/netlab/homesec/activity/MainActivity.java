package edu.memphis.netlab.homesec.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import edu.memphis.netlab.homesec.Constants;
import edu.memphis.netlab.homesec.R;
import edu.memphis.netlab.homesec.nservice.HomeSecNFDClientService;
import edu.memphis.netlab.homesec.util.AndroidLoggingHandler;
import edu.memphis.netlab.homesec.util.UIHelper;

/**
 * App entrance. Based on auto-generated code.
 */
public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    log("resetting logging handlers...");
    AndroidLoggingHandler.resetAll();
    initView(savedInstanceState);
    initializeApp();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    log(String.format(Locale.ENGLISH, "onActivityResult-> %d, %d, %s.",
        requestCode, resultCode, String.valueOf(data)));
    log(String.format(Locale.ENGLISH, "RESULT_OK=%d", RESULT_OK));
    if (requestCode == ACTIVITY_RESULT_LOGIN) {
      if (resultCode == RESULT_OK) {
        this.isLoggedIn = true;
        token = data.getStringExtra(Constants.KEY_LOGIN_TOKEN);
        log(String.valueOf(token));
        initializeApp();
        appendMessage("Token: " + token);
      } else {
        log("login failed");
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    log("onResume");
    initializeApp();
//    if (!this.isLoggedIn) {
//      redirectToLogin();
//    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void redirectToLogin() {
    Intent intent = new Intent(this, LoginActivity.class);
    startActivityForResult(intent, ACTIVITY_RESULT_LOGIN);
  }

  private void initializeApp() {
    try {
      log("initializeApp()..." + String.valueOf(!isInited));
      if (isInited) {
        return;
      }
      startNDNClientService();
      appendMessage("Welcome...");
      isInited = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void startNDNClientService() {
    Intent request = new Intent(this, HomeSecNFDClientService.class);
    request.putExtra(Constants.KEY_LOGIN_TOKEN, this.token);
    this.nfdService = startService(request);
  }

  private void appendMessage(String message) {
    TextView textView = (TextView) findViewById(R.id.textView);
    assert null != textView;
    textView.append(message);
    if (!message.endsWith("\r\n")) {
      textView.append("\r\n");
    }
  }

  private void initViewVer1(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    UIHelper.registerOnClick(MainActivity.this, R.id.bt_add_device, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
        startActivity(intent);
      }
    });

    UIHelper.registerOnClick(MainActivity.this, R.id.bt_id_manager, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, IdentityListActivity.class);
        startActivity(intent);
      }
    });

    UIHelper.registerOnClick(MainActivity.this, R.id.bt_permissions, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, PermissionManagerActivity.class);
        startActivity(intent);
      }
    });

    UIHelper.registerOnClick(MainActivity.this, R.id.bt_main_service_list, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, ServiceListActivity.class);
        startActivity(intent);
      }
    });
  }

  private void initDrawerContent(Bundle savedInstanceState) {
    FragmentManager fragmentManager = getSupportFragmentManager();

    if (savedInstanceState != null) {
      mDrawerFragment = (DrawerFragment) fragmentManager.findFragmentByTag(DrawerFragment.class.toString());
    }

    if (mDrawerFragment == null) {
      ArrayList<DrawerFragment.DrawerItem> items = new ArrayList<>();

//      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_general, 0,
//          DRAWER_ITEM_GENERAL));
//      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_faces, 0,
//          DRAWER_ITEM_FACES));
//      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_routes, 0,
//          DRAWER_ITEM_ROUTES));
//      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_ping, 0,
//          DRAWER_ITEM_PING));
//      //    items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_strategies, 0,
//      //                                            DRAWER_ITEM_STRATEGIES));
//      items.add(new DrawerFragment.DrawerItem(R.string.drawer_item_logcat, 0,
//          DRAWER_ITEM_LOGCAT));

      mDrawerFragment = DrawerFragment.newInstance(items);

      fragmentManager
          .beginTransaction()
          .replace(R.id.navigation_drawer, mDrawerFragment, DrawerFragment.class.toString())
          .commit();
    }
  }

  private void initViewVer2(Bundle savedInstanceState) {
    setContentView(R.layout.activity_main2);
    initDrawerContent(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {
    initViewVer1(savedInstanceState);
//    initViewVer2(savedInstanceState);
  }

  private void log(String msg) {
    Log.d(">>> MAIN", msg);
  }


  /* **********  members  ********** */
  boolean isLoggedIn = false;
  boolean isInited = false;
  // TODO: use log in to generate token .
  private String token = "64c823fad1d87e0df1ef3cdeb8ac684f";

  private static final int ACTIVITY_RESULT_LOGIN = 1;
  private ComponentName nfdService;
  private DrawerFragment mDrawerFragment;
}
