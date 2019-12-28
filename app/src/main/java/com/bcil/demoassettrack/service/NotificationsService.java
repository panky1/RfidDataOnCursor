package com.bcil.demoassettrack.service;

import android.app.IntentService;
import android.content.Intent;

import com.bcil.demoassettrack.utils.AppConstants;

public class NotificationsService extends IntentService {

    private static int INTENT_ID = 0;

    public NotificationsService() {

        super("NotificationsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getParcelableExtra(AppConstants.DATA_BLUETOOTH_DEVICE) != null) {
                sendCustomBroadcast(intent);
            } else {
                sendCustomBroadcast(intent.getStringExtra(AppConstants.INTENT_ACTION), intent.getStringExtra(AppConstants.INTENT_DATA));
            }
        }
    }

    /**
     * Method to set the text for the notification/alert, ID for the notification etc...
     *
     * @param action   - Action to be performed
     * @param descText - Description about the intent
     */
    private void sendCustomBroadcast(String action, String descText) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra(AppConstants.NOTIFICATIONS_TEXT, descText);
        broadcast.putExtra(AppConstants.NOTIFICATIONS_ID, INTENT_ID++);
        sendOrderedBroadcast(broadcast, null);
    }

    /**
     * Method to set the text for the notification/alert, ID for the notification etc...
     *
     * @param intent - Intent containing data about broadcast
     */
    private void sendCustomBroadcast(Intent intent) {
        Intent broadcast = new Intent(intent.getAction());
        broadcast.putExtra(AppConstants.DATA_BLUETOOTH_DEVICE, intent.getParcelableExtra(AppConstants.DATA_BLUETOOTH_DEVICE));
        sendOrderedBroadcast(broadcast, null);
    }
}
