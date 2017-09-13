package edu.memphis.netlab.homesec.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.memphis.netlab.homesec.R;
import edu.memphis.netlab.homesec.security.Identity;
import edu.memphis.netlab.homesec.security.IdentityManager;

/**
 * Description:
 * <p>
 * Author: lei
 */

public class IdListFragment extends ListFragment {

  public static IdListFragment newInstance() {
    return new IdListFragment();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    @SuppressLint("InflateParams")
    View v = getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_id_list_list_header, null);
    getListView().addHeaderView(v, null, false);
    getListView().setDivider(getResources().getDrawable(R.drawable.list_item_divider));

    m_progressbar = (ProgressBar) v.findViewById(R.id.id_list_reloading_list_progress_bar);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (m_listAdapter == null) {
      m_listAdapter = new IdListAdapter(getActivity());
    }
    setListAdapter(m_listAdapter);
    setEmptyText("no identity found");
    setListShown(true);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    setListAdapter(null);
  }

  @Override
  public void onResume() {
    super.onResume();
    setEmptyText("no identity found");
    setListShown(true);
    startFetchingIDListTask();
  }

  @Override
  public void onPause() {
    super.onPause();
    stopFetchingIDListTask();
  }

  private void updateList(List<Identity> ids) {
    ((IdListAdapter) getListAdapter()).updateList(ids);
  }

  private class IDListAsyncTask extends AsyncTask<Void, Void, Pair<List<Identity>, Exception>> {

    @Override
    protected Pair<List<Identity>, Exception> doInBackground(Void... params) {
      Log.d("IDListAsyncTask", "doInBackground: start ... ");
      List<Identity> ids = null;
      Exception err = null;
      try {
        ids = IdentityManager.INSTANCE.list();
        Log.d("IDListAsyncTask ", ids.size() + " identites listed");
      } catch (Exception e) {
        _logger.severe("IDListAsyncTask: " + e.getMessage());
        err = e;
      }
      Log.d("IDListAsyncTask", "doInBackground: end ... ");
      return new Pair<>(ids, err);
    }

    @Override
    protected void onCancelled() {
      m_progressbar.setVisibility(View.GONE);
    }

    @Override
    protected void onPostExecute(Pair<List<Identity>, Exception> result) {
      m_progressbar.setVisibility(View.GONE);

      if (result.second != null) {
        Toast.makeText(getActivity(),
            "Error querying identity list (" + result.second.getMessage() + ")",
            Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(getActivity(),
            "Identities loaded.",
            Toast.LENGTH_LONG).show();
      }

      updateList(result.first);
      setListShown(true);
    }
  }

  private void startFetchingIDListTask() {
    m_progressbar.setVisibility(View.VISIBLE);
    m_fetchIDListTask = new IDListAsyncTask();
    m_fetchIDListTask.execute();
  }

  private void stopFetchingIDListTask() {
    if (null != m_fetchIDListTask) {
      final boolean interrupt = false;
      m_fetchIDListTask.cancel(interrupt);
      m_fetchIDListTask = null;
    }
  }

  private static class IdListAdapter extends BaseAdapter {

    private IdListAdapter(Context context) {
      m_layoutInflater = LayoutInflater.from(context);
      m_identities = new LinkedList<>();
    }

    @Override
    public int getCount() {
      return m_identities.size();
    }

    @Override
    public Identity getItem(int position) {
      if (position >= m_identities.size()) {
        return null;
      }
      return m_identities.get(position);
    }

    @Override
    public long getItemId(int position) {
      if (position >= m_identities.size()) {
        return -1;
      }
      return m_identities.get(position).Id();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      IdInfoHolder holder;
      if (convertView == null) {
        holder = new IdInfoHolder();
        convertView = m_layoutInflater.inflate(R.layout.list_item_identity, null);
        convertView.setTag(holder);
        holder.m_name = (TextView) convertView.findViewById(R.id.list_item_identity_name);
        holder.m_issuer = (TextView) convertView.findViewById(R.id.list_item_identity_cert_issuer);
        holder.m_validStart = (TextView) convertView.findViewById(R.id.list_item_identity_valid_start);
        holder.m_validEnd = (TextView) convertView.findViewById(R.id.list_item_identity_valid_end);
      } else {
        holder = (IdInfoHolder) convertView.getTag();
      }
      Identity id = getItem(position);
      holder.m_name.setText(id.Name().toUri());
      holder.m_issuer.setText(id.Certificate().getName().toUri());
      Date d = new Date((long) Math.ceil(id.Certificate().getNotBefore()));
      holder.m_validStart.setText(d.toString());
      holder.m_validEnd.setText(new Date((long) Math.ceil(id.Certificate().getNotAfter())).toString());
      return convertView;
    }

    private void updateList(List<Identity> ids) {
      m_identities = ids;
      notifyDataSetChanged();
    }

    private static class IdInfoHolder {
      private TextView m_name;
      private TextView m_issuer;
      private TextView m_validStart;
      private TextView m_validEnd;
    }

    private final LayoutInflater m_layoutInflater;
    private List<Identity> m_identities;
  }

  private IdListAdapter m_listAdapter;
  private IDListAsyncTask m_fetchIDListTask;
  private ProgressBar m_progressbar;

  private final Logger _logger = Logger.getLogger(IdListFragment.class.getName());
}
