package com.example.storyhub.models;

public class UpdateProfileRequest {
    public String name;
    public String username;
    public String bio;

    public UpdateProfileRequest(String name, String username, String bio) {
        this.name = name;
        this.username = username;
        this.bio = bio;
    }
}