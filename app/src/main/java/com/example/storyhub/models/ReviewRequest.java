package com.example.storyhub.models;

public class ReviewRequest {
    public int rating;
    public String komentar;

    public ReviewRequest(int rating, String komentar) {
        this.rating = rating;
        this.komentar = komentar;
    }
}