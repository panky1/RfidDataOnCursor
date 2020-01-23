package com.bcil.demoassettrack.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bcil.demoassettrack.OnRefreshListener;
import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.CustomAssetIdAdapter;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.common.ResponseHandlerInterfaces;
import com.bcil.demoassettrack.common.Task_SaveAntennaConfiguration;
import com.bcil.demoassettrack.database.DatabaseHandler;
import com.bcil.demoassettrack.inventory.InventoryListItem;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.network.DataSelections;
import com.bcil.demoassettrack.ui.activity.LoginActivity;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CommonUtils;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.zebra.rfid.api3.RFIDResults;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class MappingFragment extends Fragment implements ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler, OnRefreshListener {

    @Bind(R.id.etAssetNo)
    EditText etAssetNo;
    @Bind(R.id.etScan)
    EditText etScan;
    @Bind(R.id.aTAstId)
    AutoCompleteTextView aTAstId;
    @Bind(R.id.aTAstDsc)
    AutoCompleteTextView aTAstDsc;
    @Bind(R.id.btStart)
    Button btStart;
    public boolean scanstatus;
    private PreferenceManager preferenceManager;
    private List<AssetInfo> assetInfoList;
    private String getUserName;
    private DatabaseHandler databaseHandler;
    private AssetInfo assetInfo;
    private ArrayList<InventoryListItem> searchItemsList = new ArrayList<>();
    private String getRfidData = null;
    private int linkedProfileIndex;
    public String tagIDField;
    public String offset;
    public String writeData;
    private int status;
    private ArrayList<AssetInfo> assetInfoList1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapping, container, false);
        ButterKnife.bind(this, view);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        setHasOptionsMenu(true);
        initView();
        initData();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
        if (MyApp.mIsInventoryRunning)
            btStart.setText(getString(R.string.stop_title));
//        MyApp.updateTagIDs();

    }

    private void initData() {
        saveConfig();
    }

    private void saveConfig() {
        if (MyApp.antennaRfConfig != null) {
            int powerLevelIndex = -1;
            try {
                powerLevelIndex = 70;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }


            int tariValue = -1;
            try {
                tariValue = 0;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            linkedProfileIndex = 0;
            if (powerLevelIndex != MyApp.antennaRfConfig.getTransmitPowerIndex() || tariValue != MyApp.antennaRfConfig.getTari()) {
                new Task_SaveAntennaConfiguration(getActivity(), powerLevelIndex, linkedProfileIndex, tariValue).execute();
            }

        }
    }



    private void initView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("RFID Mapping");
        etScan.setVisibility(View.GONE);
        databaseHandler = Room.databaseBuilder(getActivity(),
                DatabaseHandler.class, "viacom-db")
                .build();
        getUserName = preferenceManager.getPreferenceValues(AppConstants.USERNAME);
//        etScan.requestFocus();
        etAssetNo.setEnabled(false);
        etAssetNo.setInputType(InputType.TYPE_NULL);
        etAssetNo.setFocusable(false);
        InputMethodManager imeManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imeManager.showInputMethodPicker();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_logout).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                new MainActivity().i = 0;
                getRfidData = null;
                triggerReleaseEventRecieved();
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to exit")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Objects.requireNonNull(getActivity()).finish();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });
                android.support.v7.app.AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.action_setting:
                SettingListFragment settingsFragment = new SettingListFragment();
                android.support.v4.app.FragmentTransaction fragmentTransaction5
                        = getFragmentManager().beginTransaction();
                fragmentTransaction5.replace(R.id.main_container, settingsFragment);
                fragmentTransaction5.addToBackStack(null);
                fragmentTransaction5.commit();
                return true;
            case R.id.action_logout:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                preferenceManager.putPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS, 0);
                new AsyncDeleteAllData().execute();
                new MainActivity().i = 0;
                getRfidData = null;
                preferenceManager.putPreferenceValues(AppConstants.GETIDANDDESCLIST, AppConstants.EMPTY);
                triggerReleaseEventRecieved();
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
    public void onDestroyView() {
        super.onDestroyView();
        new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.btSave, R.id.btClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btSave:
                break;
            case R.id.btClose:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                new MainActivity().i = 0;
                getRfidData = null;
                triggerReleaseEventRecieved();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(MappingFragment.class.getSimpleName());
                fragmentTransaction.commit();
                break;
        }
    }

    @Override
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {
        if (etScan != null)
            etScan.requestFocus();
        getRfidData = inventoryListItem.getMemoryBankData();
        if (getRfidData != null) {
            if(!scanstatus){
                Intent i = new Intent("android.intent.action.MAIN").putExtra(AppConstants.RFIDDATA, getRfidData);
                Objects.requireNonNull(getActivity()).sendBroadcast(i);
            }

        }

    }

    @Override
    public void handleStatusResponse(final RFIDResults results) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    //String command = statusData.command.trim();
                    //if (command.equalsIgnoreCase("in") || command.equalsIgnoreCase("inventory") || command.equalsIgnoreCase("read") || command.equalsIgnoreCase("rd"))
                    {
                        MyApp.isBatchModeInventoryRunning = false;
                        MyApp.mIsInventoryRunning = false;
                        if (btStart != null) {
                            btStart.setText(getResources().getString(R.string.start_title));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void triggerPressEventRecieved() {
        if (!MyApp.mIsInventoryRunning)
            getActivity().runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    scanstatus = false;
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });
    }

    @Override
    public void triggerReleaseEventRecieved() {
        if (MyApp.mIsInventoryRunning){
            getActivity().runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });

        }


    }

    @Override
    public void batchModeEventReceived() {
        if (btStart != null) {
            btStart.setText(getString(R.string.stop_title));
        }

        if (searchItemsList != null)
            searchItemsList.clear();
    }

    public String getMemoryBankID() {
        return "tid";
    }

    public void resetTagsInfo() {
        if (MyApp.inventoryList != null && MyApp.inventoryList.size() > 0)
            MyApp.inventoryList.clear();
    }

    public void resetInventoryDetail() {
        if (getActivity() != null) {
            if (btStart != null)
                btStart.setText(getString(R.string.start_title));
        }
    }

    @Override
    public void onUpdate() {
        if (isVisible() && tagIDField != null) {
            MyApp.accessControlTag = tagIDField;
        }
    }

    @Override
    public void onRefresh() {
        if (MyApp.accessControlTag != null && tagIDField != null) {
            tagIDField = MyApp.accessControlTag;
        }
    }

    @OnClick(R.id.btWrite)
    public void onViewClicked() {
//     new MainActivity().accessOperationsWriteClicked(tagIDField,offset,writeData,getActivity());

    }


    @SuppressLint("StaticFieldLeak")
    public class AsyncDeleteAllData extends AsyncTask<Object, Object, String> {

        @Override
        protected String doInBackground(Object... objects) {
            databaseHandler.daoAccess().deleteAllScanTagList();
            return null;
        }


    }
}
