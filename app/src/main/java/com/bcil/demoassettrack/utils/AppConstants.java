package com.bcil.demoassettrack.utils;

import android.os.Environment;

public final class AppConstants {
    public static final String AUTOSTARTSTATE = "AUTOSTARTSTATE";
    public static final String XIAOMIMANUFACTURER = "xiaomi";
    public static final String OPPOMANUFACTURER = "oppo";
    public static final String VIVOMANUFACTURER = "vivo";
    public static final String HUAWEIMANUFACTURER = "huawei";
    public static final String EMPTY = "";
    public static final String IMMEDIATE = "Immediate";
    public static final String USERNAME = "USERNAME";
    public static final String LOGINSTATUS = "LOGINSTATUS";
    public static final String SERVERURL = "SERVERURL";
    public static final String LOCATION = "LOCATION";
    public static final String DURATION = "Duration";
    public static final String PERIODIC = "Periodic";
    public static final String HANDHELD = "Handheld";
    public static final int QUIET_BEEPER = 3;
    public static final String TAG_OBSERVATION = "Tag Observation";
    public static final String N_ATTEMPTS = "N attempts";
    public static final String RFIDSCANSTATUS = "RFIDSCANSTATUS";
    public static final String BUTTONCLICK = "BUTTONCLICK";
    public static final String GETASSETIDANDDESCSTATUS = "GETASSETIDANDDESCSTATUS";
    public static final String GETIDANDDESCLIST = "GETIDANDDESCLIST";
    public static final String RFIDDATA = "RFIDDATA";
    public static  String URL;
    public static final String NOTIFICATIONS_TEXT = "notifications_text";
    public static final String NOTIFICATIONS_ID = "notifications_id";
    public static final String NFC = "NFC";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String SAMPLE_ALIAS = "MYALIAS";
    public static final String STATE = "STATE";
    public static String NAMESPACE = "http://tempuri.org/";
    public static final String SOAP_ACTION = "http://tempuri.org/";
    public static final String SERVER_FILE_PATH = Environment.getExternalStorageDirectory() + "/BCIL/Viacom/Settings/";
    public static final String ACTION_READER_DISCONNECTED = "com.rfidreader.disconnected";
    public static final String APP_SETTINGS_STATUS = "AppSettingStatus";
    public static final String AUTO_DETECT_READERS = "AutoDetectReaders";
    public static final String AUTO_RECONNECT_READERS = "AutoReconnectReaders";
    public static final String NOTIFY_READER_AVAILABLE = "NotifyReaderAvailable";
    public static final String NOTIFY_READER_CONNECTION = "NotifyReaderConnection";
    public static final String NOTIFY_BATTERY_STATUS = "NotifyBatteryStatus";
    public static final String EXPORT_DATA = "ExportData";
    public static final String MESSAGE_BATTERY_CRITICAL = "Battery level critical";
    public static final String MESSAGE_BATTERY_LOW = "Battery level low";
    public static final String ACTION_READER_BATTERY_CRITICAL = "com.rfidreader.battery.critical";
    public static final String INTENT_ACTION = "intent_action";
    public static final String INTENT_DATA = "intent_data";
    public static final String DATA_BLUETOOTH_DEVICE = "com.rfidreader.data.bluetooth.device";
    public static final String ACTION_READER_BATTERY_LOW = "com.rfidreader.battery.low";
    public static final int UNIQUE_TAG_LIMIT = 120000;
    public static final String ACTION_READER_CONNECTED = "com.rfidreader.connected";
    public static final String ACTION_READER_CONN_FAILED = "com.rfidreader.conn.failed";
    public static final String ACTION_READER_STATUS_OBTAINED = "com.rfidreader.status.received";
    public static final String SETTING_ITEM_ID = "settingItemId";
    public static final String ACTION_READER_AVAILABLE = "com.rfidreader.available";
    public static final int BATTERY_FULL = 100;
    public static final long SAVE_CONFIG_RESPONSE_TIMEOUT = 15000;
    public static final String READER_PASSWORDS = "ReadersPasswords";

}
