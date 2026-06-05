package com.example.storyhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.R;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ReadingHistory;
import com.example.storyhub.utils.ImageHelper;

import java.util.List;

public class ContinueReadingAdapter extends RecyclerView.Adapter<ContinueReadingAdapter.ViewHolder> {

    private Context context;
    private List<ReadingHistory> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ReadingHistory history);
    }

    public ContinueReadingAdapter(Context context, List<ReadingHistory> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_continue_reading, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReadingHistory history = list.get(position);

        holder.txtTitle.setText(history.title);
        holder.txtCategory.setText(history.category);

        int progress = (int) Math.min(history.progressPercent, 100);
        holder.progressBar.setProgress(progress);
        holder.txtProgress.setText(progress + "%");

        Glide.with(context)
                .load(ImageHelper.getImageUrl(history.coverImg))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(history);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtCategory, txtProgress;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtProgress = itemView.findViewById(R.id.txtProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}