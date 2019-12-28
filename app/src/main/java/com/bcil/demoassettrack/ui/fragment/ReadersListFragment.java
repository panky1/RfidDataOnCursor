package com.bcil.demoassettrack.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.ReaderListAdapter;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.custom.PasswordDialog;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.ui.activity.SettingsDetailActivity;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CustomProgressDialog;
import com.bcil.demoassettrack.utils.Inventorytimer;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;

import java.util.ArrayList;
import java.util.Objects;

public class ReadersListFragment extends Fragment {
    public static ArrayList<ReaderDevice> readersList = new ArrayList<>();
    private PasswordDialog passwordDialog;
    private DeviceConnectTask deviceConnectTask;
    //    private static final String RFD8500 = "RFD8500";
    private ReaderListAdapter readerListAdapter;
    private ListView pairedListView;
    private TextView tv_emptyView;
    private CustomProgressDialog progressDialog;

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
            if (MainActivity.isBluetoothEnabled()) {
                // Get the device MAC address, which is the last 17 chars in the View
                ReaderDevice readerDevice = readerListAdapter.getItem(pos);
                if (MyApp.mConnectedReader == null) {

                    if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                        MyApp.is_connection_requested = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                            deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                            deviceConnectTask.execute();
                        }
                    }
                } else {
                    {
                        if (MyApp.mConnectedReader.isConnected()) {
                            MyApp.is_disconnection_requested = true;
                            try {
                                MyApp.mConnectedReader.disconnect();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            }
                            //
                            bluetoothDeviceDisConnected(MyApp.mConnectedDevice);
                            if (MyApp.NOTIFY_READER_CONNECTION)
                                sendNotification(AppConstants.ACTION_READER_DISCONNECTED, "Disconnected from " + MyApp.mConnectedReader.getHostName());
                            //
                            clearSettings();
                        }
                        if (!MyApp.mConnectedReader.getHostName().equalsIgnoreCase(readerDevice.getName())) {
                            MyApp.mConnectedReader = null;
                            if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                                MyApp.is_connection_requested = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                                    deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                                    deviceConnectTask.execute();
                                }
                            }
                        } else {
                            MyApp.mConnectedReader = null;
                        }
                    }
                }
                // Create the result Intent and include the MAC address
            } else
                Toast.makeText(getActivity(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
        }
    };
    private PreferenceManager preferenceManager;

    public ReadersListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReadersListFragment.
     */
    public static ReadersListFragment newInstance() {
        return new ReadersListFragment();
    }

    private void clearSettings() {
        MainActivity.clearSettings();
        MainActivity.stopTimer();
        Inventorytimer.getInstance().stopTimer();
        MyApp.mIsInventoryRunning = false;
        if (MyApp.mIsInventoryRunning) {
            MyApp.isBatchModeInventoryRunning = false;
        }
        if (MyApp.isLocatingTag) {
            MyApp.isLocatingTag = false;
        }
        //update dpo icon in settings list
//        SettingsContent.ITEMS.get(8).icon = R.drawable.title_dpo_disabled;
        MyApp.mConnectedDevice = null;
        MyApp.isAccessCriteriaRead = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getActivity()));
    }

   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_readers_list, menu);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_readers_list, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeViews();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.dl_rdl);
        actionBar.setTitle(R.string.title_activity_readers_list);

        readersList.clear();
        loadPairedDevices();
        if (MyApp.mConnectedDevice != null) {
            int index = readersList.indexOf(MyApp.mConnectedDevice);
            if (index != -1) {
                readersList.remove(index);
                readersList.add(index, MyApp.mConnectedDevice);
            } else {
                MyApp.mConnectedDevice = null;
                MyApp.mConnectedReader = null;
            }
        }

        readerListAdapter = new ReaderListAdapter(getActivity(), R.layout.readers_list_item, readersList);

        if (readerListAdapter.getCount() == 0) {
            pairedListView.setEmptyView(tv_emptyView);
        } else
            pairedListView.setAdapter(readerListAdapter);

        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

    }

    private void initializeViews() {
        pairedListView = (ListView) getActivity().findViewById(R.id.bondedReadersList);
        tv_emptyView = (TextView) getActivity().findViewById(R.id.empty);
    }

    private void loadPairedDevices() {
        try {
            readersList.addAll(MyApp.readers.GetAvailableRFIDReaderList());
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * method to check whether BT device is RFID reader
//     *
//     * @param device device to check
//     * @return true if {@link android.bluetooth.BluetoothDevice} is RFID Reader, other wise it will be false
//     */
//    public static boolean isRFIDReader(BluetoothDevice device) {
//        if (device.getName().startsWith(RFD8500))
//            return true;
//        return false;
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PasswordDialog.isDialogShowing) {
            if (passwordDialog == null || !passwordDialog.isShowing()) {
                showPasswordDialog(MyApp.mConnectedDevice);
            }
        }
        capabilitiesRecievedforDevice();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (passwordDialog != null && passwordDialog.isShowing()) {
            PasswordDialog.isDialogShowing = true;
            passwordDialog.dismiss();
        }
    }

    /**
     * method to update connected reader device in the readers list on device connected event
     *
     * @param device device to be updated
     */
    public void bluetoothDeviceConnected(ReaderDevice device) {
//        if (deviceConnectTask != null)
//            deviceConnectTask.cancel(true);
        if (device != null) {
            MyApp.mConnectedDevice = device;
            MyApp.is_connection_requested = false;
            changeTextStyle(device);
        }
    }

    public void bluetoothDeviceDisConnected(ReaderDevice device) {
        if (deviceConnectTask != null && !deviceConnectTask.isCancelled() && deviceConnectTask.getConnectingDevice().getName().equalsIgnoreCase(device.getName())) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (deviceConnectTask != null)
                deviceConnectTask.cancel(true);
        }
        if (device != null) {
            changeTextStyle(device);
        }
        MainActivity.clearSettings();
    }

    public void readerDisconnected(ReaderDevice device) {
        if (device != null) {
            if (MyApp.mConnectedReader != null && !MyApp.AUTO_RECONNECT_READERS) {
                try {
                    MyApp.mConnectedReader.disconnect();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                MyApp.mConnectedReader = null;
            }
            for (int idx = 0; idx < readersList.size(); idx++) {
                if (readersList.get(idx).getName().equalsIgnoreCase(device.getName()))
                    changeTextStyle(readersList.get(idx));
            }
        }
    }

    /**
     * method to update reader device in the readers list on device connection failed event
     *
     * @param device device to be updated
     */
    public void bluetoothDeviceConnFailed(ReaderDevice device) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
        if (device != null)
            changeTextStyle(device);

        sendNotification(AppConstants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");

        MyApp.mConnectedReader = null;
        MyApp.mConnectedDevice = null;
    }

    /**
     * check/un check the connected/disconnected reader list item
     *
     * @param device device to be updated
     */
    private void changeTextStyle(ReaderDevice device) {
        int i = readerListAdapter.getPosition(device);
        if (i >= 0) {
            readerListAdapter.remove(device);
            readerListAdapter.insert(device,i);
            readerListAdapter.notifyDataSetChanged();
        }
    }

    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        if (readerListAdapter != null && readerDevice != null) {
            if (readerListAdapter.getCount() == 0) {
                tv_emptyView.setVisibility(View.GONE);
                pairedListView.setAdapter(readerListAdapter);
            }
            readersList.add(readerDevice);
            readerListAdapter.notifyDataSetChanged();
        }
    }

    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        if (readerListAdapter != null && readerDevice != null) {
            readerListAdapter.remove(readerDevice);
            readersList.remove(readerDevice);
            if (readerListAdapter.getCount() == 0) {
                pairedListView.setEmptyView(tv_emptyView);
            }
            readerListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * method to update serial and model of connected reader device
     */
    public void capabilitiesRecievedforDevice() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (readerListAdapter.getPosition(MyApp.mConnectedDevice) >= 0) {
                    ReaderDevice readerDevice = readerListAdapter.getItem(readerListAdapter.getPosition(MyApp.mConnectedDevice));
                    //readerDevice.setModel(MyApp.mConnectedDevice.getModel());
                    //readerDevice.setSerial(MyApp.mConnectedDevice.getSerial());
                    readerListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * method to show connect password dialog
     *
     * @param connectingDevice
     */
    public void showPasswordDialog(ReaderDevice connectingDevice) {
        if (MyApp.isActivityVisible()) {
            passwordDialog = new PasswordDialog(getActivity(), connectingDevice);
            passwordDialog.show();
        } else
            PasswordDialog.isDialogShowing = true;
    }

    /**
     * method to cancel progress dialog
     */
    public void cancelProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
    }

    public void ConnectwithPassword(String password, ReaderDevice readerDevice) {
        try {
            MyApp.mConnectedReader.disconnect();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        deviceConnectTask = new DeviceConnectTask(readerDevice, "Connecting with " + readerDevice.getName(), password);
        deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * method to get connect password for the reader
     *
     * @param address - device BT address
     * @return connect password of the reader
     */
    private String getReaderPassword(String address) {
        return preferenceManager.getPreferenceValues(AppConstants.READER_PASSWORDS);
    }

    private void sendNotification(String action, String data) {
        if (getActivity().getTitle().toString().equalsIgnoreCase(getString(R.string.title_activity_settings_detail)))
            ((SettingsDetailActivity) getActivity()).sendNotification(action, data);
        else
            ((MainActivity) getActivity()).sendNotification(action, data);
    }

    /**
     * async task to go for BT connection with reader
     */
    @SuppressLint("StaticFieldLeak")
    private class DeviceConnectTask extends AsyncTask<Void, String, Boolean> {
        private final ReaderDevice connectingDevice;
        private String prgressMsg;
        private OperationFailureException ex;
        private String password;

        DeviceConnectTask(ReaderDevice connectingDevice, String prgressMsg, String Password) {
            this.connectingDevice = connectingDevice;
            this.prgressMsg = prgressMsg;
            password = Password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(getActivity(), prgressMsg);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... a) {
            try {
                if (password != null)
                    connectingDevice.getRFIDReader().setPassword(password);
                connectingDevice.getRFIDReader().connect();
                if (password != null) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.READER_PASSWORDS, 0).edit();
                    editor.putString(connectingDevice.getName(), password);
                    editor.commit();
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                ex = e;
            }
            if (connectingDevice.getRFIDReader().isConnected()) {
                MyApp.mConnectedReader = connectingDevice.getRFIDReader();
                try {
                    MyApp.mConnectedReader.Events.addEventsListener(MyApp.eventHandler);
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                }
                connectingDevice.getRFIDReader().Events.setBatchModeEvent(true);
                connectingDevice.getRFIDReader().Events.setReaderDisconnectEvent(true);
                connectingDevice.getRFIDReader().Events.setBatteryEvent(true);
                connectingDevice.getRFIDReader().Events.setInventoryStopEvent(true);
                connectingDevice.getRFIDReader().Events.setInventoryStartEvent(true);
                // if no exception in connect
                if (ex == null) {
                    try {
                        MainActivity.UpdateReaderConnection(false);
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                } else {
                    MainActivity.clearSettings();
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.cancel();
            if (ex != null) {
                if (ex.getResults() == RFIDResults.RFID_CONNECTION_PASSWORD_ERROR) {
                    showPasswordDialog(connectingDevice);
                    bluetoothDeviceConnected(connectingDevice);
                } else if (ex.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                    MyApp.isBatchModeInventoryRunning = true;
                    MyApp.mIsInventoryRunning = true;
                    bluetoothDeviceConnected(connectingDevice);
                    if (MyApp.NOTIFY_READER_CONNECTION)
                        sendNotification(AppConstants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
                    //Events.StatusEventData data = MyApp.mConnectedReader.Events.GetStatusEventData(RFID_EVENT_TYPE.BATCH_MODE_EVENT);
//                    Intent detailsIntent = new Intent(getActivity(), MainActivity.class);
//                    detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    detailsIntent.putExtra(RFID_EVENT_TYPE.BATCH_MODE_EVENT.toString(), 0/*data.BatchModeEventData.get_RepeatTrigger()*/);
//                    startActivity(detailsIntent);
                } else if (ex.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
                    bluetoothDeviceConnected(connectingDevice);
                    MyApp.regionNotSet = true;
                    sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.set_region_msg));
                    Intent detailsIntent = new Intent(getActivity(), SettingsDetailActivity.class);
                    detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    detailsIntent.putExtra(AppConstants.SETTING_ITEM_ID, 7);
                    startActivity(detailsIntent);
                } else
                    bluetoothDeviceConnFailed(connectingDevice);
            } else {
                if (result) {
                    if (MyApp.NOTIFY_READER_CONNECTION)
                        sendNotification(AppConstants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
                    bluetoothDeviceConnected(connectingDevice);
                } else {
                    bluetoothDeviceConnFailed(connectingDevice);
                }
            }
            deviceConnectTask = null;
        }

        @Override
        protected void onCancelled() {
            deviceConnectTask = null;
            super.onCancelled();
        }

        public ReaderDevice getConnectingDevice() {
            return connectingDevice;
        }
    }
}

