package com.bcil.demoassettrack.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.RfidListAdapter;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.common.ResponseHandlerInterfaces;
import com.bcil.demoassettrack.custom.CustomToast;
import com.bcil.demoassettrack.inventory.InventoryListItem;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.service.BackgroundService;
import com.bcil.demoassettrack.service.NotificationsService;
import com.bcil.demoassettrack.ui.fragment.AssetAllocateFragment;
import com.bcil.demoassettrack.ui.fragment.AssetScrapFragment;
import com.bcil.demoassettrack.ui.fragment.AssetSoldFragment;
import com.bcil.demoassettrack.ui.fragment.LocTransferFragment;
import com.bcil.demoassettrack.ui.fragment.MainMenuFragment;
import com.bcil.demoassettrack.ui.fragment.MappingFragment;
import com.bcil.demoassettrack.ui.fragment.ReadersListFragment;
import com.bcil.demoassettrack.ui.fragment.ScanAssetFragment;
import com.bcil.demoassettrack.ui.fragment.SettingListFragment;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CustomProgressDialog;
import com.bcil.demoassettrack.utils.Inventorytimer;
import com.bcil.demoassettrack.utils.MyKeyboard;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.Constants;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.SetAttribute;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Readers.RFIDReaderEventHandler {
    private static final String MSG_WRITE = "Writing Data";
    private static final String MSG_READ = "Reading Tags";
    private long mBackPressed;
    private static final int TIME_INTERVAL = 3000;
    public static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private static ArrayList<ResponseHandlerInterfaces.BluetoothDeviceFoundHandler> bluetoothDeviceFoundHandlers = new ArrayList<>();
    private static ArrayList<ResponseHandlerInterfaces.BatteryNotificationHandler> batteryNotificationHandlers = new ArrayList<>();
    protected int accessTagCount;
    private AsyncTask<Void, Void, Boolean> DisconnectTask;
    private PreferenceManager preferenceManager;
    private boolean isInventoryAborted;
    private boolean isLocationingAborted;
    public static Timer t;
    private boolean pc = false;
    private boolean rssi = false;
    private boolean phase = false;
    private boolean channelIndex = false;
    private boolean tagSeenCount = false;
    protected Menu menu;
    private boolean isDeviceDisconnected = false;
    public int i =0;
    private RfidListAdapter rfidListAdapter;
    private ProgressDialog pDialog;

    public static void addBluetoothDeviceFoundHandler(ResponseHandlerInterfaces.BluetoothDeviceFoundHandler bluetoothDeviceFoundHandler) {
        bluetoothDeviceFoundHandlers.add(bluetoothDeviceFoundHandler);
    }

    public static void addBatteryNotificationHandler(ResponseHandlerInterfaces.BatteryNotificationHandler batteryNotificationHandler) {
        batteryNotificationHandlers.add(batteryNotificationHandler);
    }

    protected CustomProgressDialog progressDialog;
    private Boolean isTriggerRepeat;

    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static void clearSettings() {
        MyApp.antennaPowerLevel = null;
        MyApp.antennaRfConfig = null;
        MyApp.singulationControl = null;
        MyApp.rfModeTable = null;
        MyApp.regulatory = null;
        MyApp.batchMode = -1;
        MyApp.tagStorageSettings = null;
        MyApp.reportUniquetags = null;
        MyApp.dynamicPowerSettings = null;
        MyApp.settings_startTrigger = null;
        MyApp.settings_stopTrigger = null;
        MyApp.beeperVolume = null;
        MyApp.preFilters = null;
        if (MyApp.versionInfo != null)
            MyApp.versionInfo.clear();
        MyApp.regionNotSet = false;
        MyApp.isBatchModeInventoryRunning = null;
        MyApp.BatteryData = null;
        MyApp.is_disconnection_requested = false;
        MyApp.mConnectedDevice = null;
//        Application.mConnectedReader = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        preferenceManager = new PreferenceManager(MainActivity.this);
        initBluetooth();
        initData();
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this,new Intent(MainActivity.this, MyKeyboard.class));

        }else {
            startService(new Intent(MainActivity.this,MyKeyboard.class));
        }*/
//        callAutoStartEvent();
    }

    private void callAutoStartEvent() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage("To receive Notifications Properly AmulAssetTracking needs to enabled in Security App-Autostart. Do you want to enable it now?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try{
                            Intent intent1 = new Intent();
                            if (AppConstants.XIAOMIMANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
                                //this will open auto start screen where user can enable permission for your app
                                intent1.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                                preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                startActivity(intent1);
                            } else if (AppConstants.OPPOMANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
                                try{
                                    intent1.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                                    preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                    startActivity(intent1);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    try{
                                        Intent i = new Intent(Intent.ACTION_MAIN);
                                        preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                        i.setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"));
                                        startActivity(i);
                                    }catch (Exception e1){
                                        e1.printStackTrace();
                                        try{
                                            Intent intent = new Intent();
                                            intent.setClassName("com.coloros.safecenter",
                                                    "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                            preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                            startActivity(intent);
                                        }catch (Exception e2){
                                            e2.printStackTrace();
                                        }
                                    }
                                }
                            } else if (AppConstants.VIVOMANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
                                try{
                                    intent1.setComponent(new ComponentName("com.vivo.permissikonmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                                    preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                    startActivity(intent1);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    try{
                                        Intent intent = new Intent();
                                        intent.setComponent(new ComponentName("com.iqoo.secure",
                                                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                                        preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                        startActivity(intent);
                                    }catch (Exception e1){
                                        e1.printStackTrace();
                                        try{
                                            Intent intent = new Intent();
                                            intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                                                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                                            preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                            startActivity(intent);
                                        }catch (Exception e2){
                                            e2.printStackTrace();
                                            try {
                                                Intent intent = new Intent();
                                                intent.setClassName("com.iqoo.secure",
                                                        "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager");
                                                preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                                startActivity(intent);
                                            }catch (Exception e3){
                                                e3.printStackTrace();
                                            }
                                        }
                                    }
                                }

                            } else if (AppConstants.HUAWEIMANUFACTURER.equalsIgnoreCase(Build.MANUFACTURER)) {
                                try{
                                    intent1.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                                    preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, "1");
                                    startActivity(intent1);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        preferenceManager.putPreferenceValues(AppConstants.AUTOSTARTSTATE, null);
                        arg0.dismiss();
                    }
                });
        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }


    private void initBluetooth() {
        MyApp.eventHandler = new EventHandler();
        Inventorytimer.getInstance().setActivity(MainActivity.this);
        initializeConnectionSettings();

        if (MyApp.readers == null) {
            MyApp.readers = new Readers();
        }
        MyApp.readers.attach(this);
        if (!isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
    }

    private void initializeConnectionSettings() {
        MyApp.AUTO_DETECT_READERS = preferenceManager.getBoolean(AppConstants.AUTO_DETECT_READERS);
        MyApp.AUTO_RECONNECT_READERS = preferenceManager.getBoolean(AppConstants.AUTO_RECONNECT_READERS);
        MyApp.NOTIFY_READER_AVAILABLE = preferenceManager.getBoolean(AppConstants.NOTIFY_READER_AVAILABLE);
        MyApp.NOTIFY_READER_CONNECTION = preferenceManager.getBoolean(AppConstants.NOTIFY_READER_CONNECTION);
        MyApp.NOTIFY_BATTERY_STATUS = preferenceManager.getBoolean(AppConstants.NOTIFY_BATTERY_STATUS);
        MyApp.EXPORT_DATA = preferenceManager.getBoolean(AppConstants.EXPORT_DATA);
    }

    private void initData() {
        MainMenuFragment mainMenuFragment = new MainMenuFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, mainMenuFragment, TAG_CONTENT_FRAGMENT);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            finishAffinity();
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
        } else {
            Toast.makeText(getBaseContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }


    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment)
            ((ReadersListFragment) fragment).RFIDReaderAppeared(readerDevice);
        if (MyApp.NOTIFY_READER_AVAILABLE) {
            if (!readerDevice.getName().equalsIgnoreCase("null"))
                sendNotification(AppConstants.ACTION_READER_AVAILABLE, readerDevice.getName() + " is available.");
        }
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        MyApp.mReaderDisappeared = readerDevice;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment)
            ((ReadersListFragment) fragment).RFIDReaderDisappeared(readerDevice);
        if (MyApp.NOTIFY_READER_AVAILABLE)
            sendNotification(AppConstants.ACTION_READER_AVAILABLE, readerDevice.getName() + " is unavailable.");
    }

    public void connectClicked(String password, ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).ConnectwithPassword(password, readerDevice);
        }
    }

    public void cancelClicked(ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).readerDisconnected(readerDevice);
        }
    }

    /*public void onWriteClicked(View view) {
        if (isBluetoothEnabled()) {
            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected()) {
                if (MyApp.mConnectedReader.isCapabilitiesReceived()) {
                    final String tagIDField = new MappingFragment().tagIDField;
                    if (tagIDField != null) {
                        if (!tagIDField.isEmpty()) {
                            MyApp.accessControlTag = tagIDField;
                            String offset = new MappingFragment().offset;
                            if (!offset.isEmpty()) {
                                progressDialog = new CustomProgressDialog(this, MSG_WRITE);
                                progressDialog.show();
                                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                                timerDelayRemoveDialog(Constants.RESPONSE_TIMEOUT, progressDialog, "Write",MainActivity.this);
                                MyApp.isAccessCriteriaRead = true;

                                //((AccessOperationsFragment) fragment).setStartStopTriggers();

                                TagAccess tagAccess = new TagAccess();
                                final TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
                                try {
                                    writeAccessParams.setAccessPassword(Long.decode("0X" + ("00")));
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Password field is empty or invalid !", Toast.LENGTH_SHORT).show();
                                }
                                writeAccessParams.setWriteDataLength(new MappingFragment().writeData.length()/4);

                                writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);

                                writeAccessParams.setOffset(Integer.parseInt(offset));
                                writeAccessParams.setWriteData(new MappingFragment().writeData);

                                new AsyncTask<Void, Void, Boolean>() {
                                    private InvalidUsageException invalidUsageException;
                                    private OperationFailureException operationFailureException;
                                    private Boolean bResult = false;

                                    @Override
                                    protected Boolean doInBackground(Void... voids) {
                                        try {
                                            MyApp.mConnectedReader.Actions.TagAccess.writeWait(tagIDField, writeAccessParams, null, null);
                                            bResult = true;
                                        } catch (InvalidUsageException e) {
                                            invalidUsageException = e;
                                            e.printStackTrace();
                                        } catch (OperationFailureException e) {
                                            operationFailureException = e;
                                            e.printStackTrace();
                                        }
                                        return bResult;
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean result) {
                                        if (!result) {
                                            if (invalidUsageException != null) {
                                                pDialog.dismiss();
                                                sendNotification1(Constants.ACTION_READER_STATUS_OBTAINED, invalidUsageException.getInfo(),MainActivity.this);
                                            } else if (operationFailureException != null) {
                                                pDialog.dismiss();
                                                sendNotification1(Constants.ACTION_READER_STATUS_OBTAINED, operationFailureException.getVendorMessage(),MainActivity.this);
                                            }
                                        } else
                                            Toast.makeText(getApplicationContext(), getString(R.string.msg_write_succeed), Toast.LENGTH_SHORT).show();
                                    }
                                }.execute();
                            }
                        }
                    }
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_reader_not_updated), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }*/


    public class EventHandler implements RfidEventsListener {

        @Override
        public void eventReadNotify(RfidReadEvents e) {
            final TagData[] myTags = MyApp.mConnectedReader.Actions.getReadTags(100);
            if (myTags != null) {
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                for (int index = 0; index < myTags.length; index++) {
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
//                        if (myTags[index].getMemoryBankData().length() > 0) {
//                            System.out.println(" Mem Bank Data " + myTags[index].getMemoryBankData());
//                        }
                    }
                    if (myTags[index].isContainsLocationInfo()) {
                        final int tag = index;
                        MyApp.TagProximityPercent = myTags[tag].LocationInfo.getRelativeDistance();
                        /*if (fragment instanceof LocationingFragment)
                            ((LocationingFragment) fragment).handleLocateTagResponse();*/
                    }
                    if (MyApp.isAccessCriteriaRead && !MyApp.mIsInventoryRunning) {
                        accessTagCount++;
                    } else {
                        if (myTags[index] != null && (myTags[index].getOpStatus() == null || myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)) {
                            final int tag = index;
                            new ResponseHandlerTask(myTags[tag], fragment).execute();
                        }
                    }
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            System.out.println("Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            notificationFromGenericReader(rfidStatusEvents);
        }
    }

    private void notificationFromGenericReader(RfidStatusEvents rfidStatusEvents) {

        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            if (MyApp.mConnectedReader != null)
                DisconnectTask = new UpdateDisconnectedStatusTask(MyApp.mConnectedReader.getHostName()).execute();
//            Application.mConnectedReader = null;
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
            if (!MyApp.isAccessCriteriaRead && !MyApp.isLocatingTag) {
                //if (!getRepeatTriggers() && Inventorytimer.getInstance().isTimerRunning()) {
                MyApp.mIsInventoryRunning = true;
                Inventorytimer.getInstance().startTimer();
                //}
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            accessTagCount = 0;
            MyApp.isAccessCriteriaRead = false;

            if (MyApp.mIsInventoryRunning) {
                Inventorytimer.getInstance().stopTimer();
            } else if (MyApp.isGettingTags) {
                MyApp.isGettingTags = false;
                MyApp.mConnectedReader.Actions.purgeTags();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (fragment instanceof ReadersListFragment) {
                            //((ReadersListFragment) fragment).cancelProgressDialog();
                            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.ReaderCapabilities.getModelName() != null) {
                                ((ReadersListFragment) fragment).capabilitiesRecievedforDevice();
                            }
                        }
                    }
                });
            }

            if (!getRepeatTriggers()) {
                if (MyApp.mIsInventoryRunning)
                    isInventoryAborted = true;
                else if (MyApp.isLocatingTag)
                    isLocationingAborted = true;
                operationHasAborted();
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.OPERATION_END_SUMMARY_EVENT) {
            /*if (fragment instanceof ScanAssetFragment)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ScanAssetFragment) fragment).updateInventoryDetails();
                    }
                });*/
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
            Boolean triggerPressed = false;
            if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED)
                triggerPressed = true;
            if (fragment instanceof ResponseHandlerInterfaces.TriggerEventHandler) {
                if (triggerPressed && (MyApp.settings_startTrigger.getTriggerType().toString().equalsIgnoreCase(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE.toString()) || (isTriggerRepeat != null && !isTriggerRepeat)))
                    ((ResponseHandlerInterfaces.TriggerEventHandler) fragment).triggerPressEventRecieved();
                else if (!triggerPressed && (MyApp.settings_stopTrigger.getTriggerType().toString().equalsIgnoreCase(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE.toString()) || (isTriggerRepeat != null && !isTriggerRepeat)))
                    ((ResponseHandlerInterfaces.TriggerEventHandler) fragment).triggerReleaseEventRecieved();
            }
        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BATTERY_EVENT) {
            final Events.BatteryData batteryData = rfidStatusEvents.StatusEventData.BatteryData;
            MyApp.BatteryData = batteryData;
            setActionBarBatteryStatus(batteryData.getLevel());

            if (batteryNotificationHandlers != null && batteryNotificationHandlers.size() > 0) {
                for (ResponseHandlerInterfaces.BatteryNotificationHandler batteryNotificationHandler : batteryNotificationHandlers)
                    batteryNotificationHandler.deviceStatusReceived(batteryData.getLevel(), batteryData.getCharging(), batteryData.getCause());
            }
            if (MyApp.NOTIFY_BATTERY_STATUS && batteryData.getCause() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (batteryData.getCause().trim().equalsIgnoreCase(AppConstants.MESSAGE_BATTERY_CRITICAL))
                            sendNotification(AppConstants.ACTION_READER_BATTERY_CRITICAL, getString(R.string.battery_status__critical_message));
                        else if (batteryData.getCause().trim().equalsIgnoreCase(AppConstants.MESSAGE_BATTERY_LOW))
                            sendNotification(AppConstants.ACTION_READER_BATTERY_CRITICAL, getString(R.string.battery_status_low_message));
                    }
                });
            }

        } else if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BATCH_MODE_EVENT) {
            MyApp.isBatchModeInventoryRunning = true;
            startTimer();
            clearInventoryData();
            MyApp.mIsInventoryRunning = true;
            MyApp.memoryBankId = 0;
            isTriggerRepeat = rfidStatusEvents.StatusEventData.BatchModeEventData.get_RepeatTrigger();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (fragment instanceof ResponseHandlerInterfaces.BatchModeEventHandler)
                        ((ResponseHandlerInterfaces.BatchModeEventHandler) fragment).batchModeEventReceived();
                    if (fragment instanceof ReadersListFragment) {
                        //((ReadersListFragment) fragment).cancelProgressDialog();
                        if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.ReaderCapabilities.getModelName() == null) {
                            ((ReadersListFragment) fragment).capabilitiesRecievedforDevice();
                        }
                    }
                }
            });
        }
    }

    public void clearInventoryData() {
        MyApp.TOTAL_TAGS = 0;
        MyApp.mRRStartedTime = 0;
        MyApp.UNIQUE_TAGS = 0;
        MyApp.TAG_READ_RATE = 0;
        if (MyApp.tagIDs != null)
            MyApp.tagIDs.clear();
        if (MyApp.tagsReadInventory.size() > 0)
            MyApp.tagsReadInventory.clear();
        if (MyApp.tagsReadInventory.size() > 0)
            MyApp.tagsReadInventory.clear();
        if (MyApp.inventoryList != null && MyApp.inventoryList.size() > 0)
            MyApp.inventoryList.clear();
    }

    public void sendNotification(String action, String data) {
        if (MyApp.isActivityVisible()) {
            if (action.equalsIgnoreCase(AppConstants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(AppConstants.ACTION_READER_BATTERY_LOW)) {
                new CustomToast(MainActivity.this, R.layout.toast_layout, data).show();
            } else {
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent i = new Intent(MainActivity.this, NotificationsService.class);
            i.putExtra(AppConstants.INTENT_ACTION, action);
            i.putExtra(AppConstants.INTENT_DATA, data);
            startService(i);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void operationHasAborted() {
        //retrieve get tags if inventory in batch mode got aborted
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (MyApp.isBatchModeInventoryRunning != null && MyApp.isBatchModeInventoryRunning) {
            if (isInventoryAborted) {
                MyApp.isBatchModeInventoryRunning = false;
                isInventoryAborted = true;
                MyApp.isGettingTags = true;
                if (MyApp.settings_startTrigger == null) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            try {
                                if (MyApp.mConnectedReader.isCapabilitiesReceived())
                                    UpdateReaderConnection(false);
                                else
                                    UpdateReaderConnection(true);
                                // update fields before getting tags
                                getTagReportingfields();
                                //
                                MyApp.mConnectedReader.Actions.getBatchedTags();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                } else
                    MyApp.mConnectedReader.Actions.getBatchedTags();
            }
        }

        if (MyApp.mIsInventoryRunning) {
            if (isInventoryAborted) {
                MyApp.mIsInventoryRunning = false;
                isInventoryAborted = false;
                isTriggerRepeat = null;
                if (Inventorytimer.getInstance().isTimerRunning())
                    Inventorytimer.getInstance().stopTimer();
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                       /* if (fragment instanceof InventoryFragment)
                            ((InventoryFragment) fragment).resetInventoryDetail();
                        else*/
                        if (fragment instanceof ScanAssetFragment)
                            ((ScanAssetFragment) fragment).resetInventoryDetail();
                        if (fragment instanceof MappingFragment)
                            ((MappingFragment) fragment).resetInventoryDetail();
                        if (fragment instanceof AssetAllocateFragment)
                            ((AssetAllocateFragment) fragment).resetInventoryDetail();
                        if (fragment instanceof LocTransferFragment)
                            ((LocTransferFragment) fragment).resetInventoryDetail();
                        if (fragment instanceof AssetScrapFragment)
                            ((AssetScrapFragment) fragment).resetInventoryDetail();
                        if (fragment instanceof AssetSoldFragment)
                            ((AssetSoldFragment) fragment).resetInventoryDetail();
                    }
                });
            }
        } else if (MyApp.isLocatingTag) {
            if (isLocationingAborted) {
                MyApp.isLocatingTag = false;
                isLocationingAborted = false;
                /*if (fragment instanceof LocationingFragment)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((LocationingFragment) fragment).resetLocationingDetails(false);
                        }
                    });*/
            }
        }
    }

    private void getTagReportingfields() {
        pc = false;
        phase = false;
        channelIndex = false;
        rssi = false;
        if (MyApp.tagStorageSettings != null) {
            TAG_FIELD[] tag_field = MyApp.tagStorageSettings.getTagFields();
            for (int idx = 0; idx < tag_field.length; idx++) {
                if (tag_field[idx] == TAG_FIELD.PEAK_RSSI)
                    rssi = true;
                if (tag_field[idx] == TAG_FIELD.PHASE_INFO)
                    phase = true;
                if (tag_field[idx] == TAG_FIELD.PC)
                    pc = true;
                if (tag_field[idx] == TAG_FIELD.CHANNEL_INDEX)
                    channelIndex = true;
                if (tag_field[idx] == TAG_FIELD.TAG_SEEN_COUNT)
                    tagSeenCount = true;
            }
        }
    }

    public static void UpdateReaderConnection(Boolean fullUpdate) throws InvalidUsageException, OperationFailureException {
        MyApp.mConnectedReader.Events.setBatchModeEvent(true);
        MyApp.mConnectedReader.Events.setReaderDisconnectEvent(true);
        MyApp.mConnectedReader.Events.setInventoryStartEvent(true);
        MyApp.mConnectedReader.Events.setInventoryStopEvent(true);
        MyApp.mConnectedReader.Events.setTagReadEvent(true);
        MyApp.mConnectedReader.Events.setHandheldEvent(true);
        MyApp.mConnectedReader.Events.setBatteryEvent(true);
        MyApp.mConnectedReader.Events.setPowerEvent(true);
        MyApp.mConnectedReader.Events.setOperationEndSummaryEvent(true);

        if (fullUpdate)
            MyApp.mConnectedReader.PostConnectReaderUpdate();

        MyApp.regulatory = MyApp.mConnectedReader.Config.getRegulatoryConfig();
        MyApp.regionNotSet = false;
        MyApp.rfModeTable = MyApp.mConnectedReader.ReaderCapabilities.RFModes.getRFModeTableInfo(0);
        MyApp.antennaRfConfig = MyApp.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
        MyApp.singulationControl = MyApp.mConnectedReader.Config.Antennas.getSingulationControl(1);
        MyApp.settings_startTrigger = MyApp.mConnectedReader.Config.getStartTrigger();
        MyApp.settings_stopTrigger = MyApp.mConnectedReader.Config.getStopTrigger();
        MyApp.tagStorageSettings = MyApp.mConnectedReader.Config.getTagStorageSettings();
        MyApp.dynamicPowerSettings = MyApp.mConnectedReader.Config.getDPOState();
        MyApp.beeperVolume = MyApp.mConnectedReader.Config.getBeeperVolume();
        MyApp.batchMode = MyApp.mConnectedReader.Config.getBatchModeConfig().getValue();
        MyApp.reportUniquetags = MyApp.mConnectedReader.Config.getUniqueTagReport();
        MyApp.mConnectedReader.Config.getDeviceVersionInfo(MyApp.versionInfo);
        // set RFID mode using attribute API
        SetAttribute setAttributeInfo = new SetAttribute();
        setAttributeInfo.setAttnum(1664);
        setAttributeInfo.setAtttype("B");
        setAttributeInfo.setAttvalue("0");
        MyApp.mConnectedReader.Config.setAttribute(setAttributeInfo);
        startTimer();
    }

    private static void startTimer() {
        if (t == null) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (MyApp.mConnectedReader != null)
                            MyApp.mConnectedReader.Config.getDeviceStatus(true, false, false);
                        else
                            stopTimer();
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                }
            };
            t = new Timer();
            t.scheduleAtFixedRate(task, 0, 60000);
        }
    }

    public static void stopTimer() {
        if (t != null) {
            t.cancel();
            t.purge();
        }
        t = null;
    }

    private boolean getRepeatTriggers() {
        if ((MyApp.settings_startTrigger != null && (MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD || MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC))
                || (isTriggerRepeat != null && isTriggerRepeat))
            return true;
        else
            return false;
    }

    @SuppressLint("StaticFieldLeak")
    public class ResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {
        private TagData tagData;
        private InventoryListItem inventoryItem;
        private InventoryListItem oldObject;
        private Fragment fragment;
        private String memoryBank;
        private String memoryBankData;

        ResponseHandlerTask(TagData tagData, Fragment fragment) {
            this.tagData = tagData;
            this.fragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean added = false;
            try {
                if (MyApp.inventoryList.containsKey(tagData.getTagID())) {
                    inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                    int index = MyApp.inventoryList.get(tagData.getTagID());
                    if (index >= 0) {
                        //Tag is already present. Update the fields and increment the count
                        if (tagData.getOpCode() != null)
                            if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                memoryBank = tagData.getMemoryBank().toString();
                                memoryBankData = tagData.getMemoryBankData().toString();
                            }
                        oldObject = MyApp.tagsReadInventory.get(index);
                        int tagSeenCount = 0;
                        if (Integer.toString(tagData.getTagSeenCount()) != null)
                            tagSeenCount = tagData.getTagSeenCount();
                        if (tagSeenCount != 0) {
                            MyApp.TOTAL_TAGS += tagSeenCount;
                            oldObject.incrementCountWithTagSeenCount(tagSeenCount);
                        } else {
                            MyApp.TOTAL_TAGS++;
                            oldObject.incrementCount();
                        }
                        if (oldObject.getMemoryBankData() != null && !oldObject.getMemoryBankData().equalsIgnoreCase(memoryBankData))
                            oldObject.setMemoryBankData(memoryBankData);
                        if (pc)
                            oldObject.setPC(Integer.toHexString(tagData.getPC()));
                        if (phase)
                            oldObject.setPhase(Integer.toString(tagData.getPhase()));
                        if (channelIndex)
                            oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                        if (rssi)
                            oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                    }
                } else {
                    //Tag is encountered for the first time. Add it.
                    if (MyApp.inventoryMode == 0 || (MyApp.inventoryMode == 1 && MyApp.UNIQUE_TAGS <= AppConstants.UNIQUE_TAG_LIMIT)) {
                        int tagSeenCount = 0;
                        if (Integer.toString(tagData.getTagSeenCount()) != null)
                            tagSeenCount = tagData.getTagSeenCount();
                        if (tagSeenCount != 0) {
                            MyApp.TOTAL_TAGS += tagSeenCount;
                            inventoryItem = new InventoryListItem(tagData.getTagID(), tagSeenCount, null, null, null, null, null, null);
                        } else {
                            MyApp.TOTAL_TAGS++;
                            inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                        }
                        added = MyApp.tagsReadInventory.add(inventoryItem);
                        if (added) {
                            MyApp.inventoryList.put(tagData.getTagID(), MyApp.UNIQUE_TAGS);
                            if (tagData.getOpCode() != null)

                                if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                    memoryBank = tagData.getMemoryBank().toString();
                                    memoryBankData = tagData.getMemoryBankData().toString();

                                }
                            oldObject = MyApp.tagsReadInventory.get(MyApp.UNIQUE_TAGS);
                            oldObject.setMemoryBankData(memoryBankData);
                            oldObject.setMemoryBank(memoryBank);
                            if (pc)
                                oldObject.setPC(Integer.toHexString(tagData.getPC()));
                            if (phase)
                                oldObject.setPhase(Integer.toString(tagData.getPhase()));
                            if (channelIndex)
                                oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                            if (rssi)
                                oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                            MyApp.UNIQUE_TAGS++;
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                //logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                oldObject = null;
                added = false;
            } catch (Exception e) {
                // logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                oldObject = null;
                added = false;
            }
            inventoryItem = null;
            memoryBank = null;
            memoryBankData = null;
            return added;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            cancel(true);
            if (oldObject != null && fragment instanceof ResponseHandlerInterfaces.ResponseTagHandler)
                ((ResponseHandlerInterfaces.ResponseTagHandler) fragment).handleTagResponse(oldObject, result);
            oldObject = null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    protected class UpdateDisconnectedStatusTask extends AsyncTask<Void, Void, Boolean> {
        private final String device;
        // store current reader state
        private final ReaderDevice readerDevice;
        long disconnectedTime;

        public UpdateDisconnectedStatusTask(String device) {
            this.device = device;
            disconnectedTime = System.currentTimeMillis();
            // store current reader state
            readerDevice = MyApp.mConnectedDevice;
            //
            MyApp.mReaderDisappeared = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void run() {
                    if (readerDevice != null && readerDevice.getName().equalsIgnoreCase(device))
                        readerDisconnected(readerDevice);
                    else
                        readerDisconnected(new ReaderDevice(device, null));
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //Check if the connected device is one we had comm with
            if (!MyApp.is_disconnection_requested && MyApp.AUTO_RECONNECT_READERS && readerDevice != null && device != null && device.equalsIgnoreCase(readerDevice.getName())) {
                if (isBluetoothEnabled()) {
                    boolean bConnected = false;
                    int retryCount = 0;
                    while (!bConnected && retryCount < 10) {
                        if (isCancelled() || isDeviceDisconnected)
                            break;
                        try {
                            Thread.sleep(1000);
                            retryCount++;
                            // check manual connection is initiated
                            if (MyApp.is_connection_requested || isCancelled())
                                break;
                            readerDevice.getRFIDReader().reconnect();
                            bConnected = true;
                            // break temporary pairing connection if reader is unpaired
                            if (MyApp.mReaderDisappeared != null && MyApp.mReaderDisappeared.getName().equalsIgnoreCase(readerDevice.getName())) {
                                readerDevice.getRFIDReader().disconnect();
                                bConnected = false;
                                break;
                            }
                        } catch (InvalidUsageException e) {
                        } catch (OperationFailureException e) {
                            if (e.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                                MyApp.isBatchModeInventoryRunning = true;
                                bConnected = true;
                            }
                            if (e.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
                                try {
                                    readerDevice.getRFIDReader().disconnect();
                                    bConnected = false;
                                    break;
                                } catch (InvalidUsageException e1) {
                                    e1.printStackTrace();
                                } catch (OperationFailureException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return bConnected;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {
                if (result)
                    readerReconnected(readerDevice);
                else if (!MyApp.is_connection_requested) {
                    sendNotification(AppConstants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");
                    try {
                        readerDevice.getRFIDReader().disconnect();
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void readerReconnected(ReaderDevice readerDevice) {
        MyApp.mConnectedDevice = readerDevice;
        MyApp.mConnectedReader = readerDevice.getRFIDReader();
        //
        if (MyApp.isBatchModeInventoryRunning != null && MyApp.isBatchModeInventoryRunning) {
            clearInventoryData();
            MyApp.mIsInventoryRunning = true;
            MyApp.memoryBankId = 0;
            startTimer();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof ResponseHandlerInterfaces.BatchModeEventHandler)
                ((ResponseHandlerInterfaces.BatchModeEventHandler) fragment).batchModeEventReceived();
        } else
            try {
                UpdateReaderConnection(false);
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        // connect call
        bluetoothDeviceConnected(readerDevice);
    }

    private void bluetoothDeviceConnected(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).bluetoothDeviceConnected(device);
        } else if (fragment instanceof SettingListFragment) {
            ((SettingListFragment) fragment).settingsListUpdated();
        }
//        else if(fragment instanceof AccessOperationsFragment)
//            ((AccessOperationsFragment) fragment).deviceConnected(device);
        if (bluetoothDeviceFoundHandlers != null && bluetoothDeviceFoundHandlers.size() > 0) {
            for (ResponseHandlerInterfaces.BluetoothDeviceFoundHandler bluetoothDeviceFoundHandler : bluetoothDeviceFoundHandlers)
                bluetoothDeviceFoundHandler.bluetoothDeviceConnected(device);
        }
        if (MyApp.NOTIFY_READER_CONNECTION)
            sendNotification(AppConstants.ACTION_READER_CONNECTED, "Connected to " + device.getName());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void readerDisconnected(ReaderDevice readerDevice) {
        stopTimer();
        //updateConnectedDeviceDetails(readerDevice, false);
        if (MyApp.NOTIFY_READER_CONNECTION)
            sendNotification(AppConstants.ACTION_READER_DISCONNECTED, "Disconnected from " + readerDevice.getName());
        clearSettings();
        setActionBarBatteryStatus(0);
        bluetoothDeviceDisConnected(readerDevice);
        MyApp.mConnectedDevice = null;
        MyApp.mConnectedReader = null;
        MyApp.is_disconnection_requested = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bluetoothDeviceDisConnected(ReaderDevice device) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

        if (MyApp.mIsInventoryRunning) {
            inventoryAborted();
            MyApp.isBatchModeInventoryRunning = false;
        }
        if (MyApp.isLocatingTag) {
            MyApp.isLocatingTag = false;
        }
        //update dpo icon in settings list
//        SettingsContent.ITEMS.get(8).icon = R.drawable.title_dpo_disabled;

        MyApp.isAccessCriteriaRead = false;
        accessTagCount = 0;

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).readerDisconnected(device);
            ((ReadersListFragment) fragment).bluetoothDeviceDisConnected(device);
        } else if (fragment instanceof ScanAssetFragment) {
            ((ScanAssetFragment) fragment).resetInventoryDetail();
        }else if (fragment instanceof MappingFragment) {
            ((MappingFragment) fragment).resetInventoryDetail();
        }else if (fragment instanceof AssetAllocateFragment) {
            ((AssetAllocateFragment) fragment).resetInventoryDetail();
        }else if (fragment instanceof LocTransferFragment) {
            ((LocTransferFragment) fragment).resetInventoryDetail();
        }else if (fragment instanceof AssetScrapFragment) {
            ((AssetScrapFragment) fragment).resetInventoryDetail();
        }else if (fragment instanceof AssetSoldFragment) {
            ((AssetSoldFragment) fragment).resetInventoryDetail();
        }else if (fragment instanceof SettingListFragment) {
            ((SettingListFragment) fragment).settingsListUpdated();
        }

        if (bluetoothDeviceFoundHandlers != null && bluetoothDeviceFoundHandlers.size() > 0) {
            for (ResponseHandlerInterfaces.BluetoothDeviceFoundHandler bluetoothDeviceFoundHandler : bluetoothDeviceFoundHandlers)
                bluetoothDeviceFoundHandler.bluetoothDeviceDisConnected(device);
        }

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
    }

    private void inventoryAborted() {
        Inventorytimer.getInstance().stopTimer();
        MyApp.mIsInventoryRunning = false;
    }

    public void setActionBarBatteryStatus(final int level) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (menu != null && menu.findItem(R.id.action_dpo) != null) {
                    if (MyApp.dynamicPowerSettings != null && MyApp.dynamicPowerSettings.getValue() == 1) {
                        menu.findItem(R.id.action_dpo).setIcon(R.drawable.action_battery_dpo_level);
                    } else {
                        menu.findItem(R.id.action_dpo).setIcon(R.drawable.action_battery_level);
                    }
                    menu.findItem(R.id.action_dpo).getIcon().setLevel(level);
                }
            }
        });
    }


/*
    public void accessOperationsWriteClicked(final String tagIDField, String offset, String writeData, final Context context) {
        if (isBluetoothEnabled()) {
            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected()) {
                if (MyApp.mConnectedReader.isCapabilitiesReceived()) {
                    if (tagIDField != null ) {
                        if (!tagIDField.isEmpty()) {
                            MyApp.accessControlTag = tagIDField;
                            String lengthText = "0";
                            if (!offset.isEmpty()) {
                                if (!lengthText.isEmpty()) {
                                    progressDialog = new CustomProgressDialog(context, MSG_READ);
                                    progressDialog.show();
                                    final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);


                                    timerDelayRemoveDialog(Constants.RESPONSE_TIMEOUT, progressDialog, "Read",context);
                                    //Stop the read after a tag is read to avoid performing a continuous read operation
                                    //((AccessOperationsFragment) fragment).setStartStopTriggers();
                                    MyApp.isAccessCriteriaRead = true;

                                    TagAccess tagAccess = new TagAccess();
                                    final TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
                                    try {
                                        readAccessParams.setAccessPassword(Long.decode("0X" + "00"));
                                    } catch (NumberFormatException nfe) {
                                        nfe.printStackTrace();
                                        Toast.makeText(this, "Password field is empty or invalid !", Toast.LENGTH_SHORT).show();
                                    }
                                    readAccessParams.setCount(Integer.parseInt(lengthText));


                                        readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);

                                    readAccessParams.setOffset(Integer.parseInt(offset));

                                    new AsyncTask<Void, Void, Boolean>() {
                                        private InvalidUsageException invalidUsageException;
                                        private OperationFailureException operationFailureException;

                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                final TagData tagData = MyApp.mConnectedReader.Actions.TagAccess.readWait(tagIDField, readAccessParams, null);
                                                if (MyApp.isAccessCriteriaRead && !MyApp.mIsInventoryRunning) {
                                                    if (fragment instanceof MappingFragment)
                                                        ((MappingFragment) fragment).handleTagResponse1(tagData);
                                                }
                                            } catch (InvalidUsageException e) {
                                                invalidUsageException = e;
                                                e.printStackTrace();
                                            } catch (OperationFailureException e) {
                                                operationFailureException = e;
                                                e.printStackTrace();
                                            }
                                            return true;
                                        }

                                        @Override
                                        protected void onPostExecute(Boolean result) {
                                            if (invalidUsageException != null) {
                                                progressDialog.dismiss();
                                                sendNotification1(Constants.ACTION_READER_STATUS_OBTAINED, invalidUsageException.getInfo(),context);
                                            } else if (operationFailureException != null) {
                                                progressDialog.dismiss();
                                                sendNotification1(Constants.ACTION_READER_STATUS_OBTAINED, operationFailureException.getVendorMessage(),context);
                                            }
                                        }
                                    }.execute();
                                }
                            }
                        }
                    }
                } else
                    Toast.makeText(context, getResources().getString(R.string.error_reader_not_updated), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }
*/

    private void sendNotification1(String action, String data, Context context) {
            if (MyApp.isActivityVisible()) {
                if (action.equalsIgnoreCase(AppConstants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(AppConstants.ACTION_READER_BATTERY_LOW)) {
                    new CustomToast(context, R.layout.toast_layout, data).show();
                } else {
                    Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent i = new Intent(context, NotificationsService.class);
                i.putExtra(AppConstants.INTENT_ACTION, action);
                i.putExtra(AppConstants.INTENT_DATA, data);
                startService(i);
            }


    }

    public void timerDelayRemoveDialog(long time, final Dialog d, final String command, final Context context) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    d.dismiss();
                    if (MyApp.isAccessCriteriaRead) {
                        if (accessTagCount == 0)
                            sendNotification1(Constants.ACTION_READER_STATUS_OBTAINED, context.getString(R.string.err_access_op_failed),context);
                        MyApp.isAccessCriteriaRead = false;
                    } else {
                        sendNotification1(Constants.ACTION_READER_STATUS_OBTAINED, command + " timeout",context);
                    }
                }
            }
        }, time);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void inventoryStartOrStop(View v) {
        Button button = (Button) v;
        captureScanData();

    }

    public void captureScanData() {
        if (isBluetoothEnabled()) {
            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected()) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
//                preferenceManager.putPreferenceIntValues(AppConstants.BUTTONCLICK,i++);
                if (!MyApp.mIsInventoryRunning) {
                    clearInventoryData();
                   /* if(button!=null)
                    button.setText("STOP");*/
//                    preferenceManager.putPreferenceIntValues(AppConstants.RFIDSCANSTATUS,0);
                    rfidListAdapter = new RfidListAdapter(MainActivity.this,new ArrayList<AssetInfo>());
                    rfidListAdapter.clear();
                    rfidListAdapter.notifyDataSetChanged();
                    //Here we send the inventory command to start reading the tags
                    if (fragment instanceof ScanAssetFragment) {
                        ((ScanAssetFragment) fragment).resetTagsInfo();
                    } else if (fragment instanceof MappingFragment) {
                        ((MappingFragment) fragment).resetTagsInfo();
                    }else if (fragment instanceof AssetAllocateFragment) {
                        ((AssetAllocateFragment) fragment).resetTagsInfo();
                    }else if (fragment instanceof LocTransferFragment) {
                        ((LocTransferFragment) fragment).resetTagsInfo();
                    }else if (fragment instanceof AssetScrapFragment) {
                        ((AssetScrapFragment) fragment).resetTagsInfo();
                    }else if (fragment instanceof AssetSoldFragment) {
                        ((AssetSoldFragment) fragment).resetTagsInfo();
                    }else {
                        if (MyApp.inventoryList != null && MyApp.inventoryList.size() > 0)
                            MyApp.inventoryList.clear();
                    }
                    //set flag value
                    isInventoryAborted = false;
                    MyApp.mIsInventoryRunning = true;
                    getTagReportingfields();

                    if (fragment instanceof ScanAssetFragment && !((ScanAssetFragment) fragment).getMemoryBankID().equalsIgnoreCase("none")) {
                        inventoryWithMemoryBank(((ScanAssetFragment) fragment).getMemoryBankID());
                    } else if (fragment instanceof MappingFragment && !((MappingFragment) fragment).getMemoryBankID().equalsIgnoreCase("none")) {
                        inventoryWithMemoryBank(((MappingFragment) fragment).getMemoryBankID());
                    }else if (fragment instanceof AssetAllocateFragment && !((AssetAllocateFragment) fragment).getMemoryBankID().equalsIgnoreCase("none")) {
                        inventoryWithMemoryBank(((AssetAllocateFragment) fragment).getMemoryBankID());
                    } else if (fragment instanceof LocTransferFragment && !((LocTransferFragment) fragment).getMemoryBankID().equalsIgnoreCase("none")) {
                        inventoryWithMemoryBank(((LocTransferFragment) fragment).getMemoryBankID());
                    }else if (fragment instanceof AssetScrapFragment && !((AssetScrapFragment) fragment).getMemoryBankID().equalsIgnoreCase("none")) {
                        inventoryWithMemoryBank(((AssetScrapFragment) fragment).getMemoryBankID());
                    }else if (fragment instanceof AssetSoldFragment && !((AssetSoldFragment) fragment).getMemoryBankID().equalsIgnoreCase("none")) {
                        inventoryWithMemoryBank(((AssetSoldFragment) fragment).getMemoryBankID());
                    }else {
                        // unique read is enabled
                        if (MyApp.reportUniquetags != null && MyApp.reportUniquetags.getValue() == 1) {
                            MyApp.mConnectedReader.Actions.purgeTags();
                        }
                        //Perform inventory
                        if (MyApp.inventoryMode == 0) {
                            try {
                                MyApp.mConnectedReader.Actions.Inventory.perform();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (final OperationFailureException e) {
                                e.printStackTrace();
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                                            if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                                                ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(e.getResults());
                                            sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, e.getVendorMessage());
                                        }
                                    });
                                }
                            }
                            if (MyApp.batchMode != -1) {
                                if (MyApp.batchMode == BATCH_MODE.ENABLE.getValue())
                                    MyApp.isBatchModeInventoryRunning = true;
                            }
                        }
                    }


                } else if (MyApp.mIsInventoryRunning) {
                    /*if(button!=null)
                    button.setText("START");*/
                    isInventoryAborted = true;
                    //Here we send the abort command to stop the inventory
                    try {
                        MyApp.mConnectedReader.Actions.Inventory.stop();
                        if (((MyApp.settings_startTrigger != null && (MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD || MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC)))
                                || (MyApp.isBatchModeInventoryRunning != null && MyApp.isBatchModeInventoryRunning))
                            operationHasAborted();
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }

                }
            } else
                Toast.makeText(MainActivity.this, getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(MainActivity.this, getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    private void inventoryWithMemoryBank(String memoryBankID) {
        if (isBluetoothEnabled()) {
            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected()) {
                TagAccess tagAccess = new TagAccess();
                TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
                //Set the param values
                readAccessParams.setCount(0);
                readAccessParams.setOffset(0);

                if ("RESERVED".equalsIgnoreCase(memoryBankID))
                    readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
                if ("EPC".equalsIgnoreCase(memoryBankID))
                    readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
                if ("TID".equalsIgnoreCase(memoryBankID))
                    readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
                if ("USER".equalsIgnoreCase(memoryBankID))
                    readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_USER);
                try {
                    //Read command with readAccessParams and accessFilter as null to read all the tags
                    MyApp.mConnectedReader.Actions.TagAccess.readEvent(readAccessParams, null, null);
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
                    if (fragment instanceof ResponseHandlerInterfaces.ResponseStatusHandler)
                        ((ResponseHandlerInterfaces.ResponseStatusHandler) fragment).handleStatusResponse(e.getResults());
                    Toast.makeText(getApplicationContext(), e.getVendorMessage(), Toast.LENGTH_SHORT).show();
                }

            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        //
        if (DisconnectTask != null)
            DisconnectTask.cancel(true);
        //disconnect from reader
        try {
            if (MyApp.mConnectedReader != null) {
                MyApp.mConnectedReader.Events.removeEventsListener(MyApp.eventHandler);
                MyApp.mConnectedReader.disconnect();
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        MyApp.mConnectedReader = null;
        //stop Timer
        Inventorytimer.getInstance().stopTimer();
        stopTimer();
        //update dpo icon in settings list
//        SettingsContent.ITEMS.get(8).icon = R.drawable.title_dpo_disabled;
        clearSettings();
        MyApp.mConnectedDevice = null;
//        Application.mConnectedReader = null;
        ReadersListFragment.readersList.clear();
        MyApp.readers.deattach(this);
        MyApp.reset();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApp.activityResumed();
    }

    @Override
    public void onPause() {
        super.onPause();
        MyApp.activityPaused();
    }

}
