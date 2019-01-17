package io.github.haohoangtran.music.adapters.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.haohoangtran.music.R;

public class DetailViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.tv_content)
    TextView tvContent;

    public DetailViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(String content) {
        tvContent.setText(content);
    }
}
