package com.bcil.demoassettrack.ui.fragment;

import android.support.v4.app.Fragment;

public abstract class BackPressedFragment  extends Fragment {

    /**
     * Method to be called when back button is pressed by the user
     */
    public abstract void onBackPressed();
}

