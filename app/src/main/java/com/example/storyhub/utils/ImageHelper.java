package com.example.storyhub.utils;

import com.example.storyhub.api.RetrofitClient;

public class ImageHelper {

    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        String url;

        if (imagePath.startsWith("http://")
                || imagePath.startsWith("https://")) {
            url = imagePath;
        } else {
            url = RetrofitClient.IMAGE_AUTH_URL + imagePath;
        }

        return url + "?t=" + System.currentTimeMillis();
    }
}