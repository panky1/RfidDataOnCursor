package com.bcil.demoassettrack.utils;
import android.telephony.PhoneNumberUtils;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {
    private static final String PHONE_REGEX = "^[789]\\d{9}$";

    private static final String PHONE_MSG = "invalid number";

    private static final String REQUIRED_MSG =  "cannot be empty";
    // check the input field has any text or not
    // return true if it contains text otherwise false
    public static boolean hasText(EditText editText) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        editText.setFocusable(true);
        // length 0 means there is no text
        if (text.length() == 0) {
            editText.setError(REQUIRED_MSG);
            return false;
        }

        return true;
    }

    public static boolean hasTextForTextView(TextView editText) {

        String text = editText.getText().toString().trim();
        editText.setError(null);
        editText.setFocusable(true);
        // length 0 means there is no text
        if (text.length() == 0) {
            editText.setError(REQUIRED_MSG);
            return false;
        }

        return true;
    }

    public static boolean hasActText(AutoCompleteTextView autoCompleteTextView) {

        String text = autoCompleteTextView.getText().toString().trim();
        autoCompleteTextView.setError(null);
        autoCompleteTextView.setFocusable(true);
        // length 0 means there is no text
        if (text.length() == 0) {
            autoCompleteTextView.setError(REQUIRED_MSG);
            return false;
        }

        return true;
    }

    public static boolean isPhoneNumber(EditText editText, boolean required) {
        return isValid(editText, PHONE_REGEX, PHONE_MSG, required);
    }

    private static boolean isValid(EditText editText, String regex, String errMsg, boolean required) {

        String text = editText.getText().toString().trim();
        // clearing the error, if it was previously set by some other values
        editText.setError(null);

        // text required and editText is blank, so return false
        if ( required && !hasText(editText) ) return false;
        if(regex.equalsIgnoreCase(PHONE_REGEX)){
            return PhoneNumberUtils.isGlobalPhoneNumber(editText.getText().toString().trim());
        }else {

            // pattern doesn't match so returning false
            if (required && !Pattern.matches(regex, text)) {
                editText.setError(errMsg);
                return false;
            }
        }

        return true;
    }


    public static boolean isValidMobile(String mobileno) {
        Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}");
        Matcher m = p.matcher(mobileno);
        return (m.find() && m.group().equals(mobileno));
    }

}
