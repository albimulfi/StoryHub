package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class Chapter {
    public int id;
    public String title;

    @SerializedName("chapter_number")
    public int chapterNumber;

    @SerializedName("art_id")
    public int artId;

    @SerializedName("isPremium")
    public int isPremium;

    @SerializedName("published_at")
    public String publishedAt;

    @SerializedName("isPublished")
    public String isPublished;

    @SerializedName("isi_chapter_novel")
    public String isiChapterNovel;
}