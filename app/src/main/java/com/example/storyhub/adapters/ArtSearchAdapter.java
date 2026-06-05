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
import com.example.storyhub.models.Art;
import com.example.storyhub.utils.ImageHelper;

import java.util.List;

public class ArtSearchAdapter extends RecyclerView.Adapter<ArtSearchAdapter.ViewHolder> {

    private Context context;
    private List<Art> artList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Art art);
    }

    public ArtSearchAdapter(Context context, List<Art> artList, OnItemClickListener listener) {
        this.context = context;
        this.artList = artList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_art_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Art art = artList.get(position);

        holder.txtTitle.setText(art.title);
        holder.txtCategory.setText(art.category);
        holder.txtPublished.setText(art.publishedAt != null ? art.publishedAt : "-");
        holder.txtReviewers.setText("👥 " + art.totalReviews);
        holder.txtChapters.setText("📖 " + (art.jumlahChapter != null ? art.jumlahChapter : "-"));
        holder.txtRating.setText("⭐ " + String.format("%.1f", art.rata2Rating));

        Glide.with(context)
                .load(ImageHelper.getImageUrl(art.coverImg))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(art);
        });
    }

    @Override
    public int getItemCount() {
        return artList != null ? artList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtCategory, txtPublished,
                txtReviewers, txtChapters, txtRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtPublished = itemView.findViewById(R.id.txtPublished);
            txtReviewers = itemView.findViewById(R.id.txtReviewers);
            txtChapters = itemView.findViewById(R.id.txtChapters);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}