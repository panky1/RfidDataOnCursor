package com.bcil.demoassettrack.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;


import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.adapter.RfidListAdapter;
import com.bcil.demoassettrack.app.MyApp;
import com.bcil.demoassettrack.common.ResponseHandlerInterfaces;
import com.bcil.demoassettrack.inventory.InventoryListItem;
import com.bcil.demoassettrack.model.AssetInfo;
import com.bcil.demoassettrack.service.BackgroundService;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.ui.fragment.AssetAllocateFragment;
import com.bcil.demoassettrack.ui.fragment.AssetScrapFragment;
import com.bcil.demoassettrack.ui.fragment.AssetSoldFragment;
import com.bcil.demoassettrack.ui.fragment.LocTransferFragment;
import com.bcil.demoassettrack.ui.fragment.MappingFragment;
import com.bcil.demoassettrack.ui.fragment.ScanAssetFragment;
import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.ContentValues.TAG;
import static android.view.KeyEvent.ACTION_DOWN;
import static com.bcil.demoassettrack.ui.activity.MainActivity.UpdateReaderConnection;
import static com.bcil.demoassettrack.ui.activity.MainActivity.isBluetoothEnabled;

/**
 * Used to handle all keyboard related activities.
 *
 * @author Tian Pretorius
 * @version 1.0
 * @since 2017-03-15
 * <p>
 * Created by tianp on 24 Mar 2017.
 */

public class MyKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener/*,ResponseHandlerInterfaces.ResponseTagHandler*/ {
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Activity activity;
    private AtomicInteger mCurrentFoundNum;
    private long mCurrentFoundTime;
    private long mBelltime;
    private boolean mRunning = false;
    protected int reader_position = 0;
    private ToneGenerator mToneGenerator;
    /* protected DemoReader mReader;
     public static Vector<DemoReader> Readers;*/
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

    private Handler handler = new Handler();
    protected short maxRssi = 0;
    protected short minRssi = 0;
    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean caps = false;
    private boolean syms = false;
    private boolean scan = false;
    public static InputConnection inputConnection;
    public View mInputView;
    private boolean isInventoryAborted;
    private boolean pc;
    private boolean phase;
    private boolean channelIndex;
    private boolean rssi;
    private boolean tagSeenCount;
    private Boolean isTriggerRepeat;
    private boolean isLocationingAborted;
    private PreferenceManager preferenceManager;
    private String getData;
    private BroadcastReceiver mReceiver;
    private List<String> stringList;


    @Override
    public void onCreate() {
        super.onCreate();
         stringList = new ArrayList<>();


    }
        /*InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(mInputView, 0);
        preferenceManager = new PreferenceManager(getApplicationContext());
        inputConnection = getCurrentInputConnection();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(preferenceManager!=null){
                    Log.d(MyKeyboard.class.getSimpleName(), "Preference is not null: ");
                }else {
                    Log.d(MyKeyboard.class.getSimpleName(), "Preference is  null: ");
                }
                if(preferenceManager.getPreferenceValues(AppConstants.RFIDDATA)!=null){
                    getData = preferenceManager.getPreferenceValues(AppConstants.RFIDDATA);
                }else{
                    getData = AppConstants.EMPTY;;
                }
                if(getData==null){
                    getData = AppConstants.EMPTY;
                }
                if(inputConnection!=null){
                    Log.d(MyKeyboard.class.getSimpleName(), "Inputconnection is not null: ");
                }else {
                    Log.d(MyKeyboard.class.getSimpleName(), "Inputconnection is  null: ");
                }
                inputConnection.commitText(*//*new AssetInfo().getRfid()*//*getData, 1);
                Log.d(MyKeyboard.class.getSimpleName(), "RFIDDATAONCREATE:"+getData);
                handler.postDelayed(this, 1000);
//                preferenceManager.putPreferenceValues(AppConstants.RFIDDATA,AppConstants.EMPTY);
            }
        };
        handler.postDelayed(runnable, 1000);*/


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.mReceiver);
    }


    @Override
    public View onCreateInputView() {
        syms = false;
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(mInputView, 0);

        inputConnection = getCurrentInputConnection();

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                getData = intent.getStringExtra(AppConstants.RFIDDATA);
                //log our message value
                Log.i("ONRECEIVE:", getData);
                if (getData != null) {
                    stringList.add(getData);
                    if(stringList!=null&&stringList.size()>0){
                        inputConnection.commitText(stringList.get(0)+"\n", 1);
                    }
                    Objects.requireNonNull(stringList).clear();
                    Log.d(MyKeyboard.class.getSimpleName(), "run:"+getData);
                } else {
                    inputConnection.commitText(AppConstants.EMPTY, 1);
                }

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);

    /*
        InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputManager.restartInput(mInputView);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mInputView, InputMethodManager.SHOW_IMPLICIT);*/
       /* inputConnection = getCurrentInputConnection();
        if (getData != null) {
            stringList.add(getData);
            if(stringList!=null&&stringList.size()>0){
                inputConnection.commitText(stringList.get(0)+"\n", 1);
            }
            Objects.requireNonNull(stringList).clear();
            Log.d(MyKeyboard.class.getSimpleName(), "run:"+getData);
        } else {
            inputConnection.commitText(AppConstants.EMPTY, 1);
        }*/
        /*Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (getData != null) {
                    stringList.add(getData);
                    if(stringList!=null&&stringList.size()>0){
                        inputConnection.commitText(stringList.get(0)+"\n", 1);
                    }
                    Objects.requireNonNull(stringList).clear();
                    Log.d(MyKeyboard.class.getSimpleName(), "run:"+getData);
                } else {
                    inputConnection.commitText(AppConstants.EMPTY, 1);
                }
                Log.d(MyKeyboard.class.getSimpleName(), "RFIDDATAONKEYBOARD:" +getData);
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(runnable, 1000);*/

        Log.d(MyKeyboard.class.getSimpleName(), "onCreateInputView: ");
        return createKeyboard1();
    }


    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }


    /*@Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        *//*if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }*//*
    }*/

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        inputConnection = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                inputConnection.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                inputConnection.sendKeyEvent(new KeyEvent(ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case -113:
                // Switch to symbols layout
                syms = !syms;
                caps = false;
                scan = false;
                createKeyboard();
                break;
            case 13:
                // Scan barcode
                clearID();
                connectBT();
                break;
            case 14:
                // Scan tag
                clearID();
                tagScanButton();
                break;
            case 15:
                //Print to screen
                disconnectRFID();
                break;
            case 16:
                // Switch to scan layout
                scan = !scan;
                syms = false;
                caps = false;
                createKeyboard();
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                if ((primaryCode != 13) || (primaryCode != 14) || (primaryCode != 15) || (primaryCode != 16) || (primaryCode != 16)) {
                    // ToScreen(code);
                }
                break;
        }
    }

    /**
     * Used to choose between layouts e.g. Alphabet and Symbols
     */
    private void createKeyboard() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        if (syms) {
            keyboard = new Keyboard(this, R.xml.symbols);
        } else {
            keyboard = new Keyboard(this, R.xml.qwerty);
        }
        if (scan) {
            keyboard = new Keyboard(this, R.xml.scanner);
        }
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        mInputView = kv;
        this.setInputView(mInputView);
    }

    /**
     * Used only to set keyboard at first launch
     */
    private View createKeyboard1() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        if (syms) {
            keyboard = new Keyboard(this, R.xml.symbols);
        } else {
            keyboard = new Keyboard(this, R.xml.qwerty);
        }
        if (scan) {
            keyboard = new Keyboard(this, R.xml.scanner);
        }
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        mInputView = kv;
        return kv;
    }

    /**
     * Check if scan successful and display if true
     */
    public void disconnectRFID() {
        /*CAENRFIDReader reader = this.mReader.getReader();
        try {
            reader.InventoryAbort();
        } catch (CAENRFIDException e) {
            e.printStackTrace();
        }*/
        Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();

    }

    /**
     * Prints character to screen
     */
    public static void ToScreen(char code) {
        inputConnection.commitText(String.valueOf(code), 1);
    }

    /**
     * Keyboard sound effects
     */
    private void playClick(int keyCode) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case 10:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    /**
     * Start the barcode scanner
     */
    private void connectBT() {
      /*  PreferenceManager manager = new PreferenceManager(this);
        String dev = manager.getPreferenceValues(AppConstants.MAC);
        new BTConnector().execute(dev);
        Log.d(TAG, "connectBT: "+dev);*/
    }

    /**
     * Start the barcode scanner
     */
    private void tagScanButton() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (getData != null) {
                    stringList.add(getData);
                    if(stringList!=null&&stringList.size()>0){
                        inputConnection.commitText(stringList.get(0)+"\n", 1);
                    }
                    Objects.requireNonNull(stringList).clear();
                    Log.d(MyKeyboard.class.getSimpleName(), "run:"+getData);
                } else {
                    inputConnection.commitText(AppConstants.EMPTY, 1);
                }
                Log.d(MyKeyboard.class.getSimpleName(), "RFIDDATAONKEYBOARD:" +getData);
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(runnable, 1000);

        /*Timer t = new Timer();
//Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      if (getData != null) {
                                          stringList.add(getData);
                                          if(stringList!=null&&stringList.size()>0){
                                              inputConnection.commitText(getData, 1);
                                          }
                                          Objects.requireNonNull(stringList).clear();
                                          Log.d(MyKeyboard.class.getSimpleName(), "run:"+getData);
                                      } else {
                                          inputConnection.commitText(AppConstants.EMPTY, 1);
                                      }
                                  }

                              },
//Set how long before to start calling the TimerTask (in milliseconds)
                0,
//Set the amount of time between each execution (in milliseconds)
                1000);*/
    }

    private void captureScanData() {
        if (isBluetoothEnabled()) {
            if (MyApp.mConnectedReader != null && MyApp.mConnectedReader.isConnected()) {
//                preferenceManager.putPreferenceIntValues(AppConstants.BUTTONCLICK,i++);
                if (!MyApp.mIsInventoryRunning) {
                    clearInventoryData();


                    if (MyApp.inventoryList != null && MyApp.inventoryList.size() > 0)
                        MyApp.inventoryList.clear();

                    //Here we send the inventory command to start reading the tags
                    isInventoryAborted = false;
                    MyApp.mIsInventoryRunning = true;
                    getTagReportingfields();
                    //set flag value
                    isInventoryAborted = false;
                    MyApp.mIsInventoryRunning = true;
                    getTagReportingfields();

                    inventoryWithMemoryBank("tid");


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
                Log.e(MyKeyboard.class.getSimpleName(), getResources().getString(R.string.error_disconnected));
//                Toast.makeText(MainActivity.this, getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        } else
            Log.e(MyKeyboard.class.getSimpleName(), getResources().getString(R.string.error_bluetooth_disabled));

//            Toast.makeText(MainActivity.this, getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("StaticFieldLeak")
    private void operationHasAborted() {
        //retrieve get tags if inventory in batch mode got aborted
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
                /*getApplicationContext().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {*/
                       /* if (fragment instanceof InventoryFragment)
                            ((InventoryFragment) fragment).resetInventoryDetail();
                        else*/
//                        resetInventoryDetail();
                    /*}
                });*/
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
                    ((ResponseHandlerInterfaces.ResponseStatusHandler) MyKeyboard.this).handleStatusResponse(e.getResults());
                    Toast.makeText(getApplicationContext(), e.getVendorMessage(), Toast.LENGTH_SHORT).show();
                }

            } else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_bluetooth_disabled), Toast.LENGTH_SHORT).show();
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

    private void clearInventoryData() {
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

    private void startInventory() {
//        new InventoryTask().execute();
    }

    /**
     * Clear scan result
     */
    private void clearID() {
//        ScanResult.SetProductID("Default ID");
    }


    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {


    }

    @Override
    public void swipeUp() {

    }

    public void printLine(String hi_sainath) {
    }

    /*@SuppressLint("StaticFieldLeak")
    class InventoryTask extends AsyncTask<Object, Object, Void>
            implements CAENRFIDEventListener {

        @Override
        protected Void doInBackground(Object... args) {
            CAENRFIDReader reader = ((DemoReader) args[0]).getReader();
            CAENRFIDLogicalSource TheSource = null;
            try {
                TheSource = reader.GetSource("Source_0");
            } catch (CAENRFIDException e1) {
                e1.printStackTrace();
            }
            try {
                TheSource.SetReadCycle(0);
                reader.addCAENRFIDEventListener(this);
                if (InventoryMode.isMaskActive) {
                    if (InventoryMode.mask != null) {
                        CAENRFIDLogicalSourceConstants sel = CAENRFIDLogicalSourceConstants.EPC_C1G2_All_SELECTED;
                        switch (InventoryMode.maskMatch) {
                            case 0:
                                sel = CAENRFIDLogicalSourceConstants.EPC_C1G2_SELECTED_YES;
                                break;
                            case 1:
                                sel = CAENRFIDLogicalSourceConstants.EPC_C1G2_SELECTED_NO;
                                break;
                            case 2:
                                sel = CAENRFIDLogicalSourceConstants.EPC_C1G2_All_SELECTED;
                                break;
                        }
                        TheSource.SetSelected_EPC_C1G2(sel);
                        TheSource
                                .EventInventoryTag(
                                        InventoryMode.mask,
                                        (short) ((InventoryMode.mask.length * 8) - (InventoryMode.startMatchPosition * 8)),
                                        (short) (InventoryMode.startMatchPosition * 8),
                                        InventoryMode.isTriggerActive ? (short) 0x27
                                                : (short) 0x07);
                    }
                } else {
                    TheSource
                            .SetSelected_EPC_C1G2(CAENRFIDLogicalSourceConstants.EPC_C1G2_All_SELECTED);
                    TheSource
                            .EventInventoryTag(
                                    new byte[0],
                                    (short) 0x0,
                                    (short) 0x0,
                                    InventoryMode.isTriggerActive ? InventoryMode.isRSSIActive ? (short) 0x27
                                            : (short) 0x26
                                            : InventoryMode.isRSSIActive ? (short) 0x07
                                            : (short) 0x06);
                }

            } catch (CAENRFIDException e) {
                e.printStackTrace();
            }
            try {
                int tmp_i[] = new int[]{0, 0, 0, 0, 0};
                int tmp_cur = 0;
                mCurrentFoundTime = System.currentTimeMillis();
                while (MyKeyboard.this.isRunning()) {
                    Thread.sleep(1);
                    if ((System.currentTimeMillis() - mCurrentFoundTime) > 1000) {
                        tmp_cur = (tmp_cur - tmp_i[4]);
                        for (int i = 1; i < 5; i++) {
                            tmp_i[5 - i] = tmp_i[5 - i - 1];
                        }
                        tmp_i[0] = mCurrentFoundNum.getAndSet(0);
                        tmp_cur += tmp_i[0];
                        // mCurrentFound.post(new MyKeyboard().CurrentUpdater(String.valueOf(tmp_cur / 5)));
                        mCurrentFoundTime = System.currentTimeMillis();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reader.removeCAENRFIDEventListener(this);
            return (Void) null;
        }

        @Override
        protected void onProgressUpdate(Object... elements) {
            CAENRFIDNotify tag = (CAENRFIDNotify) elements[0];
            short tmp_rssi = tag.getRSSI();
            // controllo gli Rssi e aggiorno max e min
            if (maxRssi == INIT_RSSI_VALUE) {
                // update for first time the max and min rssi
                maxRssi = tmp_rssi;
                minRssi = tmp_rssi;
            }
            if (tmp_rssi > maxRssi)
                maxRssi = tmp_rssi;
            else if (tmp_rssi < minRssi)
                minRssi = tmp_rssi;
            Log.e(TAG, "ADDTAG:");
           *//* String getResult = toHexString(tag.getTagID());
            ScanResult.setResultdata(getResult);*//*
            ArrayList<String> list = new ArrayList<>();
            list.add(RFIDTag.toHexString(tag.getTagID()));
            Log.d(TAG, "ID TAG"+list);
            String getResult = toHexString(tag.getTagID());
            ScanResult.setResultdata(getResult);

           *//* mRFIDTagAdapter.addTag(
                    new RFIDTag(tag, Preferencess.sInventoryColor, RFIDTag
                            .toHexString(tag.getTagID()), tag.getRSSI()),
                    maxRssi, minRssi);*//*

            //mTotalFound.setText(Integer.toString(mRFIDTagAdapter.getCount()));

        }

        @Override
        protected void onPostExecute(Void unused) {
            Toast.makeText(MyKeyboard.this, "Done!", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void CAENRFIDTagNotify(CAENRFIDEvent evt) {
            mCurrentFoundNum.incrementAndGet();
            this.publishProgress((Object[]) evt.getData().toArray(new CAENRFIDNotify[0]));
            if (Preferencess.sBeepOn) {
                try {
                    if ((System.currentTimeMillis() - mBelltime) > BELL_TIME) {
                        playSound();
                        mBelltime = System.currentTimeMillis();
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

    public void playSound() throws IllegalArgumentException, SecurityException,
            IllegalStateException, IOException {
        if (mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50))
            ;
    }


    class CurrentUpdater implements Runnable {
        private String mNum;

        public CurrentUpdater(String val) {
            this.mNum = val;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: " + mNum);
        }

    }

    protected boolean isRunning() {
        return mRunning;
    }

    protected void setRunning(boolean isRunning) {
        this.mRunning = isRunning;
    }

    /*@SuppressLint("StaticFieldLeak")
    private class BTConnector extends AsyncTask<Object, Boolean, Boolean> {

        private BluetoothSocket sock;
        private CAENRFIDReaderInfo info;
        private String fwrel;

        protected Boolean doInBackground(Object... pars) {
            boolean secure = true;
            boolean no_connection = true;
            CAENRFIDReader r =null;
            while (no_connection) {
                try {
                    if (secure)
                        sock = ((BluetoothDevice) pars[0])
                                .createRfcommSocketToServiceRecord(MY_UUID);
                    else
                        sock = ((BluetoothDevice) pars[0])
                                .createInsecureRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e1) {
                    return false;
                }
                r = new CAENRFIDReader();
                try {
                    r.Connect(sock);
                    while (!CONNECTION_SUCCESFULL)
                        Thread.yield();
                    CONNECTION_SUCCESFULL = false;
                    no_connection = false;
                    int state = ((BluetoothDevice) pars[0]).getBondState();
                    while (state != BluetoothDevice.BOND_BONDED) {
                        state = ((BluetoothDevice) pars[0]).getBondState();
                    }
                } catch (CAENRFIDException e) {
                    if (!secure)
                        return false;
                    else{
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        secure = false;
                    }
                }
            }
            try {
                // r.UnblockReader();
                info = r.GetReaderInfo();
                fwrel = r.GetFirmwareRelease();

            } catch (CAENRFIDException e) {
                e.printStackTrace();
                return false;
            }
            DemoReader dr = new DemoReader(r, info.GetModel(),
                    info.GetSerialNumber(), fwrel, CAENRFIDPort.CAENRFID_BT);
            Readers.add(dr);
            return true;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(getApplicationContext(),
                        "Error during connection...", Toast.LENGTH_SHORT)
                        .show();
            }
            updateReadersList();
        }
    }

    private void updateReadersList() {
        if (Readers != null) {
            CAENRFIDPort isTCP = null;
            data.clear();

            for (int i = 0; i < Readers.size(); i++) {
                DemoReader r = Readers.get(i);

                HashMap<String, Object> readerMap = new HashMap<String, Object>();
                isTCP = r.getConnectionType();
                readerMap
                        .put("image",
                                isTCP.equals(CAENRFIDPort.CAENRFID_TCP) ? R.drawable.ic_tcp_reader
                                        : R.drawable.ic_bt_reader);
                readerMap.put("name", r.getReaderName());
                readerMap.put("info", "Serial: " + r.getSerialNumber()
                        + "\nFirmware: " + r.getFirmwareRelease()
                        + "\nRegulation: " + r.getRegulation());
                data.add(readerMap);

            }
        }
    }*/


    //dialog fragment
    @SuppressLint("ValidFragment")
    public class DeviceSelectDialogFragment extends DialogFragment {

    }

}