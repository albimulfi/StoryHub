package com.example.storyhub.adapters;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.R;
import com.example.storyhub.models.Chapter;

import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {

    private List<Chapter> chapters;
    private OnChapterClickListener listener;
    private boolean isSubscribed;

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
    }

    public ChapterAdapter(List<Chapter> chapters, OnChapterClickListener listener, boolean isSubscribed) {
        this.chapters = chapters;
        this.listener = listener;
        this.isSubscribed = isSubscribed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        boolean isPremium = chapter.isPremium == 1;

        if (isPremium) {
            holder.txtChapterNumber.getPaint().setShader(
                    new LinearGradient(0, 0, holder.txtChapterNumber.getPaint().measureText(chapter.title),
                            holder.txtChapterNumber.getTextSize(),
                            new int[]{0xFFA855F7, 0xFF3B82F6},
                            null, Shader.TileMode.CLAMP)
            );
            holder.txtChapterNumber.setText("Chapter " + chapter.chapterNumber);
            holder.txtChapterTitle.getPaint().setShader(
                    new LinearGradient(0, 0, holder.txtChapterTitle.getPaint().measureText(chapter.title),
                            holder.txtChapterTitle.getTextSize(),
                            new int[]{0xFFA855F7, 0xFF3B82F6},
                            null, Shader.TileMode.CLAMP)
            );
            holder.txtChapterTitle.setText(chapter.title);
            holder.txtPublishedAt.setText(chapter.publishedAt != null ? chapter.publishedAt : "");
            holder.imgReadIcon.setImageResource(R.drawable.read_premium);
        } else {
            holder.txtChapterNumber.getPaint().setShader(null);
            holder.txtChapterNumber.setText("Chapter " + chapter.chapterNumber);
            holder.txtChapterTitle.getPaint().setShader(null);
            holder.txtChapterTitle.setText(chapter.title);
            holder.txtPublishedAt.setText(chapter.publishedAt != null ? chapter.publishedAt : "");
            holder.imgReadIcon.setImageResource(android.R.drawable.ic_media_play);
            holder.imgReadIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.primary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChapterClick(chapter);
        });
    }

    @Override
    public int getItemCount() {
        return chapters != null ? chapters.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtChapterNumber, txtChapterTitle, txtPublishedAt;
        ImageView imgReadIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChapterNumber = itemView.findViewById(R.id.txtChapterNumber);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
            txtPublishedAt = itemView.findViewById(R.id.txtPublishedAt);
            imgReadIcon = itemView.findViewById(R.id.imgReadIcon);
        }
    }
}