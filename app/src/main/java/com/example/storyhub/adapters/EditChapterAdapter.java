package com.example.storyhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.R;
import com.example.storyhub.models.Chapter;

import java.util.List;

public class EditChapterAdapter extends RecyclerView.Adapter<EditChapterAdapter.ViewHolder> {

    private Context context;
    private List<Chapter> chapterList;
    private OnChapterClickListener editListener;
    private OnChapterClickListener deleteListener;

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
    }

    public EditChapterAdapter(Context context, List<Chapter> chapterList,
                              OnChapterClickListener editListener,
                              OnChapterClickListener deleteListener) {
        this.context = context;
        this.chapterList = chapterList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);

        holder.txtChapterNumber.setText("Chapter " + chapter.chapterNumber);
        holder.txtChapterTitle.setText(chapter.title);
        holder.txtPublished.setText(
                chapter.publishedAt != null ? chapter.publishedAt : "-");

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onChapterClick(chapter);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onChapterClick(chapter);
        });
    }

    @Override
    public int getItemCount() {
        return chapterList != null ? chapterList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtChapterNumber, txtChapterTitle, txtPublished;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChapterNumber = itemView.findViewById(R.id.txtChapterNumber);
            txtChapterTitle = itemView.findViewById(R.id.txtChapterTitle);
            txtPublished = itemView.findViewById(R.id.txtPublished);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}