package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class ReadingHistory {
    public int id;

    @SerializedName("user_id")
    public int userId;

    @SerializedName("art_id")
    public int artId;

    @SerializedName("chapter_id")
    public int chapterId;

    @SerializedName("latest_chapter_id")
    public int latestChapterId;

    @SerializedName("page_number")
    public int pageNumber;

    @SerializedName("scroll_position")
    public int scrollPosition;

    public int hours;

    @SerializedName("progress_percent")
    public double progressPercent;

    public String title;

    @SerializedName("cover_img")
    public String coverImg;

    public String category;
}