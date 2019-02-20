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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.sun.mail.imap.IMAPFolder;

import java.io.File;
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
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.etPassword)
    EditText etPassword;
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
        requirePermision();
        btLogin.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AttemptLogin().execute();
            }
        });
        signIn();
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requirePermision() {
        if (!checkWriteExternalPermission())
            ActivityCompat.requestPermissions(LoginActivity.this,
                    permissions,
                    1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            File file = new File(App.APP_DIR);
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

    protected void signIn() {
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

    private void test() {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestProfile()
                        .build();
        gso.toJson();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        LoginActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
//        credential.setSelectedAccount(account.getAccount());
//        com.google.api.services.drive.Drive googleDriveService =
//                new com.google.api.services.drive.Drive.Builder(
//                        AndroidHttp.newCompatibleTransport(),
//                        new GsonFactory(),
//                        credential)
//                        .setApplicationName("AppName")
//                        .build();
//        DriveServiceHelper.init(googleDriveService);
//        mDriveServiceHelper = DriveServiceHelper.getInstance();
    }

    @SuppressLint("StaticFieldLeak")
    private class AttemptLogin extends AsyncTask<Void, Void, Boolean> {
        private String TAG = AttemptLogin.class.getSimpleName();
        String mail = etEmail.getText().toString();
        String pass = etPassword.getText().toString();
        String user = etUsername.getText().toString();

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (user.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
            LoginActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    dialog.setMessage("Đang đăng nhập");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });
            try {
                getMail(mail, pass);
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.dismiss();
//                        Toast.makeText(LoginActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                        SharedPrefs.getInstance().putEmail(mail);
                        SharedPrefs.getInstance().putPassword(pass);
                        SharedPrefs.getInstance().putAuto(true);
                        SharedPrefs.getInstance().putUsername(user);
                        SharedPrefs.getInstance().putLastTime((new Date()).getTime());
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "getMail: lỗi" + e.toString());
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Lỗi Đăng Nhập", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        }

        private void getMail(String mail, String pass) throws Exception {
            IMAPFolder folder = null;
            Store store = null;
            String subject = null;
            Flags.Flag flag = null;
            Log.e(TAG, "getMail: Bắt đầu " + (new Date()).toString());
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect("imap.googlemail.com", mail, pass);
            folder = (IMAPFolder) (store.getFolder("Inbox"));
            if (!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            Log.e(TAG, "getMail: Search xong " + (new Date()).toString());
        }
    }
}


