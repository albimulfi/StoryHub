package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Comment {
    public int id;
    public String username;

    @SerializedName("parent_comment_id")
    public Integer parentCommentId;

    @SerializedName("depth")
    public int depth;

    @SerializedName("root_comment_id")
    public Integer rootCommentId;

    @SerializedName("profile_img")
    public String profileImg;

    public String komentar;
    public String diedit;
    public String created;

    @SerializedName("total_likes")
    public int totalLikes;

    @SerializedName("total_balasan")
    public int totalBalasan;

    public boolean isLiked = false;
    public boolean showReplies = false;
    public List<Comment> replies = null;
}