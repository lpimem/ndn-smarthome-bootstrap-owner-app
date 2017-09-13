package edu.memphis.netlab.homesec.activity;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.memphis.netlab.homesec.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PermissionsActivityFragment extends ListFragment {

  public PermissionsActivityFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_permissions, container, false);
  }
}
