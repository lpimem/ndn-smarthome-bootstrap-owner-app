package edu.memphis.netlab.homesec.activity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import edu.memphis.netlab.homesec.R;


public class SideDrawerFragment extends Fragment {

  public SideDrawerFragment() {
    // Required empty public constructor
  }

  public static SideDrawerFragment newInstance(ArrayList<SideDrawerItem> items) {
    SideDrawerFragment fragment = new SideDrawerFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(ARG_DRAWER_ITEMS, items);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      mDrawerSelectedPosition = savedInstanceState.getInt(DRAWER_SELECTED_POSITION_BUNDLE_KEY);
    }
    if (getArguments() != null) {
      mItemList = getArguments().getParcelableArrayList(ARG_DRAWER_ITEMS);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View vw = inflater.inflate(R.layout.fragment_side_drawer, container, false);

    mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Update UI
        updateSelection(position);
      }
    });

//    mDrawerListView.setAdapter(new DrawerListAdapter(getActivity().getActionBar().getThemedContext(), mItemList));
//    mDrawerListView.setItemChecked(m_drawerSelectedPosition, true);

    return vw;
  }

  private void updateSelection(int position) {
    // TODO
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(DRAWER_SELECTED_POSITION_BUNDLE_KEY, mDrawerSelectedPosition);
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Activity act) {
    super.onAttach(act);
    if (act instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) act;
    } else {
      throw new RuntimeException(act.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }

  public static class SideDrawerItem implements Parcelable {

    protected SideDrawerItem(Parcel in) {
      this.mItemNameStringId = in.readInt();
      this.mIconResId = in.readInt();
      this.mItemCode = in.readInt();
    }

    public int getItemNameStringId() {
      return mItemNameStringId;
    }

    public int getIconResId() {
      return mIconResId;
    }

    public int getItemCode() {
      return mItemCode;
    }

    public static final Creator<SideDrawerItem> CREATOR = new Creator<SideDrawerItem>() {
      @Override
      public SideDrawerItem createFromParcel(Parcel in) {
        return new SideDrawerItem(in);
      }

      @Override
      public SideDrawerItem[] newArray(int size) {
        return new SideDrawerItem[size];
      }
    };

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(mItemNameStringId);
      dest.writeInt(mIconResId);
      dest.writeInt(mItemCode);
    }

    private final int mItemNameStringId;
    private final int mIconResId;
    private final int mItemCode;
  }

  private static final String DRAWER_SELECTED_POSITION_BUNDLE_KEY = "DRAWER_SELECTED_POSITION";
  private static final String ARG_DRAWER_ITEMS = "drawer_items";
  private ArrayList<SideDrawerItem> mItemList;
  private OnFragmentInteractionListener mListener;
  private int mDrawerSelectedPosition;
  private ListView mDrawerListView;

//  private class DrawerListAdapter extends ArrayAdapter<SideDrawerItem> {
//    /**
//     * Constructor
//     *
//     * @param context  The current context.
//     * @param resource The resource ID for a layout file containing a TextView to use when
//     *                 instantiating views.
//     * @param objects  The objects to represent in the ListView.
//     */
//    public DrawerListAdapter(@NonNull Context context, @NonNull ArrayList<SideDrawerItem> objects) {
//      super(context, 0, objects);
//    }
//  }
}
