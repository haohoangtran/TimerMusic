package io.github.haohoangtran.music.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.haohoangtran.music.R;
import io.github.haohoangtran.music.adapters.viewholders.SongViewHolder;
import io.github.haohoangtran.music.databases.Database;

public class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {
    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_song, viewGroup, false);
        //2: create ViewHolder
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder songViewHolder, int i) {
        songViewHolder.bindView(Database.getInstance().getAudio().get(i));
    }
    @Override
    public int getItemCount() {
        return Database.getInstance().getAudio().size();
    }
}
