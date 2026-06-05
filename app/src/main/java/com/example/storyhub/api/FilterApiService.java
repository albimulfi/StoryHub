package com.example.storyhub.api;

import com.example.storyhub.models.GenreResponse;
import com.example.storyhub.models.MoodResponse;
import com.example.storyhub.models.TagResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FilterApiService {

    @GET("genre")
    Call<GenreResponse> getGenres();

    @GET("tag")
    Call<TagResponse> getTags();

    @GET("mood")
    Call<MoodResponse> getMoods();
}