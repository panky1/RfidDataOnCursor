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
import com.bcil.demoassettrack.utils.JsonController;
import com.bcil.demoassettrack.utils.NetworkUtils;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.bcil.demoassettrack.utils.Validation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zebra.rfid.api3.RFIDResults;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.bcil.demoassettrack.utils.MyKeyboard.inputConnection;

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
    private Handler handler = new Handler();

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
        status = preferenceManager.getPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS);
        if (status == 0) {
            inflateAssetIdAndDesc();
        } else {
//            new AsyncLoaAssetIdAndDesc().execute();
            Gson gson = new Gson();
            String json = preferenceManager.getPreferenceValues(AppConstants.GETIDANDDESCLIST);
            Type type = new TypeToken<List<AssetInfo>>(){}.getType();
            List<AssetInfo> assetInfoList = gson.fromJson(json, type);
            if (assetInfoList != null && assetInfoList.size() > 0) {
                preferenceManager.putPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS, 1);
                CustomAssetIdAdapter customAssetIdAdapter = new CustomAssetIdAdapter(getActivity(), R.layout.assetid_list_row, assetInfoList, "ASSETID");
                aTAstId.setThreshold(1);
                aTAstId.setAdapter(customAssetIdAdapter);
                CustomAssetIdAdapter customAssetIdAdapter1 = new CustomAssetIdAdapter(getActivity(), R.layout.assetid_list_row, assetInfoList, "ASSETDESC");
                aTAstDsc.setThreshold(1);
                aTAstDsc.setAdapter(customAssetIdAdapter1);
            }
        }
        aTAstId.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssetInfo assetInfo = (AssetInfo) parent.getAdapter().getItem(position);
                aTAstDsc.setText(assetInfo.getAssetdesc());
                etAssetNo.setText(aTAstId.getText().toString().trim());
                new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
                etScan.requestFocus();
            }
        });
        aTAstDsc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssetInfo assetInfo = (AssetInfo) parent.getAdapter().getItem(position);
                aTAstId.setText(assetInfo.getAssetid());
                etAssetNo.setText(assetInfo.getAssetid());
                new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
                etScan.requestFocus();
            }
        });
        etScan.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                etScan.setError(null);
                etScan.setFocusable(true);
            }
        });
        saveConfig();
    }

    private void saveConfig() {
        if (MyApp.antennaRfConfig != null) {
            int powerLevelIndex = -1;
            try {
                powerLevelIndex = 30;
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

    private void inflateAssetIdAndDesc() {
       /* if (new NetworkUtils().isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
            AsyncGetAssetIdAndDesc asyncGetAssetIdAndDesc = new AsyncGetAssetIdAndDesc("GETASSETIDANDESC", AppConstants.EMPTY, AppConstants.EMPTY, AppConstants.EMPTY, "0");
            asyncGetAssetIdAndDesc.execute("");
        } else {
            Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();

        }*/
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!Validation.hasActText(aTAstId)) ret = false;
        if (!Validation.hasText(etScan)) ret = false;
        return ret;
    }

    private void initView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("RFID Mapping");
        databaseHandler = Room.databaseBuilder(getActivity(),
                DatabaseHandler.class, "viacom-db")
                .build();
        getUserName = preferenceManager.getPreferenceValues(AppConstants.USERNAME);
        etScan.requestFocus();
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
        menu.findItem(R.id.action_setting).setVisible(false);
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
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(MappingFragment.class.getSimpleName());
                fragmentTransaction.commit();
                return true;
            case R.id.action_logout:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS, 0);
                preferenceManager.putPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS, 0);
                new AsyncDeleteAllData().execute();
                new MainActivity().i = 0;
                getRfidData = null;
                preferenceManager.putPreferenceValues(AppConstants.GETIDANDDESCLIST,AppConstants.EMPTY);
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
                if (new NetworkUtils().isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
                    if (checkValidation()) {
                       /* AsyncValidateScanRfid asyncValidateScanRfid = new AsyncValidateScanRfid("VALIDATERFID", getUserName, etAssetNo.getText().toString().trim(), etScan.getText().toString().trim(), "0");
                        asyncValidateScanRfid.execute("");*/

                    } else {
                        Toast.makeText(getActivity(), "Please enter the required fields", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                }
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
            /*tagIDField = getRfidData;
            offset = "2";
            writeData = "AD1234567890123456700020";*/
            if (etScan != null) {
//                etScan.setText(getRfidData);
                preferenceManager.putPreferenceValues(AppConstants.RFIDDATA,getRfidData);
//                new AssetInfo().setRfid(getRfidData);
                Intent i = new Intent("android.intent.action.MAIN").putExtra(AppConstants.RFIDDATA, getRfidData);
                Objects.requireNonNull(getActivity()).sendBroadcast(i);
                if(getRfidData!=null){
                    Log.d(MappingFragment.class.getSimpleName(), "GETRFIDDATA:"+getRfidData);
                }else{
                    Log.d(MappingFragment.class.getSimpleName(), "GETRFIDDATA:"+"null");
                }
               /* Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        inputConnection.commitText(new AssetInfo().getRfid(), 1);
                        handler.postDelayed(this, 1000);
                        new AssetInfo().setRfid("");
                    }
                };

                handler.postDelayed(runnable, 1000);*/
//                etScan.setSelection(etScan.getText().toString().length());
            }
        } else {
            if (etScan != null)
                new AssetInfo().setRfid("123");
//                etScan.setText(AppConstants.EMPTY);
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
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });
    }

    @Override
    public void triggerReleaseEventRecieved() {
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

   /* public void handleTagResponse1(TagData tagData) {
        Log.d(MappingFragment.class.getSimpleName(), "handleTagResponse1:"+tagData.getTagID());

    }*/

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetAssetIdAndDesc extends AsyncTask<Object, Object, String> {
        String mode, username, assetid, rfid, status;
        private ProgressDialog progressDialog;

        AsyncGetAssetIdAndDesc(String mode, String username, String assetid, String rfid, String status) {
            this.mode = mode;
            this.username = username;
            this.assetid = assetid;
            this.rfid = rfid;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CommonUtils().startProgressBarDialog(getActivity(), "Please wait fetching assetid and description...");
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                return DataSelections.fetchAssetIdAndDesc(mode, username, assetid, rfid, status, getActivity());
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
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            progressDialog.dismiss();
            if (result != null) {
                try {
                    assetInfoList = JsonController.jsonToOneDimenStrAstIdAndDescDtls(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (assetInfoList != null && assetInfoList.size() > 0) {
                    preferenceManager.putPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS, 1);
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<AssetInfo>>() {}.getType();
                    String list = gson.toJson(assetInfoList, type);
                    preferenceManager.putPreferenceValues(AppConstants.GETIDANDDESCLIST,list);
                    CustomAssetIdAdapter customAssetIdAdapter = new CustomAssetIdAdapter(getActivity(), R.layout.assetid_list_row, assetInfoList, "ASSETID");
                    aTAstId.setThreshold(1);
                    aTAstId.setAdapter(customAssetIdAdapter);
                    CustomAssetIdAdapter customAssetIdAdapter1 = new CustomAssetIdAdapter(getActivity(), R.layout.assetid_list_row, assetInfoList, "ASSETDESC");
                    aTAstDsc.setThreshold(1);
                    aTAstDsc.setAdapter(customAssetIdAdapter1);
                } else {
                    Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
                }


            } else {
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncValidateScanRfid extends AsyncTask<Object, Object, String> {
        private String mode, getUserName, assetId, scanRfid, status;
        private ProgressDialog progressDialog;

        AsyncValidateScanRfid(String mode, String getUserName, String assetId, String scanRfid, String status) {
            this.mode = mode;
            this.getUserName = getUserName;
            this.assetId = assetId;
            this.scanRfid = scanRfid;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CommonUtils().startProgressBarDialog(getActivity(), "Please wait validating scanned rfid...");
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                return DataSelections.fetchAssetIdAndDesc(mode, getUserName, assetId, scanRfid, status, getActivity());
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
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result != null) {
                if (result.contains("MAPPING")) {
                    aTAstId.setText(AppConstants.EMPTY);
                    aTAstDsc.setText(AppConstants.EMPTY);
                    etAssetNo.setText(AppConstants.EMPTY);
                    etScan.setText(AppConstants.EMPTY);
                    aTAstId.requestFocus();
                    Toast.makeText(getActivity(), "MAPPING DONE SUCCESSFULLY.", Toast.LENGTH_SHORT).show();
                } else if (result.contains("RFID ALREADY IN USE")) {
                    new CommonUtils().Alert(getActivity(), "Alert", result);
                } else if (result.contains("ARE YOU")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                    builder.setMessage("Asset is already mapped,Do you want to remapping?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (new NetworkUtils().isNetworkAvailable(getActivity())) {
                                        if (checkValidation()) {
                                            AsyncValidateScanRfid asyncValidateScanRfid = new AsyncValidateScanRfid("VALIDATERFID", getUserName, aTAstId.getText().toString().trim(), etScan.getText().toString().trim(), "1");
                                            asyncValidateScanRfid.execute("");
                                        } else {
                                            Toast.makeText(getActivity(), "Please enter the required fields", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })

                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else {
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }
        }
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
