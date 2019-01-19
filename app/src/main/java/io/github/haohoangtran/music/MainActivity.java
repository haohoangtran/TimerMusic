package io.github.haohoangtran.music;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.haohoangtran.music.adapters.DetailAdapter;
import io.github.haohoangtran.music.adapters.SongAdapter;
import io.github.haohoangtran.music.databases.Database;
import io.github.haohoangtran.music.eventbus.ReloadData;
import io.github.haohoangtran.music.sharepref.SharedPrefs;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.rv_Music)
    RecyclerView recyclerView;
    @BindView(R.id.ibBack)
    ImageButton ibBack;
    @BindView(R.id.ibNext)
    ImageButton ibNext;
    @BindView(R.id.ibPause)
    ImageButton ibPause;
    @BindView(R.id.ibPlay)
    ImageButton ibPlay;
    @BindView(R.id.swAuto)
    Switch swAuto;
    @BindView(R.id.lnAuto)
    LinearLayout lnAuto;
    @BindView(R.id.lnManual)
    LinearLayout lnManual;

    SongAdapter songAdapter;

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        boolean isAuto = SharedPrefs.getInstance().isAuto();
        setSwitAuto(isAuto);
        swAuto.setChecked(isAuto);
        Database.getInstance().setAudio();
        songAdapter = new SongAdapter();
        recyclerView.setAdapter(songAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);

        ((TextView) view.findViewById(R.id.tv_NameHeader)).setText(SharedPrefs.getInstance().getUsername());

        ((TextView) view.findViewById(R.id.tv_mailHeader)).setText(SharedPrefs.getInstance().getEmail());
        navigationView.setNavigationItemSelectedListener(this);
        swAuto.setOnCheckedChangeListener(this);
        if (MusicService.mPlayer != null && MusicService.mPlayer.isPlaying()) {
            ibPause.setVisibility(View.VISIBLE);
            ibPlay.setVisibility(View.GONE);
        } else {
            ibPause.setVisibility(View.GONE);
            ibPlay.setVisibility(View.VISIBLE);
        }
        ibPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicService.pause();
                ibPause.setVisibility(View.GONE);
                ibPlay.setVisibility(View.VISIBLE);
            }
        });
        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicService.mPlayer != null && MusicService.mPlayer.getCurrentPosition() > 0)
                    MusicService.resume();
                ibPause.setVisibility(View.VISIBLE);
                ibPlay.setVisibility(View.GONE);
            }
        });
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    @Subscribe
    public void onReload(ReloadData data) {
        Log.e("reload", "onReload: ");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                songAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Dữ liệu được cập nhật", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void showNotification() {
        new MyNotification(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_Detail) {
            Intent intent = new Intent(this, DetailActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            SharedPrefs.getInstance().clearAll();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setSwitAuto(isChecked);
    }

    public void setSwitAuto(boolean isChecked) {
        SharedPrefs.getInstance().putAuto(isChecked);
        if (isChecked) {
            swAuto.setText("Tự động");
            lnManual.setVisibility(View.GONE);
        } else {
            swAuto.setText("Tắt");
            lnManual.setVisibility(View.VISIBLE);
        }
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }
}
