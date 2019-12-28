package com.bcil.demoassettrack.common;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMax implements InputFilter {
    private Long MAX_VALUE;


    public InputFilterMax() {
        MAX_VALUE = Long.parseLong("4294967295");
    }


    public InputFilterMax(Long max) {
        MAX_VALUE = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            Long input = Long.parseLong(dest.toString() + source.toString());
            if (isInRange(input))
                return null;
        } catch (NumberFormatException nfe) {
            return "";
        }
        return "";
    }

    /**
     * method to know whether entered value in edit text <= MAX_VALUE
     *
     * @param input entered value
     * @return true if value in range or false if not in range
     */
    private boolean isInRange(long input) {
        return input <= MAX_VALUE;
    }
}

