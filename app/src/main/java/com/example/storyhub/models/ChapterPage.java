package com.example.storyhub.models;

import com.google.gson.annotations.SerializedName;

public class ChapterPage {
    public int id;

    @SerializedName("chapter_id")
    public int chapterId;

    @SerializedName("page_number")
    public int pageNumber;

    @SerializedName("img_chapter_comic")
    public String imgChapterComic;
}