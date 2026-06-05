package com.example.storyhub.models;

import java.util.List;

public class ArtResponse {
    public List<Art> data;
    public Meta meta;

    public static class Meta {
        public int page;
        public int limit;
        public int total;

        public int total_pages;
    }
}