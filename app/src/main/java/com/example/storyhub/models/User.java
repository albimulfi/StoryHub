package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class User {
    public int id;
    public String username;
    public String name;
    public String email;
    public String role;
    public String bio;

    @SerializedName("profile_img")
    public String profileImg;
}