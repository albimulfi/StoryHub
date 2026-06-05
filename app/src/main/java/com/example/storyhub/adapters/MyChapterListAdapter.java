package com.example.storyhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.R;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ChapterList;
import com.example.storyhub.utils.ImageHelper;

import java.util.List;

public class MyChapterListAdapter extends RecyclerView.Adapter<MyChapterListAdapter.ViewHolder> {

    private Context context;
    private List<ChapterList> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChapterList chapter);
    }

    public MyChapterListAdapter(Context context, List<ChapterList> list,
                                OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_chapter_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterList chapter = list.get(position);

        holder.txtChapterTitle.setText("Chapter " + chapter.chapterNumber
                + " - " + chapter.chapterTitle);
        holder.txtArtTitle.setText(chapter.artTitle);
        holder.txtRating.setText("⭐ " + String.format("%.1f", chapter.rata2RatingCh));
        holder.txtReviewers.setText("👥 " + chapter.totalReviewsCh);
        holder.txtRatingUser.setText("Ratingmu: " +
                (chapter.rata2RatingUserCh > 0 ?
                        String.format("%.1f", chapter.rata2RatingUserCh) : "N/A"));

        Glide.with(context)
                .load(ImageHelper.getImageUrl(chapter.coverImg))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(chapter);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtChapterTitle, txtArtTitle, txtRating,
                txtReviewers, txtRatingUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
            txtArtTitle = itemView.findViewById(R.id.txtArtTitle);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtReviewers = itemView.findViewById(R.id.txtReviewers);
            txtRatingUser = itemView.findViewById(R.id.txtRatingUser);
        }
    }
}