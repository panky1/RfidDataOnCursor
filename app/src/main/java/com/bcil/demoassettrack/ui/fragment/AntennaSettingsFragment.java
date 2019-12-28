package com.bcil.demoassettrack.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.ui.activity.SettingsDetailActivity;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CustomProgressDialog;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFModeTableEntry;

import java.util.ArrayList;

public class AntennaSettingsFragment extends BackPressedFragment {

    ArrayList<String> linkedProfiles = new ArrayList<>();
    private ArrayAdapter<String> linkAdapter;
    private EditText powerLevel;
    private Spinner linkProfileSpinner;
    private EditText tari;
    private int[] powerLevels;

    public AntennaSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AntennaSettingsFragment.
     */
    public static AntennaSettingsFragment newInstance() {
        return new AntennaSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_antenna_settings, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeViews();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_antenna_settings);
        actionBar.setIcon(R.drawable.dl_antn);

        if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected() && MyApp.mConnectedReader.isCapabilitiesReceived() && MyApp.rfModeTable != null) {
            powerLevels = MyApp.mConnectedReader.ReaderCapabilities.getTransmitPowerLevelValues();
            getLinkedProfiles(linkedProfiles);

            linkAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_small_font, linkedProfiles);
            linkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            linkProfileSpinner.setAdapter(linkAdapter);

            if (MyApp.antennaRfConfig != null) {
                tari.setText(String.valueOf(MyApp.antennaRfConfig.getTari()));
                powerLevel.setText(String.valueOf(powerLevels[MyApp.antennaRfConfig.getTransmitPowerIndex()]));
                linkProfileSpinner.setSelection(getSelectedLinkedProfilePosition(MyApp.antennaRfConfig.getrfModeTableIndex()));
            }
        }
    }

    private int getSelectedLinkedProfilePosition(long rfModeTableIndex) {
        RFModeTableEntry rfModeTableEntry = null;
        for (int ix = 0; ix < MyApp.rfModeTable.length(); ix++) {
            rfModeTableEntry = MyApp.rfModeTable.getRFModeTableEntryInfo(ix);
            if (rfModeTableEntry.getModeIdentifer() == rfModeTableIndex)
                return ix;//linkAdapter.getPosition(rfModeTableEntry.getBdrValue()+" "+rfModeTableEntry.getModulation()+" "+rfModeTableEntry.getPieValue()+" "+rfModeTableEntry.getMaxTariValue()+" "+rfModeTableEntry.getMaxTariValue()+" "+rfModeTableEntry.getStepTariValue());
        }
        return 0;
    }

    private void getLinkedProfiles(ArrayList<String> linkedProfiles) {
        RFModeTableEntry rfModeTableEntry = null;
        for (int i = 0; i < MyApp.rfModeTable.length(); i++) {
            rfModeTableEntry = MyApp.rfModeTable.getRFModeTableEntryInfo(i);
            linkedProfiles.add(rfModeTableEntry.getBdrValue() + " " + rfModeTableEntry.getModulation() + " " + rfModeTableEntry.getPieValue() + " " + rfModeTableEntry.getMinTariValue() + " " + rfModeTableEntry.getMaxTariValue() + " " + rfModeTableEntry.getStepTariValue());
        }
    }

    private void initializeViews() {
        powerLevel = (EditText) getActivity().findViewById(R.id.powerLevel);
        tari = (EditText) getActivity().findViewById(R.id.tari);
        linkProfileSpinner = (Spinner) getActivity().findViewById(R.id.linkProfile);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onBackPressed() {
        if (!isSettingsChanged()) {
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }

    /**
     * method to know whether settings has changed
     *
     * @return true if settings has changed or false if settings has not changed
     */
    private boolean isSettingsChanged() {
        boolean isSettingsChanged = false;
        if (MyApp.antennaRfConfig != null ) {
            if ((powerLevel != null && !powerLevel.getText().toString().isEmpty()) && (tari != null && !tari.getText().toString().isEmpty())) {
                int powerLevelIndex = -1;
                try {
                    powerLevelIndex = getPowerLevelIndex(Integer.parseInt(powerLevel.getText().toString()));
                }
                catch (NumberFormatException e){
                    Toast.makeText(getActivity(), "Please enter a valid value for Power Level", Toast.LENGTH_LONG).show();
                }
                if (powerLevelIndex == -1) {
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_invalid_fields_antenna_config));
                    return false;
                }
                int linkedProfileIndex = getSelectedLinkedProfileIndex(linkProfileSpinner.getSelectedItem().toString());
                if (linkedProfileIndex == -1) {
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_invalid_fields_antenna_config));
                    return false;
                }
                int tariValue = -1;
                try {
                    tariValue = Integer.parseInt(tari.getText().toString());
                }catch (NumberFormatException e){
                    Toast.makeText(getActivity(), "Please enter a valid value for Tari Value", Toast.LENGTH_LONG).show();
                }
                if (tariValue == -1) {
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + getString(R.string.error_invalid_fields_antenna_config));
                    return false;
                }
                if (powerLevelIndex != MyApp.antennaRfConfig.getTransmitPowerIndex() || linkedProfileIndex != MyApp.antennaRfConfig.getrfModeTableIndex() || tariValue != MyApp.antennaRfConfig.getTari()) {
                    new Task_SaveAntennaConfiguration(powerLevelIndex, linkedProfileIndex, tariValue).execute();
                    isSettingsChanged = true;
                }
            }
        }
        return isSettingsChanged;
    }

    private int getSelectedLinkedProfileIndex(String linkedProfile) {
        RFModeTableEntry rfModeTableEntry = null;
        for (int i = 0; i < MyApp.rfModeTable.length(); i++) {
            rfModeTableEntry = MyApp.rfModeTable.getRFModeTableEntryInfo(i);
            if (linkedProfile.equalsIgnoreCase(rfModeTableEntry.getBdrValue() + " " + rfModeTableEntry.getModulation() + " " + rfModeTableEntry.getPieValue() + " " + rfModeTableEntry.getMinTariValue() + " " + rfModeTableEntry.getMaxTariValue() + " " + rfModeTableEntry.getStepTariValue())) {
                return rfModeTableEntry.getModeIdentifer();
            }
        }
        return -1;
    }

    private int getPowerLevelIndex(int powerLevel) {
        for (int i = 0; i < powerLevels.length; i++) {
            if (powerLevel == powerLevels[i]) {
                return i;
            }
        }
        return -1;
    }

    @SuppressLint("StaticFieldLeak")
    private class Task_SaveAntennaConfiguration extends AsyncTask<Void, Void, Boolean> {
        private CustomProgressDialog progressDialog;
        private OperationFailureException operationFailureException;
        private InvalidUsageException invalidUsageException;
        private final int powerLevel;
        private final int linkedProfile;
        private final int tari;

        public Task_SaveAntennaConfiguration(int powerLevelIndex, int linkedProfileIndex, int tariValue) {
            powerLevel = powerLevelIndex;
            linkedProfile = linkedProfileIndex;
            tari = tariValue;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.antenna_progress_title));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
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
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + invalidUsageException.getVendorMessage());
                if (operationFailureException != null)
                    ((SettingsDetailActivity) getActivity()).sendNotification(AppConstants.ACTION_READER_STATUS_OBTAINED, getString(R.string.status_failure_message) + "\n" + operationFailureException.getVendorMessage());
            }
            if (invalidUsageException == null && operationFailureException == null)
                Toast.makeText(getActivity(), R.string.status_success_message, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        }
    }

}

