package com.birdhouse.thanhhoang.kimyen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.birdhouse.thanhhoang.kimyen.drive.DriveServiceHelper;
import com.birdhouse.thanhhoang.kimyen.drive.GMimeType;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.sun.mail.imap.IMAPFolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private final int REQUEST_CODE_SIGN_IN = 1002;
    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;

    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;
    ProgressDialog dialog;
    @BindView(R.id.btnLogin)
    Button btLogin;
    @BindView(R.id.etUsername)
    EditText etUsername;
    String[] permissions;
    private DriveServiceHelper mDriveServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!SharedPrefs.getInstance().getEmail().isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        dialog = new ProgressDialog(LoginActivity.this);

        signInGoogleDrive();

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInApp();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Log.e("ccqq", "Sign-in failed.1 " + resultCode);
                    finish();
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Log.e("ccqq", "Sign-in failed.");
                    finish();
                }
                break;
        }
        requirePermision();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requirePermision() {
        if (!checkWriteExternalPermission())
            ActivityCompat.requestPermissions(LoginActivity.this,
                    permissions,
                    1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            java.io.File file = new java.io.File(App.APP_DIR);
            if (!file.exists()) {
                file.mkdir();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(LoginActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                    requirePermision();
                }
            }
        }
    }

    protected void signInGoogleDrive() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            initializeDriveClient(signInAccount);
        } else {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .requestScopes(Drive.SCOPE_APPFOLDER)
                            .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        Log.e("ccqq", "initializeDriveClient: ready");
    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void signInApp() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestProfile()
                        .build();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
        if (account != null) {
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            LoginActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            com.google.api.services.drive.Drive googleDriveService =
                    new com.google.api.services.drive.Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("KimYen")
                            .build();
            DriveServiceHelper.init(googleDriveService);
            mDriveServiceHelper = DriveServiceHelper.getInstance();
            File fileMetadata = new File();
            fileMetadata.setName("HaoHoangTranHiHi");
            fileMetadata.setMimeType(GMimeType.TYPE_GOOGLE_DRIVE_FOLDER);
            File file = null;
            try {
                file = mDriveServiceHelper.getmDriveService().files().create(fileMetadata)
                        .setFields("id")
                        .execute();
                Log.e("ccqq", "Folder ID: " + file.getId() + " " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Vui lòng cho phép truy cập Drive của bạn", Toast.LENGTH_SHORT).show();
        }
    }
}


