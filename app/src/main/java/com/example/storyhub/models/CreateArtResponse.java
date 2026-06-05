package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class CreateArtResponse {
    public String message;

    @SerializedName("art_id")
    public int artId;
}