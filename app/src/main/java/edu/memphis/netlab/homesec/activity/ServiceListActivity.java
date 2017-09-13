package edu.memphis.netlab.homesec.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.memphis.netlab.homesec.R;


/**
 * Access to a list of available services
 */
public class ServiceListActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_service_list);
  }
}
