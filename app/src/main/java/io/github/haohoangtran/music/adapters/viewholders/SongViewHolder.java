package io.github.haohoangtran.music.adapters.viewholders;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.haohoangtran.music.MusicService;
import io.github.haohoangtran.music.PlayingService;
import io.github.haohoangtran.music.R;
import io.github.haohoangtran.music.databases.Database;
import io.github.haohoangtran.music.eventbus.PlayFileEvent;
import io.github.haohoangtran.music.eventbus.ReloadData;

public class SongViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.tv_name)
    TextView tvName;
    private View itemView;
    @BindView(R.id.iv_status)
    ImageView ivStatus;

    public SongViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
    }

    public void bindView(File file) {
        tvName.setText(file.getName());
//        if (file.getAbsolutePath().equals(Database.getInstance().getPlaying().getAbsolutePath()))
        if (file==Database.getInstance().getPlaying())
            ivStatus.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        else {
            ivStatus.setImageResource(android.R.color.transparent);
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicService.playingFile(file);
            }
        });
    }
}