package com.example.storyhub.models;

import java.util.List;

public class AnilistResponse {

    public Data data;

    public class Data {
        public Page Page;
    }

    public class Page {
        public List<Media> media;
    }

    public class Media {
        public int id;
        public String titleRomaji;
        public CoverImage coverImage;
    }

    public class CoverImage {
        public String large;
    }

}