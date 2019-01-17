package io.github.haohoangtran.music.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import io.github.haohoangtran.music.R;
import io.github.haohoangtran.music.adapters.viewholders.DetailViewHolder;
import io.github.haohoangtran.music.adapters.viewholders.SongViewHolder;
import io.github.haohoangtran.music.databases.Database;

public class DetailAdapter extends RecyclerView.Adapter<DetailViewHolder> {
    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_detail, viewGroup, false);
        //2: create ViewHolder
        return new DetailViewHolder(itemView);
    }

    public DetailAdapter() {
    }


    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder detailViewHolder, int i) {
        detailViewHolder.bind(Database.getInstance().getScheduleDetails().get(i));
    }

    @Override
    public int getItemCount() {
        return Database.getInstance().getScheduleDetails().size();
    }
}
