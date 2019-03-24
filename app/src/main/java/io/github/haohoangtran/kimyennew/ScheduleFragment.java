package io.github.haohoangtran.kimyennew;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ScheduleFragment extends Fragment {
    String TAG = this.getClass().getSimpleName();
    private OnFragmentInteractionListener mListener;
    @BindView(R.id.rv_schedule)
    RecyclerView rvSchedule;
    ScheduleAdapter adapter;
    @BindView(R.id.cb_checkall)
    CheckBox cbCheckAll;
    SparseBooleanArray itemStateArray = new SparseBooleanArray();
    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, view);
        adapter = new ScheduleAdapter();
        rvSchedule.setAdapter(adapter);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this.getContext()));
        cbCheckAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DbContext.getInstance().handleAllScheduleState(isChecked);
                MusicService.initSchedule(getContext(), false);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        cbCheckAll.setChecked(DbContext.getInstance().isCheckAllSchedule());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dataChange(DataChangeEvent event) {
        Handler handler = new Handler();
        postAndNotifyAdapter(handler, rvSchedule, adapter);
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
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {

        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            View itemView = layoutInflater.inflate(R.layout.schedule_itemview, viewGroup, false);
            Schedule schedule = DbContext.getInstance().getSchedules().get(i);
            return new ScheduleViewHolder(itemView, schedule);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder scheduleViewHolder, int i) {
            scheduleViewHolder.bind(DbContext.getInstance().getSchedules().get(i));
            if (i % 2 == 1) {
                scheduleViewHolder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                scheduleViewHolder.itemView.setBackgroundColor(Color.parseColor("#40008577"));
            }
        }

        @Override
        public int getItemCount() {
            return DbContext.getInstance().getSchedules().size();
        }
    }

    private class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        CheckBox cb;

        public ScheduleViewHolder(@NonNull View itemView, Schedule schedule) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_name);
            cb = itemView.findViewById(R.id.cb_isSelect);
            this.setIsRecyclable(false);
        }

        public void bind(Schedule schedule) {
            // use the sparse boolean array to check
            tv.setText(schedule.getTime());
            cb.setChecked(schedule.isSelect());
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    schedule.setSelect(isChecked);
                    Log.e(TAG, "onCheckedChanged: " + schedule);
                    DbContext.getInstance().insertOrUpdateSchedule(schedule);
                    Calendar now = Calendar.getInstance();
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    int minute = now.get(Calendar.MINUTE);
                    int currentMinuteOfDay = ((hour * 60) + minute);
                    if (Math.abs(schedule.getMinute() - currentMinuteOfDay) < DbContext.TIME_INTERVAL) {
                        MusicService.initSchedule(getContext(), false);
                    }
//                    cbCheckAll.setChecked(DbContext.getInstance().isCheckAllSchedule());
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
