package com.bcil.demoassettrack.utils;

import android.app.Activity;

import com.bcil.demoassettrack.app.MyApp;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Inventorytimer {
    private static final int INV_UPDATE_INTERVAL = 500;
    private static Inventorytimer inventorytimer;
    private static long startedTime;
    private Activity activity;
    private Timer rrTimer;

    public static Inventorytimer getInstance() {
        if (inventorytimer == null)
            inventorytimer = new Inventorytimer();
        return inventorytimer;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void startTimer() {
        if (isTimerRunning())
            stopTimer();
        startedTime = System.currentTimeMillis();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //ReadRate = (No Of Tags Read / Inventory Duration)
                MyApp.mRRStartedTime += (System.currentTimeMillis() - startedTime);
                if (MyApp.mRRStartedTime == 0)
                    MyApp.TAG_READ_RATE = 0;
                else
                    MyApp.TAG_READ_RATE = (int) (MyApp.TOTAL_TAGS * 1000 / MyApp.mRRStartedTime);
                startedTime = System.currentTimeMillis();
                updateUI();
            }
        };
        rrTimer = new Timer();
        rrTimer.scheduleAtFixedRate(task, 0, INV_UPDATE_INTERVAL);
    }

    public void stopTimer() {
        if (rrTimer != null) {
            //Stop the timer
            rrTimer.cancel();
            rrTimer.purge();
            //ReadRate = (No Of Tags Read / Inventory Duration)
            MyApp.mRRStartedTime += (System.currentTimeMillis() - startedTime);
            if (MyApp.mRRStartedTime == 0)
                MyApp.TAG_READ_RATE = 0;
            else
                MyApp.TAG_READ_RATE = (int) (MyApp.TOTAL_TAGS * 1000 / MyApp.mRRStartedTime);
        }
        rrTimer = null;
        updateUI();
    }

    public boolean isTimerRunning() {
        if (rrTimer != null)
            return true;
        return false;
    }

    void updateUI() {
        activity.runOnUiThread(new Runnable() {
            StringBuilder min;
            StringBuilder sec;

            @Override
            public void run() {

                /*if (readRate != null) {
                    readRate.setText(MyApp.TAG_READ_RATE + AppConstants.TAGS_SEC);
                }
                if (uniqueTags != null) {
                    uniqueTags.setText(String.valueOf(MyApp.UNIQUE_TAGS));
                    if (uniqueTags.getTextScaleX() > 0.5 && uniqueTags.getText().length() > 4)
                        uniqueTags.setTextScaleX(uniqueTags.getTextScaleX() - (float) 0.1);
                    else if (uniqueTags.getTextScaleX() > 0.4 && uniqueTags.getText().length() > 5)
                        uniqueTags.setTextScaleX(uniqueTags.getTextScaleX() - (float) 0.03);
                }
                if (totalTags != null)
                    totalTags.setText(String.valueOf(MyApp.TOTAL_TAGS));
                if (timeText != null) {*/
                    long displayTime = MyApp.mRRStartedTime;
                    min = new StringBuilder(String.format("%d", TimeUnit.MILLISECONDS.toMinutes(displayTime)));
                    sec = new StringBuilder(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(displayTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(displayTime))));
                    if (min.length() == 1) {
                        min = min.insert(0, "0");
                    }
                    if (sec.length() == 1) {
                        sec = sec.insert(0, "0");
                    }
//                    timeText.setText(min + ":" + sec);
//                }
                min = null;
                sec = null;
            }
        });
    }


}

