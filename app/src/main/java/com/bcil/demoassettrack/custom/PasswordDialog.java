package com.bcil.demoassettrack.custom;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.ui.activity.MainActivity;
import com.bcil.demoassettrack.ui.activity.SettingsDetailActivity;
import com.zebra.rfid.api3.ReaderDevice;

public class PasswordDialog extends Dialog implements View.OnClickListener {
    public static boolean isDialogShowing;
    private final Activity activity;
    private Button connect;
    private Button cancel;
    private EditText password;
    private ReaderDevice readerDevice;

    /**
     * Constructor of the class
     *
     * @param activity         activity context
     * @param connectingDevice
     */
    public PasswordDialog(Activity activity, ReaderDevice connectingDevice) {
        super(activity);
        this.activity = activity;
        setCancelable(false);
        readerDevice = connectingDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_password);
        connect = (Button) findViewById(R.id.btn_connect);
        cancel = (Button) findViewById(R.id.btn_cancel);
        password = (EditText) findViewById(R.id.connect_password);
        connect.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                if (!password.getText().toString().isEmpty()) {
                    if (activity.getTitle().toString().equalsIgnoreCase(activity.getString(R.string.title_activity_settings_detail)))
                        ((SettingsDetailActivity) activity).connectClicked(password.getText().toString(), readerDevice);
                    else
                        ((MainActivity) activity).connectClicked(password.getText().toString(), readerDevice);
                    dismiss();
                    isDialogShowing = false;
                }
                break;
            case R.id.btn_cancel:
                if (activity.getTitle().toString().equalsIgnoreCase(activity.getString(R.string.title_activity_settings_detail)))
                    ((SettingsDetailActivity) activity).cancelClicked(readerDevice);
                else
                    ((MainActivity) activity).cancelClicked(readerDevice);
                dismiss();
                isDialogShowing = false;
                break;
            default:
                break;
        }
    }
}
