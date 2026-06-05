package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommentResponse {
    public List<Comment> data;

    @SerializedName("total_comments")
    public TotalComments totalComments;

    public static class TotalComments {
        public int total_comments;
    }
}