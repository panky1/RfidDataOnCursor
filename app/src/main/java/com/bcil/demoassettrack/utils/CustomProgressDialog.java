package com.bcil.demoassettrack.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog extends ProgressDialog {
    private static final String MESSAGE = "Saving Settings....";

    /**
     * Constructor to handle the initialization
     *
     * @param context - Context to be used
     */
    public CustomProgressDialog(Context context, String message) {
        super(context, ProgressDialog.STYLE_SPINNER);
        setTitle(null);
        if (message != null)
            setMessage(message);
        else
            setMessage(MESSAGE);
        setCancelable(false);
    }
}
