package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class Art {
    public int id;
    public String title;
    public String nametag;
    public String category;
    public String status;
    public String synopsis;
    public String tagline;

    @SerializedName("isPublished")
    public String isPublished;

    @SerializedName("cover_img")
    public String coverImg;

    @SerializedName("banner_img")
    public String bannerImg;

    @SerializedName("authorOrDev")
    public String authorOrDev;

    public String artist;

    @SerializedName("published_at")
    public String publishedAt;

    @SerializedName("play_url")
    public String playUrl;

    @SerializedName("jumlah_chapter")
    public Integer jumlahChapter;

    @SerializedName("ss1_img")
    public String ss1Img;

    @SerializedName("ss2_img")
    public String ss2Img;

    @SerializedName("ss3_img")
    public String ss3Img;

    @SerializedName("rata2_rating")
    public double rata2Rating;

    @SerializedName("total_reviews")
    public int totalReviews;

    @SerializedName("rata2_rating_ch")
    public double rata2RatingCh;

    @SerializedName("total_reviews_ch")
    public int totalReviewsCh;

    @SerializedName("rata2_rating_user")
    public double rata2RatingUser;

    @SerializedName("total_reviews_user")
    public int totalReviewsUser;

    @SerializedName("rata2_rating_user_ch")
    public double rata2_rating_user_ch;

    @SerializedName("total_reviews_user_ch")
    public int total_reviews_user_ch;
}