package com.bcil.demoassettrack.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.SettingAdapter;
import com.bcil.demoassettrack.ui.activity.LoginActivity;
import com.bcil.demoassettrack.ui.activity.SettingsDetailActivity;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.bcil.demoassettrack.utils.SettingsContent;
import com.zebra.rfid.api3.Constants;

import java.util.Objects;

public class SettingListFragment extends ListFragment {
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private SettingAdapter adapter;
    private PreferenceManager preferenceManager;
    private String getStatus;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingListFragment() {
    }

    public static SettingListFragment newInstance() {
        SettingListFragment fragment = new SettingListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        adapter = new SettingAdapter(getActivity(), R.layout.setting_list, SettingsContent.ITEMS);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_STANDARD);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        //Change the icon
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.dl_sett);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_dpo);
    }

    @Override
    public void onResume() {
        super.onResume();
        settingsListUpdated();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Intent detailsIntent = new Intent(getActivity(), SettingsDetailActivity.class);
        detailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        detailsIntent.putExtra(Constants.SETTING_ITEM_ID, Integer.parseInt(SettingsContent.ITEMS.get(position).id));
        startActivity(detailsIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public void settingsListUpdated() {
        //
        /*if (MyApp.dynamicPowerSettings != null && MyApp.dynamicPowerSettings.getValue() == 1)
            SettingsContent.ITEMS.get(8).icon = R.drawable.title_dpo_enabled;
        else {
            SettingsContent.ITEMS.get(8).icon = R.drawable.title_dpo_disabled;
        }*/
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onSettingsItemSelected(String id);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return true;
            case R.id.action_logout:
//                preferenceManager.putPreferenceIntValues(AppConstants.LOGINSTATUS,0);
                preferenceManager.clearSharedPreferance();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
// You can hide the state of the menu item here if you call getActivity().supportInvalidateOptionsMenu(); somewhere in your code
        MenuItem menuItem = menu.findItem(R.id.action_setting);
        menuItem.setVisible(false);
    }
}

