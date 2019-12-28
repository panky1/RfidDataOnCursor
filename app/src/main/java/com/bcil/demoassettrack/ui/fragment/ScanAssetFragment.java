package com.bcil.demoassettrack.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.RfidListAdapter;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.common.ResponseHandlerInterfaces;
import com.bcil.demoassettrack.common.Task_SaveAntennaConfiguration;
import com.bcil.demoassettrack.database.DatabaseHandler;
import com.bcil.demoassettrack.inventory.InventoryListItem;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.model.AssetInfoNew;
import com.bcil.demoassettrack.network.DataSelections;
import com.bcil.demoassettrack.ui.activity.LoginActivity;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CommonUtils;
import com.bcil.demoassettrack.utils.NetworkUtils;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.zebra.rfid.api3.RFIDResults;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanAssetFragment extends Fragment implements ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler {
    @Bind(R.id.etScan)
    EditText etScan;
    @Bind(R.id.rvList)
    RecyclerView rvList;
    @Bind(R.id.btStart)
    Button btStart;
    private PreferenceManager preferenceManager;
    private List<AssetInfo> assetInfoList = new ArrayList<>();
    private List<String> stringList = new ArrayList<>();
    private String memoryBankID = "tid";
    private String getUserName;
    private DatabaseHandler databaseHandler;
    private AssetInfoNew assetInfo;
    private RfidListAdapter rfidListAdapter;
    private int linkedProfileIndex;
    private int getStatus;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_asset, container, false);
        ButterKnife.bind(this, view);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        databaseHandler = Room.databaseBuilder(getActivity(),
                DatabaseHandler.class, "viacom-db")
                .build();
        setHasOptionsMenu(true);
        initView();
        initData();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
        if (MyApp.mIsInventoryRunning)
            btStart.setText(getString(R.string.stop_title));
        rfidListAdapter = new RfidListAdapter(getActivity(), new ArrayList<AssetInfo>());
        rvList.setAdapter(rfidListAdapter);
    }

    private void initData() {
        saveConfig();
        /*rvList.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                rvList, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well

            }

            @Override
            public void onLongClick(View view, final int position) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                alertDialogBuilder.setTitle("Alert");
                alertDialogBuilder
                        .setMessage("Are you sure to delete data")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new AsyncScanRfid(etScan.getText().toString().trim(), "DeleteSingleData", position).execute();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close
                                // current activity
                                //MainActivity.this.finish();
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }));*/
    }

    private void saveConfig() {
        if (MyApp.antennaRfConfig != null) {
            int powerLevelIndex = -1;
            try {
                powerLevelIndex = 270;
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Scan Asset");
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        getUserName = preferenceManager.getPreferenceValues(AppConstants.USERNAME);
        etScan.requestFocus();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_setting).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                assetInfoList.clear();
                stringList.clear();
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                new MainActivity().i = 0;
                stopRfidScan();
                if (rvList.getAdapter() != null) {
                    ((RfidListAdapter) rvList.getAdapter()).clear();
                    ((RfidListAdapter) rvList.getAdapter()).notifyDataSetChanged();
                }
                new AsyncScanRfid(etScan.getText().toString().trim(), "Exit", 0).execute();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(ScanAssetFragment.class.getSimpleName());
                fragmentTransaction.commit();
                return true;
            case R.id.action_logout:
                assetInfoList.clear();
                stringList.clear();
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                new MainActivity().i = 0;
                stopRfidScan();
                if (rvList.getAdapter() != null) {
                    ((RfidListAdapter) rvList.getAdapter()).clear();
                    ((RfidListAdapter) rvList.getAdapter()).notifyDataSetChanged();
                }
                new AsyncScanRfid(etScan.getText().toString().trim(), "Exit", 0).execute();
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
    }

    public String getMemoryBankID() {
        return memoryBankID;
    }

    @OnClick({R.id.btSave, R.id.btClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btSave:
                List<AssetInfo> assetInfoList1 = new ArrayList<>();
                List<AssetInfo> astInfoList = null;
                if (rfidListAdapter != null) {
                    astInfoList = ((RfidListAdapter) rfidListAdapter)
                            .getAssetInfoList();
                }
                if (astInfoList != null && astInfoList.size() > 0) {
                    for (int i = 0; i < astInfoList.size(); i++) {
                        AssetInfo assetInfo = astInfoList.get(i);
                        assetInfoList1.add(new AssetInfo(assetInfo.getRfid()));
                    }
                    Set<String> set = new HashSet<>(stringList);
                    stringList.clear();
                    stringList.addAll(set);

                    if (new NetworkUtils().isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
                        AsyncOnSavePhysicalAudit asyncOnSavePhysicalAudit = new AsyncOnSavePhysicalAudit(getUserName, /*assetInfoList1*/stringList);
                        asyncOnSavePhysicalAudit.execute("");
                    } else {
                        Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    new CommonUtils().Alert(getActivity(), "Alert", "Rfid data not found please scan the rfid");
                }

                break;
            case R.id.btClose:
                assetInfoList.clear();
                stringList.clear();
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                new MainActivity().i = 0;
                stopRfidScan();
                if (rvList.getAdapter() != null) {
                    ((RfidListAdapter) rvList.getAdapter()).clear();
                    ((RfidListAdapter) rvList.getAdapter()).notifyDataSetChanged();
                }
                new AsyncScanRfid(etScan.getText().toString().trim(), "Exit", 0).execute();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(MappingFragment.class.getSimpleName());
                fragmentTransaction.commit();
                break;
        }
    }

    private String sendJsonArrayData(List<AssetInfo> assetInfoList) {
        JSONArray jsonArray = new JSONArray();
        if (assetInfoList != null && assetInfoList.size() > 0) {
            for (int i = 0; i < assetInfoList.size(); i++) {
                jsonArray.put(assetInfoList.get(i).getJsonObject());
            }
        }

        return jsonArray.toString();
    }

    private String sendJsonArrayData1(List<String> assetInfoList) {
        JSONArray jsonArray = new JSONArray();
        if (assetInfoList != null && assetInfoList.size() > 0) {
            for (int i = 0; i < assetInfoList.size(); i++) {
                try {
                    jsonArray.put(new JSONObject().put("RFID", assetInfoList.get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsonArray.toString();
    }

    public void resetInventoryDetail() {
        if (getActivity() != null) {
            if (btStart != null)
                btStart.setText(getString(R.string.start_title));
        }
    }

    public void resetTagsInfo() {
        if (MyApp.inventoryList != null && MyApp.inventoryList.size() > 0)
            MyApp.inventoryList.clear();
        if (rvList.getAdapter() != null) {
            ((RfidListAdapter) rvList.getAdapter()).clear();
            ((RfidListAdapter) rvList.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {
        etScan.requestFocus();
        if (rvList.getAdapter() == null) {
            rvList.setAdapter(rfidListAdapter);
        }
        if (inventoryListItem.getMemoryBankData() != null) {
            etScan.setText(inventoryListItem.getMemoryBankData());
            etScan.setSelection(etScan.getText().toString().length());
            new AsyncScanRfid(inventoryListItem.getMemoryBankData(), "Scan", 0).execute();
        } else {
            etScan.setText(AppConstants.EMPTY);
        }
    }

    @Override
    public void handleStatusResponse(final RFIDResults results) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    {
                        MyApp.isBatchModeInventoryRunning = false;
                        MyApp.mIsInventoryRunning = false;
                        Button inventoryButton = (Button) getActivity().findViewById(R.id.btStart);
                        if (inventoryButton != null) {
                            inventoryButton.setText(getResources().getString(R.string.start_title));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void triggerPressEventRecieved() {
        /*if (!MyApp.mIsInventoryRunning)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });*/
    }

    @Override
    public void triggerReleaseEventRecieved() {
        /*if (MyApp.mIsInventoryRunning)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });*/
    }

    public void stopRfidScan() {
        if (MyApp.mIsInventoryRunning)
            getActivity().runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });
    }

    @Override
    public void batchModeEventReceived() {
        if (btStart != null) {
            btStart.setText(getString(R.string.stop_title));
        }

        if (rvList != null) {
            rfidListAdapter.clear();
            rfidListAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncOnSavePhysicalAudit extends AsyncTask<Object, Object, String> {
        private String getUserName;
        private List<AssetInfo> assetInfoList;
        private List<String> stringList;
        private ProgressDialog progressDialog;

        AsyncOnSavePhysicalAudit(String getUserName, /*List<AssetInfo> assetInfoList*/List<String> stringList) {
            this.getUserName = getUserName;
            this.stringList = stringList;
//            this.assetInfoList = assetInfoList;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CommonUtils().startProgressBarDialog(getActivity(), "Please wait validating data...");
        }

        @Override
        protected String doInBackground(Object... objects) {
            if (!sendJsonArrayData1(/*assetInfoList*/stringList).equals("[]")) {
                try {
                    return DataSelections.savePhysicalAudit(getUserName, sendJsonArrayData1(/*assetInfoList*/stringList), getActivity());
                } catch (Exception ex) {
                    try {
                        throw ex;
                    } catch (final IOException | XmlPullParserException e1) {
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (e1.toString().startsWith("java.net.ConnectException: failed to connect")) {
                                    new CommonUtils().Alert(getActivity(), "Alert", "Failed to connect,Please try again.");
                                } else if (e1.toString().startsWith("java.net.SocketTimeoutException: failed to connect")) {
                                    new CommonUtils().Alert(getActivity(), "Alert", "Failed to connect,Please try again.");
                                }

                            }
                        });
                    }
                }
            } else {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new CommonUtils().Alert(getActivity(), "Alert", "Rfid data not found please scan the rfid");
                    }
                });
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            etScan.setText(AppConstants.EMPTY);
            if (result != null) {
                Toast.makeText(getActivity(), "Data Saved Successfully.", Toast.LENGTH_SHORT).show();

                if (result.contains("PHYSICAL AUDIT DONE")) {
//                    assetInfoList.clear();
                    stringList.clear();
                    ((RfidListAdapter) Objects.requireNonNull(rvList.getAdapter())).clear();
                    ((RfidListAdapter) rvList.getAdapter()).notifyDataSetChanged();
                } else {
//                    assetInfoList.clear();
                    stringList.clear();
                    ((RfidListAdapter) Objects.requireNonNull(rvList.getAdapter())).clear();
                    ((RfidListAdapter) rvList.getAdapter()).notifyDataSetChanged();
                }
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                new MainActivity().i = 0;
                new AsyncScanRfid(etScan.getText().toString().trim(), "Exit", 0).execute();
            } else {
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncScanRfid extends AsyncTask<Void, Void, Void> {
        String rfid, mode;
        int position;

        AsyncScanRfid(String rfid, String mode, int position) {
            this.rfid = rfid;
            this.mode = mode;
            this.position = position;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            switch (mode) {
                case "Scan":
                    assetInfo = databaseHandler.daoAccess().getRfidDataExist(rfid);
                /*Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable(){
                    @Override
                    public void run() {*/
                    if (assetInfo != null) {
                        if (!assetInfo.getRfid().equals(rfid)) {
                            databaseHandler.daoAccess().insertOnlySingleRecord(new AssetInfoNew(rfid));
                            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rfidListAdapter.add(rfid);
                                    stringList.add(rfid);
                                    rfidListAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            if (preferenceManager.getPreferenceIntValues(AppConstants.RFIDSCANSTATUS) == 0) {
                                if (preferenceManager.getPreferenceIntValues(AppConstants.BUTTONCLICK) >= 2) {
                                    List<AssetInfoNew> assetInfoNewList = databaseHandler.daoAccess().fetchAllData();
                                    for (final AssetInfoNew assetInfoNew : assetInfoNewList) {
                                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                rfidListAdapter.add(assetInfoNew.getRfid());
                                                stringList.add(assetInfoNew.getRfid());
                                                rfidListAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                    preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 1);
                                }
                            }

                        }
                    } else {
                        databaseHandler.daoAccess().insertOnlySingleRecord(new AssetInfoNew(rfid));
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                rfidListAdapter.add(rfid);
                                stringList.add(rfid);
                                rfidListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    break;
                case "DeleteSingleData":
                    List<AssetInfoNew> assetInfoNewList = databaseHandler.daoAccess().fetchAllData();
                    databaseHandler.daoAccess().deleteRfidData(assetInfoNewList.get(position).getRfid());
                    final ArrayList<AssetInfo> assetInfoList = ((RfidListAdapter) rfidListAdapter)
                            .getAssetInfoList();
                    assetInfoList.remove(position);
                    stringList.remove(position);
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (assetInfoList.size() > 0) {
                                rfidListAdapter = new RfidListAdapter(getActivity(), assetInfoList);
                                rvList.setAdapter(rfidListAdapter);
                                rfidListAdapter.notifyDataSetChanged();
                            } else {
                                rfidListAdapter = new RfidListAdapter(getActivity(), new ArrayList<AssetInfo>());
                                rvList.setAdapter(rfidListAdapter);
                                rfidListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    break;
                default:
                    databaseHandler.daoAccess().deleteAllScanTagList();
                    break;
            }


            return null;
        }
    }
}
