package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateArtRequest {

    @SerializedName("isPublished")
    public String isPublished;

    public String title;

    @SerializedName("cover_img")
    public String coverImg;

    @SerializedName("banner_img")
    public String bannerImg;

    public String status;
    public String category;
    public String tagline;
    public String synopsis;

    public List<String> genres;
    public List<String> tags;
    public List<String> moods;

    public CreateArtRequest(String isPublished, String title, String coverImg,
                            String bannerImg, String status, String category,
                            String tagline, String synopsis) {
        this.isPublished = isPublished;
        this.title = title;
        this.coverImg = coverImg;
        this.bannerImg = bannerImg;
        this.status = status;
        this.category = category;
        this.tagline = tagline;
        this.synopsis = synopsis;
    }
}