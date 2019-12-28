package com.bcil.demoassettrack.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    private  ProgressDialog mProgressDialog;
    public ProgressDialog startProgressBarDialog(Context context, String message) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(message);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public void stopProgressBarDialog() {
        mProgressDialog.dismiss();
    }

    public  String todayDate(String separator) {
        try {
            String day, month, year;
            day = "dd";
            month = "MM";
            year = "yyyy";
            Date dNow = new Date();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat ft = new SimpleDateFormat(day + separator + month + separator + year);
            return ft.format(dNow);
        } catch (Exception e) {
            throw e;
        }
    }

    public  String todayDateDiffFormat(String separator) {
        try {
            String day, month, year;
            day = "dd";
            month = "MM";
            year = "yyyy";
            Date dNow = new Date();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat ft = new SimpleDateFormat(year + separator + month + separator + day);
            return ft.format(dNow);
        } catch (Exception e) {
            throw e;
        }
    }

    public void Alert(Context context, String Type, String Message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(Type);
        alertDialogBuilder
                .setMessage(Message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        //MainActivity.this.finish();
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public  String[] readSettingFile(String settingFilePath, String settingFile) throws IOException {
        try {
            String[] credentials = new String[2];
            File fin = new File(settingFilePath + settingFile);
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if(i<2){
                    if(!line.equals(AppConstants.EMPTY)){
                        String[] tempLine1 = line.split("~");
                        credentials[i] = tempLine1[1];
                        i++;
                    }

//                    break;
                }

            }
            br.close();
            return credentials;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{e.toString()};
        }

    }

    public  boolean checkSettingFile(String file_path, String file_name) {
        try {
            File file = new File(file_path + "/" + file_name);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public  void copyAssets(Context context, String FileName, String folderName) {
        try {
            File settingFilePath = new File(folderName);
            if (!settingFilePath.exists()) {
                //noinspection ResultOfMethodCallIgnored
                settingFilePath.mkdirs();
            }
        } catch (Exception e) {
            Log.e("tag", "Failed to create folder.", e);
        }
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) {
            for (String filename : files) {
                if (filename.equals(FileName)) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open(filename);
                        File outFile = new File(folderName + "/", filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                    } catch (IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (out != null) {
                            //noinspection EmptyCatchBlock
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        }
    }

    //
    private  void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    public void hideKeyboardOnLeaving(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
