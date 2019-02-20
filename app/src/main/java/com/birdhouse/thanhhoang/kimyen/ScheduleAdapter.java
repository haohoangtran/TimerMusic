package com.birdhouse.thanhhoang.kimyen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ScheduleAdapter extends BaseAdapter {
    private Context context;
//    private List<Schedule> listSchedule;

    public ScheduleAdapter(Context context, List<Schedule> listSchedule) {
        this.context = context;
//        this.listSchedule = listSchedule;
    }

    @Override
    public int getCount() {
//        return listSchedule.size();
        return Database.getInstance().getSchedules().size();
    }

    @Override
    public Object getItem(int position) {
//        return listSchedule.get(position);
        return Database.getInstance().getSchedules().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_adapter, null);
            viewHolder = new ViewHolder();
//            viewHolder.imgViewOnPlay = convertView.findViewById(R.id.imgViewOnPlay);
            viewHolder.txtViewSchedule = convertView.findViewById(R.id.txtViewSchedule);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Schedule schedule = Database.getInstance().getSchedules().get(position);
//        viewHolder.imgViewOnPlay.setEnabled(schedule.onPlay());
        viewHolder.txtViewSchedule.setText(schedule.toString());
        if (schedule.onPlay()) {
            viewHolder.txtViewSchedule.setBackgroundResource(R.color.colorGray);
        } else {
            viewHolder.txtViewSchedule.setBackgroundResource(R.color.colorAccent);
        }
        return convertView;
    }

    class ViewHolder {
        //        ImageView imgViewOnPlay;
        TextView txtViewSchedule;
    }
}
