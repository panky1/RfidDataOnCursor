package com.bcil.demoassettrack.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bcil.demoassettrack.R;
import com.bcil.demoassettrack.network.DataSelections;
import com.bcil.demoassettrack.utils.AppConstants;
import com.bcil.demoassettrack.utils.CommonUtils;
import com.bcil.demoassettrack.utils.NetworkUtils;
import com.bcil.demoassettrack.utils.PreferenceManager;
import com.bcil.demoassettrack.utils.Validation;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 900;
    @Bind(R.id.etUserId)
    EditText etUserId;
    @Bind(R.id.etPassword)
    EditText etPassword;
    @Bind(R.id.btLogin)
    Button btLogin;
    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    private PreferenceManager preferenceManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        preferenceManager = new PreferenceManager(LoginActivity.this);
        preferenceManager.putPreferenceIntValues(AppConstants.LOGINSTATUS, 1);
        int getLoginStatus = preferenceManager.getPreferenceIntValues(AppConstants.LOGINSTATUS);
        etUserId.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//        filePermission();
        secureOverEditText();
        if (getLoginStatus == 1) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void secureOverEditText() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        etUserId.setFilters(new InputFilter[]{filter});
//        etPassword.setFilters(new InputFilter[]{filter});

        etUserId.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        etPassword.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        etUserId.setLongClickable(false);
        etPassword.setLongClickable(false);
    }

    private void filePermission() {
        boolean result = checkpermission();
        if (result) {
            createFile();
        }
    }

    private void createFile() {
        try {
            new File(AppConstants.SERVER_FILE_PATH);
            copySettings("Service.txt");
            String[] credentials = new CommonUtils().readSettingFile(AppConstants.SERVER_FILE_PATH, "Service.txt");
            AppConstants.URL = credentials[0];
            preferenceManager.putPreferenceValues(AppConstants.SERVERURL,credentials[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void copySettings(String settingsFile) {
        try {
            if (!new CommonUtils().checkSettingFile(AppConstants.SERVER_FILE_PATH, settingsFile)) {
                new CommonUtils().copyAssets(LoginActivity.this, settingsFile, AppConstants.SERVER_FILE_PATH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkpermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LoginActivity.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Write file permission is necessary to read info!!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_FILE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_FILE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @OnClick(R.id.btLogin)
    public void onViewClicked() {
        if (checkValidation()) {
            if (new NetworkUtils().isNetworkAvailable(LoginActivity.this)) {
                AsyncLoginTask async = new AsyncLoginTask(etUserId.getText().toString().trim().toLowerCase(), etPassword.getText().toString().trim().toLowerCase());
                async.execute("");
            } else {
                Toast.makeText(LoginActivity.this, "Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter the required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!Validation.hasText(etUserId)) ret = false;
        if (!Validation.hasText(etPassword)) ret = false;
        return ret;
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncLoginTask extends AsyncTask<Object, Object, String> {
        String username;
        String password;

        AsyncLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new CommonUtils().startProgressBarDialog(LoginActivity.this, "Please wait validating login credential...");
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                return DataSelections.checkLogin(username, password, getApplicationContext());
            } catch (Exception ex) {
                try {
                    throw ex;
                } catch (final IOException | XmlPullParserException e1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e1.toString().startsWith("java.net.ConnectException: failed to connect")) {
                                new CommonUtils().Alert(LoginActivity.this, "Alert", "Failed to connect,Please try again.");
                            } else if (e1.toString().startsWith("java.net.SocketTimeoutException: failed to connect")) {
                                new CommonUtils().Alert(LoginActivity.this, "Alert", "Failed to connect,Please try again.");
                            }

                        }
                    });
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            progressDialog.dismiss();
            if (result != null) {
                if (result.contains("User id does not exists")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new CommonUtils().Alert(LoginActivity.this, "Alert", "User id does not exists in this system.");
                        }
                    });
                    etUserId.setText(AppConstants.EMPTY);
                    etPassword.setText(AppConstants.EMPTY);
                    etUserId.requestFocus();
                } else if (result.contains("Invalid login")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new CommonUtils().Alert(LoginActivity.this, "Alert", "Invalid login credentials entered.");
                        }
                    });
                    etUserId.setText(AppConstants.EMPTY);
                    etPassword.setText(AppConstants.EMPTY);
                    etUserId.requestFocus();
                } else if (result.contains("Your account")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new CommonUtils().Alert(LoginActivity.this, "Alert", "Your account is deactivated.");
                        }
                    });
                    etUserId.setText(AppConstants.EMPTY);
                    etPassword.setText(AppConstants.EMPTY);
                    etUserId.requestFocus();
                } else {
                    preferenceManager.putPreferenceIntValues(AppConstants.LOGINSTATUS, 1);
                    preferenceManager.putPreferenceValues(AppConstants.USERNAME, etUserId.getText().toString().trim());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }

            } else {
                Toast.makeText(LoginActivity.this, "Something went wrong,please try again", Toast.LENGTH_SHORT).show();
            }

        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_FILE) {
            //noinspection StatementWithEmptyBody
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createFile();
            } else {
                Toast.makeText(this, "Please grant the permission to allocate file", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
