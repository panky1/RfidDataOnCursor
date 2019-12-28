package com.bcil.demoassettrack.ui.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.ui.activity.SettingsDetailActivity;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CustomProgressDialog;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

public class DPOSettingsFragment extends BackPressedFragment {
    private CheckBox dynamicPower;

    public DPOSettingsFragment() {

    }

    public static DPOSettingsFragment newInstance() {
        return new DPOSettingsFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dpo, container, false);

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_dpo_settings);
        dynamicPower = (CheckBox) getActivity().findViewById(R.id.dynamicPower);


        if (MyApp.dynamicPowerSettings != null) {
            if (MyApp.dynamicPowerSettings.getValue() == 0)
                dynamicPower.setChecked(false);
            else if (MyApp.dynamicPowerSettings.getValue() == 1)
                dynamicPower.setChecked(true);
        }

    }

    @Override
    public void onBackPressed() {
        if (isSettingsChanged())
            new Task_SaveDynamicPowerSetting(dynamicPower.isChecked()).execute();
        else
            ((SettingsDetailActivity) getActivity()).callBackPressed();

    }

    public boolean isSettingsChanged() {
        if (MyApp.dynamicPowerSettings != null && !((dynamicPower.isChecked() && MyApp.dynamicPowerSettings.getValue() == 1) || (!dynamicPower.isChecked() && MyApp.dynamicPowerSettings.getValue() != 1))) {
            return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    private class Task_SaveDynamicPowerSetting extends AsyncTask<Void, Void, Boolean> {
        private final boolean enabled;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private CustomProgressDialog progressDialog;

        public Task_SaveDynamicPowerSetting(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.dynamic_power_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean bResult = true;
            try {
                if (enabled)
                    MyApp.mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
                else
                    MyApp.mConnectedReader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
                MyApp.dynamicPowerSettings = MyApp.mConnectedReader.Config.getDPOState();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                invalidUsageException = e;
                bResult = false;
            } catch (OperationFailureException e) {
                e.printStackTrace();
                operationFailureException = e;
                bResult = false;
            }
            return bResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.cancel();
            if (!result) {
                if (invalidUsageException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                if (operationFailureException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
            }
            else
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }

    public void deviceConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MyApp.dynamicPowerSettings != null) {
                    if (MyApp.dynamicPowerSettings.getValue() == 1)
                        dynamicPower.setChecked((true));
                    else if (MyApp.dynamicPowerSettings.getValue() == 0)
                        dynamicPower.setChecked((false));
                }
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dynamicPower.setChecked(false);
            }
        });
    }
   /* @Override
    public void handleStatusResponse(final Response_Status statusData) {
        String command = statusData.command.trim();
        if (command.equalsIgnoreCase(Constants.COMMAND_DYNAMICPOWER))
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (statusData.Status.trim().equalsIgnoreCase("OK")) {
                        ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_success_message));
                    } else
                        ((BaseReceiverActivity) getActivity()).sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + statusData.Status);

                    ((SettingsDetailActivity) getActivity()).callBackPressed();
                }
            });
    }*/
}

