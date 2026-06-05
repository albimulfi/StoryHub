package com.example.storyhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.R;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.Art;
import com.example.storyhub.utils.ImageHelper;

import java.util.List;

public class MyWorksAdapter extends RecyclerView.Adapter<MyWorksAdapter.ViewHolder> {

    private Context context;
    private List<Art> artList;
    private OnItemClickListener editListener;
    private OnItemClickListener addChapterListener;

    public interface OnItemClickListener {
        void onItemClick(Art art);
    }

    public MyWorksAdapter(Context context, List<Art> artList,
                          OnItemClickListener editListener,
                          OnItemClickListener addChapterListener) {
        this.context = context;
        this.artList = artList;
        this.editListener = editListener;
        this.addChapterListener = addChapterListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_works, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Art art = artList.get(position);

        holder.txtTitle.setText(art.title);
        holder.txtStatus.setText(art.status);
        holder.txtCategory.setText(art.category);
        holder.txtChapters.setText(
                "📖 " + (art.jumlahChapter != null ? art.jumlahChapter : 0) + " Chapter");
        holder.txtPublished.setText(art.publishedAt != null ? art.publishedAt : "-");

        Glide.with(context)
                .load(ImageHelper.getImageUrl(art.coverImg))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgCover);

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onItemClick(art);
        });

        holder.btnAddChapter.setOnClickListener(v -> {
            if (addChapterListener != null) addChapterListener.onItemClick(art);
        });
    }

    @Override
    public int getItemCount() {
        return artList != null ? artList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtStatus, txtCategory, txtChapters, txtPublished;
        Button btnEdit, btnAddChapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtChapters = itemView.findViewById(R.id.txtChapters);
            txtPublished = itemView.findViewById(R.id.txtPublished);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnAddChapter = itemView.findViewById(R.id.btnAddChapter);
        }
    }
}