package com.bcil.demoassettrack.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.common.ResponseHandlerInterfaces;
import com.bcil.demoassettrack.custom.CustomToast;
import com.bcil.demoassettrack.custom.PasswordDialog;
import com.bcil.demoassettrack.service.NotificationsService;
import com.bcil.demoassettrack.ui.fragment.AntennaSettingsFragment;
import com.bcil.demoassettrack.ui.fragment.ApplicationSettingsFragment;
import com.bcil.demoassettrack.ui.fragment.BackPressedFragment;
import com.bcil.demoassettrack.ui.fragment.BatteryFragment;
import com.bcil.demoassettrack.ui.fragment.DPOSettingsFragment;
import com.bcil.demoassettrack.ui.fragment.ReadersListFragment;
import com.bcil.demoassettrack.ui.fragment.StartStopTriggersFragment;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CustomProgressDialog;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

public class SettingsDetailActivity extends AppCompatActivity implements ResponseHandlerInterfaces.BluetoothDeviceFoundHandler, Readers.RFIDReaderEventHandler, ResponseHandlerInterfaces.BatteryNotificationHandler {
    //Tag to identify the currently displayed fragment
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    protected CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_detail);
        // Enabling Up / Back navigation
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        startFragment(getIntent());
        MainActivity.addBluetoothDeviceFoundHandler(this);
        MainActivity.addBatteryNotificationHandler(this);
        if (MyApp.readers == null) {
            MyApp.readers = new Readers();
        }
        // attach to reader list handler
        MyApp.readers.attach(this);
    }

    /**
     * start the fragment based on intent data
     *
     * @param intent received intent from previous activity
     */
    private void startFragment(Intent intent) {
        Fragment fragment = null;
        int settingItemSelected = intent.getIntExtra(AppConstants.SETTING_ITEM_ID, 1);

        //Show the selected item
        switch (settingItemSelected) {
            case 0:
//                fragment = InventoryFragment.newInstance();
                break;
            case 1:
                fragment = ReadersListFragment.newInstance();
                break;

            case 2:
                fragment = ApplicationSettingsFragment.newInstance();
                break;

            case 3:
                fragment = AntennaSettingsFragment.newInstance();
                break;

           /* case 4:
                fragment = SingulationControlFragment.newInstance();
                break;*/

            case 4:
                fragment = StartStopTriggersFragment.newInstance();
                break;

          /*  case 6:
                fragment = TagReportingFragment.newInstance();
                break;*/

          /*  case 7:
                fragment = RegulatorySettingsFragment.newInstance();
                break;*/

            /*case 5:
                fragment = BatteryFragment.newInstance();
                break;*/

            case 5:
                fragment = DPOSettingsFragment.newInstance();
                break;

           /* case 10:
                fragment = BeeperFragment.newInstance();
                break;*/
            /*case 6:
                fragment = SaveConfigurationsFragment.newInstance();
                break;*/
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        MyApp.activityResumed();
    }

    /**
     * call back of activity,which will call before activity went to paused
     */
    @Override
    public void onPause() {
        super.onPause();
        MyApp.activityPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // deattach to reader list handler
        MyApp.readers.deattach(this);
    }

    //    @Override
//    protected void onStart() {
//        MyApp.readers.attach(this);
//        super.onStart();
//    }
//
//
//    @Override
//    protected void onStop() {
//        MyApp.readers.deattach(this);
//        super.onStop();
//    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startFragment(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        if (findViewById(android.R.id.home) != null)
            findViewById(android.R.id.home).setPadding(0, 0, 20, 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
            //return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //We are handling back pressed for saving settings(if any). Notify the appropriate fragment.
        //{@link BaseReceiverActivity # onBackPressed should be called by the fragment when the processing is done}
        //super.onBackPressed();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment != null && fragment instanceof BackPressedFragment) {
            ((BackPressedFragment) fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Method to be called from Fragments of this activity after handling the response from the reader(success / failure)
     */
    public void callBackPressed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SettingsDetailActivity.super.onBackPressed();
            }
        });
    }

    /**
     * method to stop progress dialog on timeout
     *
     * @param time    timeout of the progress dialog
     * @param d       id of progress dialog
     * @param command command that has been sent to the reader
     */
    public void timerDelayRemoveDialog(long time, final Dialog d, final String command, final boolean isPressBack) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, command + " timeout");
                    d.dismiss();
                    if (MyApp.isActivityVisible() && isPressBack)
                        callBackPressed();
                }
            }
        }, time);
    }

    /**
     * Method called when save config button is clicked
     *
     * @param v - View to be addressed
     */
    public void saveConfigClicked(View v) {
        if (MainActivity.isBluetoothEnabled()) {
            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected()) {
                progressDialog = new CustomProgressDialog(this, getString(R.string.save_config_progress_title));
                progressDialog.show();
                timerDelayRemoveDialog(AppConstants.SAVE_CONFIG_RESPONSE_TIMEOUT, progressDialog, getString(R.string.status_failure_message), false);
                new AsyncTask<Void, Void, Boolean>() {
                    private OperationFailureException operationFailureException;

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        boolean bResult = false;
                        try {
                            MyApp.mConnectedReader.Config.saveConfig();
                            bResult = true;
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                            operationFailureException = e;
                        }
                        return bResult;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        progressDialog.dismiss();
                        if (!result) {
                            Toast.makeText(getApplicationContext(), operationFailureException.getVendorMessage(), Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.status_success_message), Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void bluetoothDeviceConnected(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).bluetoothDeviceConnected(device);
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceConnected();
        } /*if (fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof TagReportingFragment) {
            ((TagReportingFragment) fragment).deviceConnected();
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof AntennaSettingsFragment) {
            ((AntennaSettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof SaveConfigurationsFragment) {
            ((SaveConfigurationsFragment) fragment).deviceConnected();
        }*/
    }

    @Override
    public void bluetoothDeviceDisConnected(ReaderDevice device) {
        PasswordDialog.isDialogShowing = false;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).bluetoothDeviceDisConnected(device);
            ((ReadersListFragment) fragment).readerDisconnected(device);
        } else if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceDisconnected();
        }else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceDisconnected();
        } /*else if (fragment instanceof BeeperFragment) {
            ((BeeperFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof TagReportingFragment) {
            ((TagReportingFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof AntennaSettingsFragment) {
            ((AntennaSettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof SaveConfigurationsFragment) {
            ((SaveConfigurationsFragment) fragment).deviceDisconnected();
        }*/
    }

    @Override
    public void bluetoothDeviceConnFailed(ReaderDevice device) {

    }

    public void setRegion() {

    }

    public void sendNotification(String action, String data) {
        if (MyApp.isActivityVisible()) {
            if (action.equalsIgnoreCase(AppConstants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(AppConstants.ACTION_READER_BATTERY_LOW)) {
                new CustomToast(this, R.layout.toast_layout, data).show();
            } else {
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent i = new Intent(this, NotificationsService.class);
            i.putExtra(AppConstants.INTENT_ACTION, action);
            i.putExtra(AppConstants.INTENT_DATA, data);
            startService(i);
        }
    }


    /**
     * method to send connect command request to reader
     * after connect button clicked on connect password dialog
     *
     * @param password     - reader password
     * @param readerDevice
     */
    public void connectClicked(String password, ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).ConnectwithPassword(password, readerDevice);
        }
    }

    /**
     * method which will exe cute after cancel button clicked on connect pwd dialog
     *
     * @param readerDevice
     */
    public void cancelClicked(ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).readerDisconnected(readerDevice);
        }
    }

    @Override
    public void RFIDReaderAppeared(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).RFIDReaderAppeared(device);
        }
        if (MyApp.NOTIFY_READER_AVAILABLE) {
            if (!device.getName().equalsIgnoreCase("null"))
                sendNotification(AppConstants.ACTION_READER_AVAILABLE, device.getName() + " is available.");
        }
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof ReadersListFragment) {
            ((ReadersListFragment) fragment).RFIDReaderDisappeared(device);
        }
        if (MyApp.NOTIFY_READER_AVAILABLE)
            sendNotification(AppConstants.ACTION_READER_AVAILABLE, device.getName() + " is unavailable.");
    }

    @Override
    public void deviceStatusReceived(int level, boolean charging, String cause) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceStatusReceived(level, charging, cause);
        }
    }

}
