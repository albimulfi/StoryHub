package com.example.storyhub.models;

import java.util.List;

public class ArtGenreTagMoodResponse {
    public List<Item> data;

    public static class Item {
        public String genre;
        public String tag;
        public String mood;

        public String getValue() {
            if (genre != null) return genre;
            if (tag != null) return tag;
            if (mood != null) return mood;
            return "";
        }
    }
}