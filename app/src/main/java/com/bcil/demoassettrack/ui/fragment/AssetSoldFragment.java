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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import com.bcil.demoassettrack.ClickListener;
import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.ScrapListAdapter;
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
import com.bcil.demoassettrack.utils.JsonController;
import com.bcil.demoassettrack.utils.NetworkUtils;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.bcil.demoassettrack.utils.RecyclerTouchListener;
import com.bcil.demoassettrack.utils.Validation;
import com.zebra.rfid.api3.RFIDResults;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
@RequiresApi(api = Build.VERSION_CODES.M)
public class AssetSoldFragment extends Fragment implements  ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler,View.OnScrollChangeListener{
    @Bind(R.id.etScan)
    EditText etScan;
    @Bind(R.id.rvList)
    RecyclerView rvList;
    @Bind(R.id.etReason)
    EditText etReason;
    @Bind(R.id.btStart)
    Button btStart;
    @Bind(R.id.hscrl)
    HorizontalScrollView hscrl;
    private PreferenceManager preferenceManager;
    private String getUserName;
    private List<AssetInfo> assetInfoList;
    private ScrapListAdapter scrapListAdapter;
    private DatabaseHandler databaseHandler;
    private AssetInfoNew assetInfo;
    private String parseMsg;
    private List<AssetInfo> assetInfoList1;
    private int linkedProfileIndex;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
        if (MyApp.mIsInventoryRunning)
            btStart.setText(getString(R.string.stop_title));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_scrap, container, false);
        ButterKnife.bind(this, view);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        databaseHandler =  Room.databaseBuilder(getActivity(),
                DatabaseHandler.class, "viacom-db")
                .build();
        assetInfoList = new ArrayList<>();
        setHasOptionsMenu(true);
        initView();
        initData();
        return view;
    }

    private void initData() {
        saveConfig();
        rvList.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
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
                                new AsyncScanRfid(etScan.getText().toString().trim(),"DeleteSingleData",position).execute();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }));
    }

    private void saveConfig() {
        if (MyApp.antennaRfConfig != null ) {
            int powerLevelIndex = -1;
            try {
                powerLevelIndex = 30;
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }


            int tariValue = -1;
            try {
                tariValue = 0;
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
            linkedProfileIndex = 0;
            if (powerLevelIndex != MyApp.antennaRfConfig.getTransmitPowerIndex()  || tariValue != MyApp.antennaRfConfig.getTari()) {
                new Task_SaveAntennaConfiguration(getActivity(),powerLevelIndex, linkedProfileIndex, tariValue).execute();
            }

        }
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!Validation.hasText(etReason)) ret = false;
        return ret;
    }

    private void initView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Asset Sold");
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        getUserName = preferenceManager.getPreferenceValues(AppConstants.USERNAME);
        etScan.requestFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hscrl.setOnScrollChangeListener(this);
        }
       /* hscrl.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        etScan.requestFocus();
                    }
                });*/
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
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS,0);
                new MainActivity().i = 0;
                triggerReleaseEventRecieved();
                new AsyncScanRfid(etScan.getText().toString().trim(),"Exit", 0).execute();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(AssetSoldFragment.class.getSimpleName());
                fragmentTransaction.commit();
                return true;
            case R.id.action_logout:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS,0);
                preferenceManager.putPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS, 0);
                new MainActivity().i = 0;
                triggerReleaseEventRecieved();
                new AsyncScanRfid(etScan.getText().toString().trim(),"Exit", 0).execute();
                preferenceManager.clearSharedPreferance();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public String getMemoryBankID() {
        return "tid";
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
    }


    @OnClick({R.id.btSave, R.id.btClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btSave:
                List<AssetInfo> assetInfoList1 = new ArrayList<>();
                List<AssetInfo> assetInfoList = null;
                if (scrapListAdapter != null) {
                    assetInfoList = ((ScrapListAdapter) scrapListAdapter)
                            .getAssetInfoList();
                }
                if(assetInfoList!=null&&assetInfoList.size()>0){
                    for (int i = 0; i < assetInfoList.size(); i++) {
                        AssetInfo assetInfo = assetInfoList.get(i);
                        if (assetInfo.isSelected()) {
                            assetInfoList1.add(new AssetInfo(assetInfo.getAssetid(),assetInfo.getRfid(),assetInfo.getLocation()));
                        }

                    }

                    if (new NetworkUtils().isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
                        if (checkValidation()) {
                            AsyncOnSaveScrapData asyncOnSaveScrapData = new AsyncOnSaveScrapData("OnSave", getUserName,AppConstants.EMPTY,assetInfoList1,"SOLDING",etReason.getText().toString().trim());
                            asyncOnSaveScrapData.execute("");
                        } else {
                            Toast.makeText(getActivity(), "Please enter the required fields", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    new CommonUtils().Alert(getActivity(),"Alert","Rfid data not found please scan the rfid");
                }
                break;
            case R.id.btClose:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS,0);
                new MainActivity().i = 0;
                triggerReleaseEventRecieved();
                new AsyncScanRfid(etScan.getText().toString().trim(),"Exit", 0).execute();
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
        etScan.requestFocus();
        if(inventoryListItem.getMemoryBankData()!=null){
            etScan.setText(inventoryListItem.getMemoryBankData());
            etScan.setSelection(etScan.getText().toString().length());
            if (new NetworkUtils().isNetworkAvailable(getActivity())) {
                new AsyncScanRfid(etScan.getText().toString().trim(), "Scan", 0).execute();
            }else{
                etScan.setText(AppConstants.EMPTY);
                new CommonUtils().Alert(getActivity(),"Alert","Please check internet connection and try again.");
            }

        }else {
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
        if (!MyApp.mIsInventoryRunning)
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) getActivity()).inventoryStartOrStop(btStart);
                }
            });
    }

    @Override
    public void triggerReleaseEventRecieved() {
        if (MyApp.mIsInventoryRunning)
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
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
    }

    @Override
    public void onScrollChange(View view, int i, int i1, int i2, int i3) {
        etScan.requestFocus();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncValidateRfid extends AsyncTask<Object, Object, String> {
        String mode, getUserName, scanRfid, strData, module;
        private ProgressDialog progressDialog;

        AsyncValidateRfid(String mode, String getUserName, String scanRfid, String strData, String module) {
            this.mode = mode;
            this.getUserName = getUserName;
            this.scanRfid = scanRfid;
            this.strData = strData;
            this.module = module;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CommonUtils().startProgressBarDialog(getActivity(), "Please wait validating scanned rfid...");
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                return DataSelections.validateRfid(mode, getUserName, scanRfid, strData, module,AppConstants.EMPTY, getActivity());
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
            String scanData = etScan.getText().toString().trim();
//            etScan.setText(AppConstants.EMPTY);
            if (result != null) {
                try {
                    parseMsg = JsonController.FetchMsg(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (result.contains("RFID MAPPING NOT DONE")) {
                   new CommonUtils().Alert(getActivity(), "Alert", parseMsg);
                } else if(result.contains("INVALID ASSET SCAN")){
                   new CommonUtils().Alert(getActivity(), "Alert", parseMsg);
                }else  {
                    try {
                        assetInfoList1 = JsonController.jsonToOneDimenStrScrapAndSold(result,assetInfoList);
                        if (assetInfoList1 != null && assetInfoList1.size() > 0) {
                            scrapListAdapter = new ScrapListAdapter(getActivity(), assetInfoList1);
                            rvList.setAdapter(scrapListAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                new AsyncScanRfid(scanData,"Delete", 0).execute();
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncOnSaveScrapData extends AsyncTask<Object, Object, String>{
        String mode, getUserName, scanRfid, module ,reason;
        List<AssetInfo> assetInfoList;
        private ProgressDialog progressDialog;

        AsyncOnSaveScrapData(String mode, String getUserName, String scanRfid, List<AssetInfo> assetInfoList, String module,String reason) {
            this.mode = mode;
            this.getUserName = getUserName;
            this.scanRfid = scanRfid;
            this.assetInfoList = assetInfoList;
            this.module = module;
            this.reason = reason;
        }

        @Override
        protected void onPreExecute() {
            progressDialog =  new CommonUtils().startProgressBarDialog(getActivity(), "Please wait validating scrapping data...");
        }

        @Override
        protected String doInBackground(Object... objects) {
            if(!sendJsonArrayData(assetInfoList).equals("[]")){
                try{
                    return DataSelections.validateRfid(mode, getUserName, scanRfid, sendJsonArrayData(assetInfoList),module,reason, getActivity());
                }catch (Exception ex){
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
            }else {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       new CommonUtils().Alert(getActivity(),"Alert","Please select list item to sold");
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
           progressDialog.dismiss();
            if (result != null) {
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                etScan.setText(AppConstants.EMPTY);
                etReason.setText(AppConstants.EMPTY);
                etScan.requestFocus();
                assetInfoList1.clear();
                ScrapListAdapter scrapListAdapter = new ScrapListAdapter(getActivity(),new ArrayList<AssetInfo>());
                rvList.setAdapter(scrapListAdapter);
            }
            else {
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String sendJsonArrayData(List<AssetInfo> assetInfoList) {
        JSONArray jsonArray = new JSONArray();
        if(assetInfoList!=null&&assetInfoList.size()>0){
            for (int i = 0; i < assetInfoList.size(); i++) {
                jsonArray.put(assetInfoList.get(i).getJsonObject1());
            }
        }

        return jsonArray.toString();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncScanRfid extends AsyncTask<Void, Void, Void>{
        String rfid,mode;
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
                 /*   Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {*/
                            if (assetInfo != null) {
                                if (!assetInfo.getRfid().equals(rfid)) {
                                    databaseHandler.daoAccess().insertOnlySingleRecord(new AssetInfoNew(rfid));
                                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AsyncValidateRfid asyncValidateRfid = new AsyncValidateRfid("ValidateRfid", getUserName, etScan.getText().toString().trim(), new AssetInfo().getJsonObject3().toString(), "SOLDING");
                                            asyncValidateRfid.execute("");
                                        }
                                    });

                                } else {
                                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "Rfid already scan,Please scan other rfid", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                databaseHandler.daoAccess().insertOnlySingleRecord(new AssetInfoNew(rfid));
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AsyncValidateRfid asyncValidateRfid = new AsyncValidateRfid("ValidateRfid", getUserName, etScan.getText().toString().trim(), new AssetInfo().getJsonObject3().toString(), "SOLDING");
                                        asyncValidateRfid.execute("");
                                    }
                                });

                            }
                    break;
                case "Delete":
                    databaseHandler.daoAccess().deleteRfidData(rfid);
                    break;
                case "DeleteSingleData":
                    List<AssetInfoNew> assetInfoNewList = databaseHandler.daoAccess().fetchAllData();
                    databaseHandler.daoAccess().deleteRfidData(assetInfoNewList.get(position).getRfid());
                    assetInfoList1.remove(position);
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (assetInfoList1 != null && assetInfoList1.size() > 0) {
                                ScrapListAdapter scrapListAdapter = new ScrapListAdapter(getActivity(), assetInfoList1);
                                rvList.setAdapter(scrapListAdapter);
                                scrapListAdapter.notifyDataSetChanged();
                            } else {
                                ScrapListAdapter scrapListAdapter = new ScrapListAdapter(getActivity(),new ArrayList<AssetInfo>());
                                rvList.setAdapter(scrapListAdapter);
                                scrapListAdapter.notifyDataSetChanged();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
    }
}

