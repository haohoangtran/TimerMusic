package io.github.haohoangtran.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sun.mail.imap.IMAPFolder;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.haohoangtran.music.sharepref.SharedPrefs;

public class LoginActivity extends AppCompatActivity {


    ProgressDialog dialog;
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.btnLogin)
    Button btLogin;
    private final String SUBJECT = "fwdandpop";
    String[] permisions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        permisions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!SharedPrefs.getInstance().getEmail().isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        dialog = new ProgressDialog(LoginActivity.this);
        reqirePermision();
        btLogin.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
//                String mail = etEmail.getText().toString();
//                String pass = etPassword.getText().toString();
//                SharedPrefs.getInstance().putUserName(mail);
//                SharedPrefs.getInstance().putPassword(pass);
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
                new AtempLogin().execute();
            }
        });
    }

    private void reqirePermision() {
        if (!checkWriteExternalPermission())
            ActivityCompat.requestPermissions(LoginActivity.this,
                    permisions,
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
                    reqirePermision();
                }
            }
        }
    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @SuppressLint("StaticFieldLeak")
    private class AtempLogin extends AsyncTask<Void, Void, Boolean> {
        private String TAG = AtempLogin.class.getSimpleName();

        @Override
        protected Boolean doInBackground(Void... voids) {
            LoginActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    dialog.setMessage("Đang đăng nhập");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });

            String mail = etEmail.getText().toString();
            String pass = etPassword.getText().toString();
            try {
                getMail(mail, pass);
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {

                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                        SharedPrefs.getInstance().putEmail(mail);
                        SharedPrefs.getInstance().putPassword(pass);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "getMail: lỗi" + e.toString());
                LoginActivity.this.runOnUiThread(new Runnable() {
                    public void run() {

                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();
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

