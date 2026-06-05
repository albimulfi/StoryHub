package com.example.storyhub.api;

import com.example.storyhub.models.FollowCountResponse;
import com.example.storyhub.models.FollowListResponse;
import com.example.storyhub.models.IsFollowResponse;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.SubscribeRequest;
import com.example.storyhub.models.SubscribeResponse;
import com.example.storyhub.models.UpdateProfileRequest;
import com.example.storyhub.models.UserListResponse;
import com.example.storyhub.models.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public interface UserApiService {

    @GET("users/profile")
    Call<UserResponse> getProfile();

    @GET("users/{id}/followers/count")
    Call<FollowCountResponse> getFollowerCount(@Path("id") int userId);

    @GET("users/{id}/following/count")
    Call<FollowCountResponse> getFollowingCount(@Path("id") int userId);

    @GET("users/{id}/followers")
    Call<FollowListResponse> getFollowers(@Path("id") int userId);

    @GET("users/{id}/following")
    Call<FollowListResponse> getFollowing(@Path("id") int userId);

    @POST("users/{id}/follow")
    Call<MessageResponse> followUser(@Path("id") int userId);

    @DELETE("users/{id}/follow")
    Call<MessageResponse> unfollowUser(@Path("id") int userId);

    @GET("users/{id}/isfollow")
    Call<IsFollowResponse> isFollowing(@Path("id") int userId);

    @GET("users/{id}/issubscribe")
    Call<SubscribeResponse> isSubscribe(@Path("id") int userId);

    @POST("users/subscribe")
    Call<SubscribeResponse> subscribe(@Body SubscribeRequest request);

    @GET("users/search")
    Call<UserListResponse> searchUsers(@Query("q") String query);

    @GET("users/{username}")
    Call<UserResponse> getUserByUsername(@Path("username") String username);

    @Multipart
    @PUT("users/profile")
    Call<UserResponse> updateProfileWithImage(
            @Part("name") RequestBody name,
            @Part("username") RequestBody username,
            @Part("bio") RequestBody bio,
            @Part MultipartBody.Part profile_img
    );

    @PUT("users/profile")
    Call<UserResponse> updateProfile(@Body UpdateProfileRequest request);
}