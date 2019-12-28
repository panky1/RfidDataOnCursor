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
import android.text.InputType;
import android.view.KeyEvent;
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

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.CustomAssetIdAdapter;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.common.ResponseHandlerInterfaces;
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

public class AssetAllocateFragment extends Fragment  implements  ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler{

    @Bind(R.id.etAssetNo)
    EditText etAssetNo;
    @Bind(R.id.aTAstId)
    AutoCompleteTextView aTAstId;
    @Bind(R.id.aTAstDsc)
    AutoCompleteTextView aTAstDsc;
    @Bind(R.id.etLocation)
    EditText etLocation;
    @Bind(R.id.btStart)
    Button btStart;
    private PreferenceManager preferenceManager;
    private String getUserName;
    private List<AssetInfo> assetInfoList;
    private DatabaseHandler databaseHandler;
    private ArrayList<AssetInfo> assetInfoList1;
    private List<AssetInfoNew> assetInfoNewList;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asst_alloction, container, false);
        ButterKnife.bind(this, view);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
        setHasOptionsMenu(true);
        initView();
        initData();
        return view;
    }

    private void initData() {
//        new AsyncLoaAssetIdAndDesc().execute();
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
        }else {
            inflateAssetIdAndDesc();
        }

        aTAstId.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssetInfo assetInfo = (AssetInfo)parent.getAdapter().getItem(position);
                aTAstDsc.setText(assetInfo.getAssetdesc());
                etAssetNo.setText(aTAstId.getText().toString().trim());
                new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
                etLocation.requestFocus();
            }
        });
        aTAstDsc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssetInfo assetInfo = (AssetInfo)parent.getAdapter().getItem(position);
                aTAstId.setText(assetInfo.getAssetid());
                etAssetNo.setText(assetInfo.getAssetid());
                new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
                etLocation.requestFocus();
            }
        });

        etLocation.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            return true;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            etLocation.requestFocus();
                            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                                    .hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                            if (new NetworkUtils().isNetworkAvailable(getActivity())) {
                               /* if (checkValidation("ScanLocation")) {
                                    AsyncAssignLocation asyncAssignLocation = new AsyncAssignLocation("OnLocationScan",getUserName,aTAstId.getText().toString().trim(),etLocation.getText().toString().trim());
                                    asyncAssignLocation.execute("");
                                } else {
                                    Toast.makeText(getActivity(), "Please enter the required fields", Toast.LENGTH_SHORT).show();
                                    etLocation.setText(AppConstants.EMPTY);
                                }*/
                                if (checkValidation("AssignLoc")) {
                                    AsyncAssignLocation asyncAssignLocation = new AsyncAssignLocation("OnAssign",getUserName,etAssetNo.getText().toString().trim(),etLocation.getText().toString().trim());
                                    asyncAssignLocation.execute("");
                                } else {
                                    Toast.makeText(getActivity(), "Please enter the required fields", Toast.LENGTH_SHORT).show();
                                    etLocation.setText(AppConstants.EMPTY);
                                }

                            } else {
                                new CommonUtils().Alert(getActivity(), "Alert", "Please check internet connection and try again.");
                                etLocation.setText(AppConstants.EMPTY);
                            }


                            return true;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            return true;
                    }
                }
                return false;


            }
        });
    }

    private void inflateAssetIdAndDesc() {
        if (new NetworkUtils().isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
            AsyncGetAssetIdAndDesc asyncGetAssetIdAndDesc = new AsyncGetAssetIdAndDesc("GETASSETIDANDESC",AppConstants.EMPTY,AppConstants.EMPTY,AppConstants.EMPTY,"0");
            asyncGetAssetIdAndDesc.execute("");
        } else {
            Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();

        }
    }


    private void initView() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Allocation");
        databaseHandler =  Room.databaseBuilder(getActivity(),
                DatabaseHandler.class, "viacom-db")
                .build();
        getUserName = preferenceManager.getPreferenceValues(AppConstants.USERNAME);
        aTAstId.requestFocus();
        etAssetNo.setEnabled(false);
        etAssetNo.setInputType(InputType.TYPE_NULL);
        etAssetNo.setFocusable(false);
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
//                assetInfoList1.clear();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                return true;
            case R.id.action_logout:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS,0);
                preferenceManager.putPreferenceIntValues(AppConstants.GETASSETIDANDDESCSTATUS, 0);
                new AsyncDeleteAllData().execute();
                new MainActivity().i = 0;
//                assetInfoList1.clear();
                preferenceManager.clearSharedPreferance();
                preferenceManager.putPreferenceValues(AppConstants.GETIDANDDESCLIST,AppConstants.EMPTY);
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btAsgn, R.id.btClose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btAsgn:
                if (new NetworkUtils().isNetworkAvailable(Objects.requireNonNull(getActivity()))) {
                    if (checkValidation("AssignLoc")) {
                        AsyncAssignLocation asyncAssignLocation = new AsyncAssignLocation("OnAssign",getUserName,etAssetNo.getText().toString().trim(),etLocation.getText().toString().trim());
                        asyncAssignLocation.execute("");
                    } else {
                        Toast.makeText(getActivity(), "Please enter the required fields", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btClose:
                preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS,0);
                new MainActivity().i = 0;
//                assetInfoList1.clear();
                MainMenuFragment mainMenuFragment = new MainMenuFragment();
                FragmentTransaction fragmentTransaction
                        = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(MappingFragment.class.getSimpleName());
                fragmentTransaction.commit();
                break;
        }
    }

    private boolean checkValidation(String mode) {
        boolean ret = true;
        if(mode.equals("ScanLocation")){
            if (!Validation.hasActText(aTAstId)) ret = false;
        }else {
            if (!Validation.hasActText(aTAstId)) ret = false;
            if(!Validation.hasText(etLocation)) ret = false;
        }

        return ret;
    }

    @Override
    public void triggerPressEventRecieved() {
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
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {

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
    public void batchModeEventReceived() {

    }


        public void resetTagsInfo() {
            if (MyApp.inventoryList != null && MyApp.inventoryList.size() > 0)
                MyApp.inventoryList.clear();
        }

    public String getMemoryBankID() {
        return "tid";
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncAssignLocation extends AsyncTask<Object, Object, String> {
        private String getUserName,assetId,location,mode;
        private ProgressDialog progressDialog;

        AsyncAssignLocation(String mode,String getUserName, String assetId, String location) {
             this.mode = mode;
            this.getUserName = getUserName;
            this.assetId = assetId;
            this.location = location;
        }

        @Override
        protected void onPreExecute() {
             progressDialog =  new CommonUtils().startProgressBarDialog(getActivity(), "Please wait assigning location...");
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                return DataSelections.assignLocation(mode,getUserName,assetId,location,getActivity());
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
            if(result!=null){
                if(result.equals("LOCATION ASSIGN SUCCESSFULLY")){
                    Toast.makeText(getActivity(), "LOCATION ASSIGN SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                    aTAstId.setText(AppConstants.EMPTY);
                    aTAstDsc.setText(AppConstants.EMPTY);
                    etAssetNo.setText(AppConstants.EMPTY);
                    etLocation.setText(AppConstants.EMPTY);
                    aTAstId.requestFocus();
                }else  {
                       if(result.equals("INVALID LOCATION SCAN")||result.equals("LOCATION ALREADY ASSIGNED")||result.equals("INVALID ASSET SCAN")||result.equals("RFID MAPPING NOT DONE")){
                           new CommonUtils().Alert(getActivity(),"Alert",result);
                           etLocation.requestFocus();
                           etLocation.setText(AppConstants.EMPTY);
                       }else if(result.equals("VALID LOCATION SCAN")){
//                           Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                           etLocation.requestFocus();
                       }
                }
            }else {
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGetAssetIdAndDesc extends AsyncTask<Object, Object, String> {
        String mode,username,assetid,rfid,status;
        private ProgressDialog progressDialog;

        AsyncGetAssetIdAndDesc(String mode, String username,String assetid,String rfid,String status) {
            this.mode = mode;
            this.username = username;
            this.assetid = assetid;
            this.rfid = rfid;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CommonUtils().startProgressBarDialog(getActivity(), "Please wait fetching assetno...");
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                return DataSelections.fetchAssetIdAndDesc(mode,username,assetid,rfid,status,getActivity());
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
                if(assetInfoList!=null&&assetInfoList.size()>0){
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
                }else {
                    Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
                }


            }else {
                Toast.makeText(getActivity(), "Something went wrong please try again", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        new CommonUtils().hideKeyboardOnLeaving(Objects.requireNonNull(getActivity()));
    }



    @SuppressLint("StaticFieldLeak")
    public class AsyncDeleteAllData extends AsyncTask<Object, Object, String> {

        @Override
        protected String doInBackground(Object... objects) {
            databaseHandler.daoAccess().deleteAllScanTagList();
            return null;
        }



    }

    public void resetInventoryDetail() {
        if (getActivity() != null) {
            if (btStart != null)
                btStart.setText(getString(R.string.start_title));
        }
    }
}
