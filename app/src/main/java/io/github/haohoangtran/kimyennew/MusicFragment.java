package io.github.haohoangtran.kimyennew;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicFragment extends Fragment {
    MusicAdapter musicAdapter;
    @BindView(R.id.rv_music)
    RecyclerView rvMusic;
    @BindView(R.id.sw_onoff)
    Switch swOnOff;
    private static String TAG = MusicFragment.class.toString();

    public MusicFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, view);
        swOnOff.setOnCheckedChangeListener(null);
        swOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean prev_state = SharePref.getInstance().isOn();
                SharePref.getInstance().saveOnOff(isChecked);
                if (isChecked) {
                    if (!prev_state) {
                        MusicServiceV2.initSchedule(getContext(), true);
                    }
                } else {
                    MusicServiceV2.pause();
                }
            }
        });
        musicAdapter = new MusicAdapter();
        rvMusic.setAdapter(musicAdapter);
        rvMusic.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        swOnOff.setChecked(SharePref.getInstance().isOn());
    }


    private void postAndNotifyAdapter(Handler handler, final RecyclerView recyclerView, final RecyclerView.Adapter adapter) {
        if (handler == null) {
            handler = new Handler();
        }
        Handler finalHandler = handler;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!recyclerView.isComputingLayout()) {
                    adapter.notifyDataSetChanged();
                } else {
                    postAndNotifyAdapter(finalHandler, recyclerView, adapter);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        postAndNotifyAdapter(null, rvMusic, musicAdapter);
    }

    private class MusicAdapter extends RecyclerView.Adapter<MusicViewHolder> {
        @NonNull
        @Override
        public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View itemView = layoutInflater.inflate(R.layout.music_itemview, viewGroup, false);
            return new MusicViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MusicViewHolder musicViewHolder, int i) {
            musicViewHolder.bind(DbContext.getInstance().getMusics().get(i), i);
        }

        @Override
        public int getItemCount() {
            return DbContext.getInstance().getMusics().size();
        }
    }

    private class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        RadioButton rd;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_name);
            tv.setSelected(true);
            rd = itemView.findViewById(R.id.rd_isPlaying);

        }

        public void bind(Music music, int i) {
            if (i % 2 == 1) {
                itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                itemView.setBackgroundColor(Color.parseColor("#40008577"));
            }
            String filename = music.getName().trim();
            tv.setText(filename.substring(0, filename.lastIndexOf(".")));
            rd.setChecked(music.isPlaying());
            rd.setOnCheckedChangeListener(null);
            rd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DbContext.getInstance().changePlayFile(music);
                    Log.e(TAG, "onCheckedChanged: " + music);
                    MusicServiceV2.initSchedule(getContext(), true);
                    postAndNotifyAdapter(null, rvMusic, musicAdapter);
                }
            });
        }
    }
}
