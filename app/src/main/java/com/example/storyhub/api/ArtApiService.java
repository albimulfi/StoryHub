package com.example.storyhub.api;

import com.example.storyhub.models.ArtDetailResponse;
import com.example.storyhub.models.ArtGenreTagMoodResponse;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.BookmarkResponse;
import com.example.storyhub.models.ChapterDetailResponse;
import com.example.storyhub.models.ChapterListResponse;
import com.example.storyhub.models.ChapterPageResponse;
import com.example.storyhub.models.ChapterResponse;
import com.example.storyhub.models.CreateArtRequest;
import com.example.storyhub.models.CreateArtResponse;
import com.example.storyhub.models.CreateChapterRequest;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.ReadingHistoryRequest;
import com.example.storyhub.models.ReadingHistoryResponse;
import com.example.storyhub.models.UserListResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

public interface ArtApiService {

    @GET("arts")
    Call<ArtResponse> getArts(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("genre") String genre,
            @Query("tag") String tag,
            @Query("mood") String mood,
            @Query("urutan") String urutan,
            @Query("order") String order,
            @Query("sudahDiReview") String sudahDiReview
    );

    @GET("arts")
    Call<ArtResponse> getArtsUserLain(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("genre") String genre,
            @Query("tag") String tag,
            @Query("mood") String mood,
            @Query("urutan") String urutan,
            @Query("order") String order,
            @Query("sudahDiReview") String sudahDiReview,
            @Query("usernameUserLain") String usernameUserLain
    );

    @GET("arts/{id}")
    Call<ArtDetailResponse> getArtById(@Path("id") int id);

    @GET("arts/{id}/genres")
    Call<ArtGenreTagMoodResponse> getArtGenres(@Path("id") int artId);

    @GET("arts/{id}/tags")
    Call<ArtGenreTagMoodResponse> getArtTags(@Path("id") int artId);

    @GET("arts/{id}/moods")
    Call<ArtGenreTagMoodResponse> getArtMoods(@Path("id") int artId);

    @GET("arts")
    Call<ArtResponse> getTrending(
            @Query("limit") int limit,
            @Query("urutan") String urutan,
            @Query("order") String order
    );

    @GET("arts/{artId}/chapters")
    Call<ChapterResponse> getChapters(
            @Path("artId") int artId,
            @Query("isPublished") String isPublished
    );

    @GET("arts/{artId}/chapters/{chapterId}/pages")
    Call<ChapterPageResponse> getChapterPages(
            @Path("artId") int artId,
            @Path("chapterId") int chapterId
    );

    @GET("bookmark")
    Call<ArtResponse> getBookmarks(@Query("category") String category);

    @POST("arts/{id}/bookmark")
    Call<BookmarkResponse> addBookmarkArt(@Path("id") int artId);

    @DELETE("arts/{id}/bookmark")
    Call<BookmarkResponse> removeBookmarkArt(@Path("id") int artId);

    @POST("chapters/{id}/bookmark")
    Call<BookmarkResponse> addBookmarkChapter(@Path("id") int chapterId);

    @DELETE("chapters/{id}/bookmark")
    Call<BookmarkResponse> removeBookmarkChapter(@Path("id") int chapterId);

    @GET("arts/continue_reading")
    Call<ReadingHistoryResponse> getContinueReading();

    @GET("arts/continue_reading")
    Call<ReadingHistoryResponse> getContinueReadingUserLain(
            @Query("usernameUserLain") String usernameUserLain
    );

    @POST("arts/{artId}/chapters/{id}/reading_history")
    Call<MessageResponse> postReadingHistory(
            @Path("artId") int artId,
            @Path("id") int chapterId,
            @Body ReadingHistoryRequest request
    );

    @PUT("arts/{artId}/chapters/{id}/reading_history")
    Call<MessageResponse> updateReadingHistory(
            @Path("artId") int artId,
            @Path("id") int chapterId,
            @Body ReadingHistoryRequest request
    );

    @GET("arts/myarts")
    Call<ArtResponse> getMyArts(@Query("ispublished") String isPublished);

    @GET("arts/myarts")
    Call<ArtResponse> getUserLainArts(
            @Query("ispublished") String isPublished,
            @Query("usernameUserLain") String usernameUserLain
    );

    @GET("chapters")
    Call<ChapterListResponse> getChapterList(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("genre") String genre,
            @Query("tag") String tag,
            @Query("mood") String mood,
            @Query("urutan") String urutan,
            @Query("order") String order
    );

    @GET("novel/{artId}/chapter/{chapterNumber}")
    Call<ChapterDetailResponse> getNovelChapter(
            @Path("artId") int artId,
            @Path("chapterNumber") int chapterNumber
    );

    @GET("comic/{artId}/chapter/{chapterNumber}")
    Call<ChapterPageResponse> getComicChapter(
            @Path("artId") int artId,
            @Path("chapterNumber") int chapterNumber
    );

    @GET("arts")
    Call<ArtResponse> searchArts(
            @Query("nyari") String query,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("urutan") String sort,
            @Query("order") String order
    );

    @GET("chapters")
    Call<ChapterListResponse> searchChapters(
            @Query("nyari") String query,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("urutan") String sort,
            @Query("order") String order
    );

    @Multipart
    @POST("arts")
    Call<CreateArtResponse> createArt(
            @Part("isPublished") RequestBody isPublished,
            @Part("title") RequestBody title,
            @Part("status") RequestBody status,
            @Part("category") RequestBody category,
            @Part("tagline") RequestBody tagline,
            @Part("synopsis") RequestBody synopsis,
            @Part("genres[]") List<RequestBody> genres,
            @Part("tags[]") List<RequestBody> tags,
            @Part("moods[]") List<RequestBody> moods,
            @Part MultipartBody.Part cover_img,
            @Part MultipartBody.Part banner_img
    );

    @Multipart
    @PUT("arts/{id}")
    Call<CreateArtResponse> updateArt(
            @Path("id") int id,
            @Part("isPublished") RequestBody isPublished,
            @Part("title") RequestBody title,
            @Part("status") RequestBody status,
            @Part("category") RequestBody category,
            @Part("tagline") RequestBody tagline,
            @Part("synopsis") RequestBody synopsis,
            @Part("genres[]") List<RequestBody> genres,
            @Part("tags[]") List<RequestBody> tags,
            @Part("moods[]") List<RequestBody> moods,
            @Part MultipartBody.Part cover_img,
            @Part MultipartBody.Part banner_img
    );

    @DELETE("arts/{id}")
    Call<MessageResponse> deleteArt(@Path("id") int id);

    @POST("arts/{artId}/chapters")
    Call<MessageResponse> createChapter(
            @Path("artId") int artId,
            @Body CreateChapterRequest request
    );

    @PUT("arts/{artId}/chapters/{id}")
    Call<MessageResponse> updateChapter(
            @Path("artId") int artId,
            @Path("id") int chapterId,
            @Body CreateChapterRequest request
    );

    @DELETE("arts/{artId}/chapters/{id}")
    Call<MessageResponse> deleteChapter(
            @Path("artId") int artId,
            @Path("id") int chapterId
    );
}