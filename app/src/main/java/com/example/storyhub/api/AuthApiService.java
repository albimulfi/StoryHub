package com.example.storyhub.api;

import com.example.storyhub.models.AuthResponse;
import com.example.storyhub.models.LoginRequest;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<MessageResponse> register(@Body RegisterRequest request);
}