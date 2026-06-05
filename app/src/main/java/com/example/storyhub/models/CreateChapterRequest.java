package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class CreateChapterRequest {
    public String title;

    @SerializedName("isi_chapter")
    public String isiChapter;

    public String isPublished;

    public CreateChapterRequest(String title, String isiChapter, String isPublished) {
        this.title = title;
        this.isiChapter = isiChapter;
        this.isPublished = isPublished;
    }
}