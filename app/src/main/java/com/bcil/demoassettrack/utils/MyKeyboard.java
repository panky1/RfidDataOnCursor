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
    public List<String> stringList;
    private Runnable runnable;
    private String getTriggerStatus;

    @Override
    public void onCreate() {
        super.onCreate();
         stringList = new ArrayList<>();


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.mReceiver);
    }


    @Override
    public View onCreateInputView() {
        scan = true;
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(mInputView, 0);
        stringList.clear();
        inputConnection = getCurrentInputConnection();

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    //extract our message from intent
                    getData = intent.getStringExtra(AppConstants.RFIDDATA);
                    getTriggerStatus = intent.getStringExtra(AppConstants.TRIGGERSTATUS);
                    //log our message value
                    if (getData != null) {
                        new MappingFragment().scanstatus = true;
                        if(getTriggerStatus!=null&&getTriggerStatus.equals("STOP")){
                            stringList.clear();
                        }else {
                            stringList.add(getData);
                        }

                        if(stringList!=null&&stringList.size()==1){
                            inputConnection.commitText(stringList.get(0)+"\n", 1);

                        }

                    }else {
                        if(getTriggerStatus!=null&&getTriggerStatus.equals("STOP"))
                            stringList.clear();
                    }



                }catch (Exception e){
                    Log.d(MyKeyboard.class.getSimpleName(),"GETEXCEPTION:"+e.toString());
                }


            }
        };
        registerReceiver(mReceiver, intentFilter);


        return createKeyboard1();
    }


    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }



    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        stringList.clear();
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

                connectBT();
                break;
            case 14:
                // Scan tag

//                tagScanButton();
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
//                tagScanButton();
                /*char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                if ((primaryCode != 13) || (primaryCode != 14) || (primaryCode != 15) || (primaryCode != 16) || (primaryCode != 16)) {
                    // ToScreen(code);
                }*/
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
    }



    /**
     * Clear scan result
     */



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













}