package com.bcil.demoassettrack.app;

import android.app.Application;

import com.bcil.demoassettrack.inventory.InventoryListItem;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.utils.MaxLimitArrayList;
import com.bcil.demoassettrack.utils.MaxLimitArrayList1;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.PreFilters;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFModeTable;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RegulatoryConfig;
import com.zebra.rfid.api3.StartTrigger;
import com.zebra.rfid.api3.StopTrigger;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.UNIQUE_TAG_REPORT_SETTING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class MyApp extends Application {
    public static DYNAMIC_POWER_OPTIMIZATION dynamicPowerSettings;
    public static Readers readers;
    public static MainActivity.EventHandler eventHandler;
    public static RFIDReader mConnectedReader;
    public static short TagProximityPercent = -1;
    public static boolean isAccessCriteriaRead = false;
    public static volatile boolean mIsInventoryRunning;
    public static boolean isLocatingTag;
    public static volatile long mRRStartedTime;
    public static volatile int TAG_READ_RATE = 0;
    public static volatile int TOTAL_TAGS = 0;
    public static volatile int UNIQUE_TAGS = 0;
    public static boolean isGettingTags;
    public static boolean EXPORT_DATA;
    public static ArrayList<InventoryListItem> tagsReadInventory = new MaxLimitArrayList();
    public static ArrayList<AssetInfo> tagsReadInventory1 = new MaxLimitArrayList1();
    public static ReaderDevice mConnectedDevice;
    public static Boolean isBatchModeInventoryRunning;
    public static boolean is_connection_requested;
    public static boolean is_disconnection_requested;
    public static volatile boolean NOTIFY_READER_CONNECTION;
    public static int[] antennaPowerLevel;
    public static Antennas.AntennaRfConfig antennaRfConfig;
    public static Antennas.SingulationControl singulationControl;
    public static RFModeTable rfModeTable;
    public static RegulatoryConfig regulatory;
    public static int batchMode;
    public static TagStorageSettings tagStorageSettings;
    public static UNIQUE_TAG_REPORT_SETTING reportUniquetags = null;
    public static StartTrigger settings_startTrigger;
    public static StopTrigger settings_stopTrigger;
    public static BEEPER_VOLUME beeperVolume = null;
    public static PreFilters[] preFilters = null;
    public static HashMap<String, String> versionInfo = new HashMap<>(5);
    public static Boolean regionNotSet = false;
    public static Events.BatteryData BatteryData = null;
    public static volatile boolean AUTO_DETECT_READERS;
    public static volatile boolean AUTO_RECONNECT_READERS;
    public static volatile boolean NOTIFY_READER_AVAILABLE;
    public static volatile boolean NOTIFY_BATTERY_STATUS;
    private static boolean activityVisible;
    public static ArrayList<String> tagIDs;
    public static int memoryBankId = -1;
    public static int inventoryMode = 0;
    public static ReaderDevice mReaderDisappeared;
    public static boolean isActivityVisible() {
        return activityVisible;
    }
    public static TreeMap<String, Integer> inventoryList = new TreeMap<String, Integer>();
    public static void activityResumed() {
        activityVisible = true;
    }
    public static void activityPaused() {
        activityVisible = false;
    }
    public static volatile int INTENT_ID = 100;
    public static String accessControlTag;
    public static int preFilterIndex = -1;
    public static String locateTag;
    @Override
    public void onCreate() {
        super.onCreate();
       /* if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/
    }

    public static void updateTagIDs() {
        if (tagsReadInventory == null)
            return;

        if (tagsReadInventory.size() == 0)
            return;

        if (tagIDs == null) {
            tagIDs = new ArrayList<>();
            for (InventoryListItem i : tagsReadInventory) {
                tagIDs.add(i.getTagID());
            }
        } else if (tagIDs.size() != tagsReadInventory.size()) {
            tagIDs.clear();
            for (InventoryListItem i : tagsReadInventory) {
                tagIDs.add(i.getTagID());
            }
        }/*else{
            //Do Nothing. Array is up to date
        }*/
    }

    //clear saved data
    public static void reset() {

        UNIQUE_TAGS = 0;
        TOTAL_TAGS = 0;
        TAG_READ_RATE = 0;
        mRRStartedTime = 0;

        if (tagsReadInventory != null)
            tagsReadInventory.clear();
        if (tagIDs != null)
            tagIDs.clear();

        mIsInventoryRunning = false;
        inventoryMode = 0;
        memoryBankId = -1;
        if (inventoryList != null)
            inventoryList.clear();

        mConnectedDevice = null;

        INTENT_ID = 100;
        antennaPowerLevel = null;

        //Triggers
        settings_startTrigger = null;
        settings_startTrigger = null;

        //Beeper
        beeperVolume = null;

        accessControlTag = null;
        isAccessCriteriaRead = false;

        // reader settings
        regulatory = null;
        regionNotSet = false;

        preFilters = null;
        preFilterIndex = -1;

        settings_startTrigger = null;
        settings_stopTrigger = null;

        if (versionInfo != null)
            versionInfo.clear();

        BatteryData = null;

        isLocatingTag = false;
        TagProximityPercent = -1;
        locateTag = null;
        is_disconnection_requested = false;
        is_connection_requested = false;
        readers = null;
    }

}
