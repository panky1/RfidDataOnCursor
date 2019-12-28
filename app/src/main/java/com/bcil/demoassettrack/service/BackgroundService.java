package com.bcil.demoassettrack.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bcil.demoassettrack.utils.AppConstants;

import java.util.Calendar;

import static com.bcil.demoassettrack.utils.MyKeyboard.inputConnection;

public class BackgroundService extends InputMethodService {
    private static final String TAG = BackgroundService.class.getSimpleName();
    private BroadcastReceiver mReceiver;
    private String getData;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                getData = intent.getStringExtra(AppConstants.RFIDDATA);
                //log our message value
                inputConnection = getCurrentInputConnection();
                inputConnection.commitText(getData, 1);
                Log.i("ONRECEIVE:", getData);

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
    }






    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(BackgroundService.class.getSimpleName(), "onStartCommand: ");

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent myIntent = new Intent(getApplicationContext(), BackgroundService.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);

        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.SECOND, 5);

        alarmManager1.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);


        Intent myIntent = new Intent(getApplicationContext(), BackgroundService.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);

        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.SECOND, 5);

        alarmManager1.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);



    }

}
