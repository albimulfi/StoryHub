package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {
    public List<Review> data;

    @SerializedName("total_reviews")
    public int totalReviews;

    public String message;
}