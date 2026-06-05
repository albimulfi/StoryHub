package com.example.storyhub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.R;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ChapterPage;

import java.util.List;

public class ComicPageAdapter extends RecyclerView.Adapter<ComicPageAdapter.ViewHolder> {

    private Context context;
    private List<ChapterPage> pages;

    public ComicPageAdapter(Context context, List<ChapterPage> pages) {
        this.context = context;
        this.pages = pages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comic_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterPage page = pages.get(position);

        Glide.with(context)
                .load(RetrofitClient.IMAGE_BASE_URL + page.imgChapterComic)
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPage);
    }

    @Override
    public int getItemCount() {
        return pages != null ? pages.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPage = itemView.findViewById(R.id.imgPage);
        }
    }
}