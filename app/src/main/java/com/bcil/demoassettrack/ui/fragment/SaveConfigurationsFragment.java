package com.bcil.demoassettrack.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.utils.AppConstants;
import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFModeTableEntry;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TAG_FIELD;

import java.util.ArrayList;

public class SaveConfigurationsFragment extends Fragment {
    ArrayList<String> linkedProfiles = new ArrayList<>();
    private TextView antennaPower;
    private TextView linkProfile;
    private TextView session;
    private TextView startTrigger;
    private TextView stopTrigger;
    private TextView tagPopulation;
    private TextView invState;
    private TextView slFlag;
    private TextView saveIncPC;
    private TextView saveIncRSSI;
    private TextView saveIncPhase;
    private TextView saveIncChannel;
    private LinearLayout saveStartPeriodicLayout;
    private LinearLayout saveStartHandheldLayout;
    private TableRow saveStopTimeOutLayout;
    private LinearLayout saveStopNObserveAttemptsLayout;
    private LinearLayout saveStopTagObserveLayout;
    private LinearLayout saveStopHandheldLayout;
    private TextView saveStopTriggerReleased;
    private TextView saveStopTriggerPressed;
    private LinearLayout saveStopDurationLayout;
    private TextView saveStartTriggerReleased;
    private TextView saveStartTriggerPressed;
    private TextView saveSledBeeper;
    private TextView saveSledBeeperVolume;
    //private TextView saveRegion;
    private TextView saveIncTagSeenCount;
    private TextView savebatchMode;
    private TextView saveDPO;
    private TextView reportUniqueTags;

    public SaveConfigurationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SaveConfigurationsFragment.
     */
    public static SaveConfigurationsFragment newInstance() {
        return new SaveConfigurationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_save_configurations, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_save_configuration);
        actionBar.setIcon(R.drawable.dl_save);

        loadData();
    }

    /**
     * Method to initialize the data to be shown
     */
    private void loadData() {
        antennaPower = (TextView) getActivity().findViewById(R.id.antennaPower);
        linkProfile = (TextView) getActivity().findViewById(R.id.saveLinkProfile);

        session = (TextView) getActivity().findViewById(R.id.saveSession);
        tagPopulation = (TextView) getActivity().findViewById(R.id.tagPopulation);
        invState = (TextView) getActivity().findViewById(R.id.invState);
        slFlag = (TextView) getActivity().findViewById(R.id.saveSlFlag);

        saveIncPC = (TextView) getActivity().findViewById(R.id.saveIncPC);
        saveIncRSSI = (TextView) getActivity().findViewById(R.id.saveIncRSSI);
        saveIncPhase = (TextView) getActivity().findViewById(R.id.saveIncPhase);
        saveIncChannel = (TextView) getActivity().findViewById(R.id.saveIncChannel);
        saveIncTagSeenCount = (TextView) getActivity().findViewById(R.id.saveIncTagSeenCount);

        savebatchMode = (TextView) getActivity().findViewById(R.id.savebatchMode);

        saveDPO = (TextView) getActivity().findViewById(R.id.saveDPO);
        reportUniqueTags = (TextView) getActivity().findViewById(R.id.reportUniqueTags);
        startTrigger = (TextView) getActivity().findViewById(R.id.saveStartTrigger);
        saveStartPeriodicLayout = (LinearLayout) getActivity().findViewById(R.id.saveStartPeriodicLayout);
        saveStartHandheldLayout = (LinearLayout) getActivity().findViewById(R.id.saveStartHandheldLayout);
        stopTrigger = (TextView) getActivity().findViewById(R.id.saveStopTrigger);
        saveStopDurationLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopDurationLayout);
        saveStopHandheldLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopHandheldLayout);
        saveStopTagObserveLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopTagObserveLayout);
        saveStopNObserveAttemptsLayout = (LinearLayout) getActivity().findViewById(R.id.saveStopNObserveAttemptsLayout);
        saveStopTimeOutLayout = (TableRow) getActivity().findViewById(R.id.saveStopTimeOutLayout);

        saveSledBeeper = (TextView) getActivity().findViewById(R.id.saveSledBeeper);
        saveSledBeeperVolume = (TextView) getActivity().findViewById(R.id.saveSledBeeperVolume);

        //saveRegion = (TextView) getActivity().findViewById(R.id.saveRegion);

        //Set Anntenna settings detals
        if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected() && MyApp.mConnectedReader.isCapabilitiesReceived()) {
            try {
                MyApp.rfModeTable = MyApp.mConnectedReader.ReaderCapabilities.RFModes.getRFModeTableInfo(0);

                MyApp.antennaRfConfig = MyApp.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
                MyApp.antennaPowerLevel = MyApp.mConnectedReader.ReaderCapabilities.getTransmitPowerLevelValues();
                getLinkedProfiles(linkedProfiles);

                if (MyApp.antennaRfConfig != null) {
                    antennaPower.setText(String.valueOf(MyApp.antennaPowerLevel[MyApp.antennaRfConfig.getTransmitPowerIndex()]));
                    linkProfile.setText(linkedProfiles.get(getSelectedLinkedProfilePosition(MyApp.antennaRfConfig.getrfModeTableIndex())));
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }

        //Singulation settings detail

        if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected() && MyApp.mConnectedReader.isCapabilitiesReceived()) {
            try {
                MyApp.singulationControl = MyApp.mConnectedReader.Config.Antennas.getSingulationControl(1);
                session.setText(getResources().getStringArray(R.array.session_array)[MyApp.singulationControl.getSession().getValue()]);
                if (MyApp.singulationControl.getTagPopulation() == 30)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[0]);
                if (MyApp.singulationControl.getTagPopulation() == 100)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[1]);
                if (MyApp.singulationControl.getTagPopulation() == 200)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[2]);
                if (MyApp.singulationControl.getTagPopulation() == 300)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[3]);
                if (MyApp.singulationControl.getTagPopulation() == 400)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[4]);
                if (MyApp.singulationControl.getTagPopulation() == 500)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[5]);
                if (MyApp.singulationControl.getTagPopulation() == 600)
                    tagPopulation.setText(getResources().getStringArray(R.array.tag_population_array)[6]);

                invState.setText(getResources().getStringArray(R.array.inventory_state_array)[MyApp.singulationControl.Action.getInventoryState().getValue()]);

                switch (MyApp.singulationControl.Action.getSLFlag().getValue()) {
                    case 0:
                        slFlag.setText(getResources().getStringArray(R.array.sl_flags_array)[2]);
                        break;
                    case 1:
                        slFlag.setText(getResources().getStringArray(R.array.sl_flags_array)[1]);
                        break;
                    case 2:
                        slFlag.setText(getResources().getStringArray(R.array.sl_flags_array)[0]);
                        break;
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }

        //Tag Report settings Details
        if (MyApp.tagStorageSettings != null) {
            TAG_FIELD[] tag_field = MyApp.tagStorageSettings.getTagFields();
            for (int idx = 0; idx < tag_field.length; idx++) {
                if (tag_field[idx] == TAG_FIELD.PEAK_RSSI)
                    saveIncRSSI.setText(AppConstants.ON);
                if (tag_field[idx] == TAG_FIELD.PHASE_INFO)
                    saveIncPhase.setText(AppConstants.ON);
                if (tag_field[idx] == TAG_FIELD.PC)
                    saveIncPC.setText(AppConstants.ON);
                if (tag_field[idx] == TAG_FIELD.CHANNEL_INDEX)
                    saveIncChannel.setText(AppConstants.ON);
                if (tag_field[idx] == TAG_FIELD.TAG_SEEN_COUNT)
                    saveIncTagSeenCount.setText(AppConstants.ON);
            }
        }
        //batch mode detail
        if (MyApp.batchMode != -1) {
            savebatchMode.setText(getResources().getStringArray(R.array.batch_modes_array)[MyApp.batchMode]);
        }
        //Power management detail
        if (MyApp.dynamicPowerSettings != null) {
            if (MyApp.dynamicPowerSettings.getValue() == 1) {
                saveDPO.setText(AppConstants.ON);
            } else
                saveDPO.setText(AppConstants.OFF);
        }
        //Unique Tag Report Settings
        if (MyApp.reportUniquetags!=null){
            if(MyApp.reportUniquetags.getValue() == 1) {
                reportUniqueTags.setText(AppConstants.ON);
            } else {
                reportUniqueTags.setText(AppConstants.OFF);
            }
        }
        //Start / Stop Trgger details if (MyApp.setStartTriggerSettings != null) {
        if (MyApp.settings_startTrigger != null) {
            saveStartHandheldLayout.setVisibility(View.GONE);
            saveStartPeriodicLayout.setVisibility(View.GONE);
            if (MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE) {
                startTrigger.setText(AppConstants.IMMEDIATE);
            } else if (MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC) {
                startTrigger.setText(AppConstants.PERIODIC);
                saveStartPeriodicLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStartPeriodic)).setText(String.valueOf(MyApp.settings_startTrigger.Periodic.getPeriod()));
            } else if (MyApp.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD) {
                startTrigger.setText(AppConstants.HANDHELD);
                saveStartHandheldLayout.setVisibility(View.VISIBLE);
                saveStartTriggerPressed = (TextView) getActivity().findViewById(R.id.saveStartTriggerPressed);
                saveStartTriggerReleased = (TextView) getActivity().findViewById(R.id.saveStartTriggerReleased);
                ((TableRow) getActivity().findViewById(R.id.saveStartTriggerPressedRow)).setVisibility(View.GONE);
                ((TableRow) getActivity().findViewById(R.id.saveStartTriggerReleasedRow)).setVisibility(View.GONE);
                if (MyApp.settings_startTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    ((TableRow) getActivity().findViewById(R.id.saveStartTriggerPressedRow)).setVisibility(View.VISIBLE);
                    saveStartTriggerPressed.setText(AppConstants.ON);
                } else {
                    ((TableRow) getActivity().findViewById(R.id.saveStartTriggerReleasedRow)).setVisibility(View.VISIBLE);
                    saveStartTriggerReleased.setText(AppConstants.ON);
                }
            }
        }

        if (MyApp.settings_stopTrigger != null) {
            saveStopDurationLayout.setVisibility(View.GONE);
            saveStopHandheldLayout.setVisibility(View.GONE);
            saveStopTagObserveLayout.setVisibility(View.GONE);
            saveStopNObserveAttemptsLayout.setVisibility(View.GONE);
            saveStopTimeOutLayout.setVisibility(View.GONE);
            if (MyApp.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE) {
                stopTrigger.setText(AppConstants.IMMEDIATE);
            } else if (MyApp.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_HANDHELD_WITH_TIMEOUT) {
                stopTrigger.setText(AppConstants.HANDHELD);
                saveStopHandheldLayout.setVisibility(View.VISIBLE);
                saveStopTimeOutLayout.setVisibility(View.VISIBLE);
                saveStopTriggerPressed = (TextView) getActivity().findViewById(R.id.saveStopTriggerPressed);
                saveStopTriggerReleased = (TextView) getActivity().findViewById(R.id.saveStopTriggerReleased);
                ((TableRow) getActivity().findViewById(R.id.saveStopTriggerPressedRow)).setVisibility(View.GONE);
                ((TableRow) getActivity().findViewById(R.id.saveStopTriggerReleasedRow)).setVisibility(View.GONE);
                if (MyApp.settings_stopTrigger.Handheld.getHandheldTriggerEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    ((TableRow) getActivity().findViewById(R.id.saveStopTriggerPressedRow)).setVisibility(View.VISIBLE);
                    saveStopTriggerPressed.setText(AppConstants.ON);
                } else {
                    ((TableRow) getActivity().findViewById(R.id.saveStopTriggerReleasedRow)).setVisibility(View.VISIBLE);
                    saveStopTriggerReleased.setText(AppConstants.ON);
                }
                ((TextView) getActivity().findViewById(R.id.saveStopTimeOut)).setText(String.valueOf(MyApp.settings_stopTrigger.Handheld.getHandheldTriggerTimeout()));

            } else if (MyApp.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION) {
                stopTrigger.setText(AppConstants.DURATION);
                saveStopDurationLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStopDuration)).setText(String.valueOf(MyApp.settings_stopTrigger.getDurationMilliSeconds()));
            } else if (MyApp.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT) {
                stopTrigger.setText(AppConstants.TAG_OBSERVATION);
                saveStopTagObserveLayout.setVisibility(View.VISIBLE);
                saveStopTimeOutLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStopTagObserve)).setText(String.valueOf(MyApp.settings_stopTrigger.TagObservation.getN()));
                ((TextView) getActivity().findViewById(R.id.saveStopTimeOut)).setText(String.valueOf(MyApp.settings_stopTrigger.TagObservation.getTimeout()));
            } else if (MyApp.settings_stopTrigger.getTriggerType() == STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT) {
                stopTrigger.setText(AppConstants.N_ATTEMPTS);
                saveStopNObserveAttemptsLayout.setVisibility(View.VISIBLE);
                saveStopTimeOutLayout.setVisibility(View.VISIBLE);
                ((TextView) getActivity().findViewById(R.id.saveStopNObserveAttempts)).setText(String.valueOf(MyApp.settings_stopTrigger.NumAttempts.getN()));
                ((TextView) getActivity().findViewById(R.id.saveStopTimeOut)).setText(String.valueOf(MyApp.settings_stopTrigger.NumAttempts.getTimeout()));
            }
        }
        //Beeper settings Detail
        if (MyApp.beeperVolume != null) {
            if (MyApp.beeperVolume.equals(AppConstants.QUIET_BEEPER)) {
                saveSledBeeper.setText(AppConstants.OFF);
                saveSledBeeperVolume.setText("");
            } else if (!MyApp.beeperVolume.equals(AppConstants.QUIET_BEEPER)) {
                saveSledBeeper.setText(AppConstants.ON);
                saveSledBeeperVolume.setText(getResources().getStringArray(R.array.beeper_volume_array)[MyApp.beeperVolume.getValue()]);
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
            linkedProfiles.add(rfModeTableEntry.getBdrValue() + " " + rfModeTableEntry.getModulation() + " " + rfModeTableEntry.getPieValue() + " " + rfModeTableEntry.getMaxTariValue() + " " + rfModeTableEntry.getMaxTariValue() + " " + rfModeTableEntry.getStepTariValue());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void deviceConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    public void deviceDisconnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Set Anntenna settings detals
                antennaPower.setText("");
                linkProfile.setText("");
                //Singulation settings detail
                if (MyApp.singulationControl != null) {
                    session.setText("");
                    tagPopulation.setText("");
                    invState.setText("");
                    slFlag.setText("");
                    slFlag.setText("");
                }
                saveIncRSSI.setText(AppConstants.OFF);
                saveIncPhase.setText(AppConstants.OFF);
                saveIncPC.setText(AppConstants.OFF);
                saveIncChannel.setText(AppConstants.OFF);
                saveIncTagSeenCount.setText(AppConstants.OFF);
                reportUniqueTags.setText(AppConstants.OFF);
                savebatchMode.setText(getResources().getStringArray(R.array.batch_modes_array)[BATCH_MODE.AUTO.getValue()]);
                saveDPO.setText("");
                startTrigger.setText("");
                stopTrigger.setText("");
                saveSledBeeper.setText("");
                saveSledBeeperVolume.setText("");
            }
        });
    }

}

