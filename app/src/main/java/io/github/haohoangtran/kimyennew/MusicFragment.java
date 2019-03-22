package io.github.haohoangtran.kimyennew;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MusicFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MusicFragment} factory method to
 * create an instance of this fragment.
 */
public class MusicFragment extends Fragment {
    MusicAdapter musicAdapter;
    private OnFragmentInteractionListener mListener;
    @BindView(R.id.rv_music)
    RecyclerView rvMusic;
    @BindView(R.id.sw_onoff)
    Switch swOnOff;

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
        swOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharePref.getInstance().saveOnOff(isChecked);
                if (isChecked) {

                    MusicService.initSchedule(getContext());
                } else {
                    MusicService.stopPlayingFile();
                }
            }
        });
        musicAdapter = new MusicAdapter();
        rvMusic.setAdapter(musicAdapter);
        rvMusic.setLayoutManager(new LinearLayoutManager(this.getContext()));
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dataChange(DataChangeEvent event) {
        Handler handler = new Handler();
        postAndNotifyAdapter(handler, rvMusic, musicAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        swOnOff.setChecked(SharePref.getInstance().isOn());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    private void postAndNotifyAdapter(final Handler handler, final RecyclerView recyclerView, final RecyclerView.Adapter adapter) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!recyclerView.isComputingLayout()) {
                    adapter.notifyDataSetChanged();
                } else {
                    postAndNotifyAdapter(handler, recyclerView, adapter);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
            musicViewHolder.bind(DbContext.getInstance().getMusics().get(i));
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
            rd = itemView.findViewById(R.id.rd_isPlaying);

        }

        public void bind(Music music) {
            tv.setText(music.getName());
            rd.setChecked(music.isPlaying());
            rd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //huy cai cu chon cai moi
                    DbContext.getInstance().changePlayFile(music);
                    EventBus.getDefault().post(new DataChangeEvent());
                    if (!music.isPlaying() && isChecked) {
                        //neu dang khong chay ma dc chon
                        SharePref.getInstance().savePathRunning(music.getPath());
                        MusicService.initSchedule(getContext());
                    }
                }
            });
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
