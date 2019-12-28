package com.bcil.demoassettrack.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.ui.activity.SettingsDetailActivity;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CustomProgressDialog;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

public class Task_SaveAntennaConfiguration extends AsyncTask<Void, Void, Boolean> {
    private CustomProgressDialog progressDialog;
    private OperationFailureException operationFailureException;
    private InvalidUsageException invalidUsageException;
    private final int powerLevel;
    private final int linkedProfile;
    private final int tari;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public Task_SaveAntennaConfiguration(Context context,int powerLevelIndex, int linkedProfileIndex, int tariValue) {
        this.context = context;
        powerLevel = powerLevelIndex;
        linkedProfile = linkedProfileIndex;
        tari = tariValue;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new CustomProgressDialog(context, context.getString(R.string.antenna_progress_title));
        progressDialog.show();

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Antennas.AntennaRfConfig antennaRfConfig;
        try {
            antennaRfConfig = MyApp.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
            antennaRfConfig.setTransmitPowerIndex(powerLevel);
            antennaRfConfig.setrfModeTableIndex(linkedProfile);
            antennaRfConfig.setTari(tari);
            MyApp.mConnectedReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig);
            MyApp.antennaRfConfig = antennaRfConfig;
            return true;
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            invalidUsageException = e;
        } catch (OperationFailureException e) {
            e.printStackTrace();
            operationFailureException = e;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.cancel();
        if (!result) {
            if (invalidUsageException != null)
                ((SettingsDetailActivity)context).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, context.getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
            if (operationFailureException != null)
                ((SettingsDetailActivity) context).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, context.getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
        }
        if (invalidUsageException == null && operationFailureException == null)
            Toast.makeText(context, R.string.status_success_message, Toast.LENGTH_SHORT).show();
    }
}
