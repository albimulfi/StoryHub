package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    public int id;
    public String username;

    @SerializedName("profile_img")
    public String profileImg;

    public int rating;
    public String komentar;
    public String diedit;

    @SerializedName("total_likes")
    public int totalLikes;

    @SerializedName("total_balasan")
    public int totalBalasan;

    public String created;

    public boolean isLiked = false;
    public boolean showReplies = false;
}