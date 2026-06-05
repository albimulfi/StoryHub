package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class ChapterList {
    public int id;

    @SerializedName("chapter_title")
    public String chapterTitle;

    @SerializedName("chapter_number")
    public int chapterNumber;

    @SerializedName("art_id")
    public int artId;

    @SerializedName("art_title")
    public String artTitle;

    @SerializedName("cover_img")
    public String coverImg;

    @SerializedName("rata2_rating_ch")
    public double rata2RatingCh;

    @SerializedName("total_reviews_ch")
    public int totalReviewsCh;

    @SerializedName("rata2_rating_user_ch")
    public double rata2RatingUserCh;

    @SerializedName("total_reviews_user_ch")
    public int totalReviewsUserCh;
}