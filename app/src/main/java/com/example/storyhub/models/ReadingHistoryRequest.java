package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class ReadingHistoryRequest {

    @SerializedName("page_number")
    public int pageNumber;

    @SerializedName("scroll_position")
    public int scrollPosition;

    public ReadingHistoryRequest(int pageNumber, int scrollPosition) {
        this.pageNumber = pageNumber;
        this.scrollPosition = scrollPosition;
    }
}