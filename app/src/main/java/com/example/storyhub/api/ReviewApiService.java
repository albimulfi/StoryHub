package com.example.storyhub.api;

import com.example.storyhub.models.CommentRequest;
import com.example.storyhub.models.CommentResponse;
import com.example.storyhub.models.IsLikedResponse;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.ReviewRequest;
import com.example.storyhub.models.ReviewResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReviewApiService {

    @GET("interaksi/{target_type}/{id}/reviews")
    Call<ReviewResponse> getReviews(
            @Path("target_type") String targetType,
            @Path("id") int id
    );

    @POST("interaksi/{target_type}/{id}/reviews")
    Call<MessageResponse> postReview(
            @Path("target_type") String targetType,
            @Path("id") int id,
            @Body ReviewRequest request
    );

    @PUT("interaksi/{target_type}/{id}/reviews/{reviewId}")
    Call<MessageResponse> updateReview(
            @Path("target_type") String targetType,
            @Path("id") int id,
            @Path("reviewId") int reviewId,
            @Body ReviewRequest request
    );

    @DELETE("interaksi/{target_type}/{id}/reviews/{reviewId}")
    Call<MessageResponse> deleteReview(
            @Path("target_type") String targetType,
            @Path("id") int id,
            @Path("reviewId") int reviewId
    );

    @GET("interaksi/{target_type}/{id}/comments")
    Call<CommentResponse> getComments(
            @Path("target_type") String targetType,
            @Path("id") int id
    );

    @POST("interaksi/{target_type}/{id}/comments")
    Call<MessageResponse> postComment(
            @Path("target_type") String targetType,
            @Path("id") int id,
            @Body CommentRequest request
    );

    @DELETE("interaksi/{target_type}/{id}/comments/{commentId}")
    Call<MessageResponse> deleteComment(
            @Path("target_type") String targetType,
            @Path("id") int id,
            @Path("commentId") int commentId
    );

    @POST("interaksi/{artOrChapter}/{artOrChapterId}/{target_type}/{targetId}")
    Call<MessageResponse> postLike(
            @Path("artOrChapter") String artOrChapter,
            @Path("artOrChapterId") int artOrChapterId,
            @Path("target_type") String targetType,
            @Path("targetId") int targetId
    );

    @DELETE("interaksi/{artOrChapter}/{artOrChapterId}/{target_type}/{targetId}")
    Call<MessageResponse> deleteLike(
            @Path("artOrChapter") String artOrChapter,
            @Path("artOrChapterId") int artOrChapterId,
            @Path("target_type") String targetType,
            @Path("targetId") int targetId
    );

    @GET("interaksi/{artOrChapter}/{artOrChapterId}/{target_type}/{targetId}/is-liked")
    Call<IsLikedResponse> isLiked(
            @Path("artOrChapter") String artOrChapter,
            @Path("artOrChapterId") int artOrChapterId,
            @Path("target_type") String targetType,
            @Path("targetId") int targetId
    );
}