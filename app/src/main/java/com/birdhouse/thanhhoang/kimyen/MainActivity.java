package com.birdhouse.thanhhoang.kimyen;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;


import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.birdhouse.thanhhoang.kimyen.MusicService.readExcelFile;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.fileNameText)
    TextView fileNameText;
    @BindView(R.id.swAuto)
    Switch swAuto;
    @BindView(R.id.listViewSchedule)
    ListView listViewSchedule;
    @BindView(R.id.btnLogout)
    ImageView btnLogout;
    @BindView(R.id.txtViewEmail)
    TextView txtViewEmail;
    @BindView(R.id.txtViewUserName)
    TextView txtViewUserName;

    private static String TAG = MainActivity.class.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        boolean isAuto = SharedPrefs.getInstance().isAuto();
        setSwitchAuto(isAuto);
        swAuto.setChecked(isAuto);
        fileNameText.setSelected(true);
        txtViewEmail.setText(SharedPrefs.getInstance().getEmail());
        txtViewUserName.setText(SharedPrefs.getInstance().getUsername());
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefs.getInstance().clearAll();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                File f = new File(App.APP_DIR);
                if (f.exists()) {
                    f.delete();
                }
                finish();
            }
        });
        swAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSwitchAuto(isChecked);
            }
        });
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);

//        List list = readExcelFile(MainActivity.this, "Schedule.xlsx");
        readExcelFile("Schedule.xlsx");
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(this, Database.getInstance().getSchedules());
        listViewSchedule.setAdapter(scheduleAdapter);
    }

    public void setSwitchAuto(boolean isChecked) {
        if (isChecked) {
            SharedPrefs.getInstance().putAuto(true);
            fileNameText.setEnabled(true);
            fileNameText.setSelected(true);
            //resume music if played
            MusicService.resume();
            //display ListView
            for (int i = 0; i < listViewSchedule.getChildCount(); i++) {
                listViewSchedule.getChildAt(i).setEnabled(true);
            }
        } else {
            SharedPrefs.getInstance().putAuto(false);
            fileNameText.setEnabled(false);
            fileNameText.setSelected(false);
            //stop music permanently
            MusicService.pause();
            //turn off ListView
            for (int i = 0; i < listViewSchedule.getChildCount(); i++) {
                listViewSchedule.getChildAt(i).setEnabled(false);
            }
        }
    }
}
