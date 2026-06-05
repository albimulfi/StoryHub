package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.adapters.ArtAdapter;
import com.example.storyhub.adapters.ContinueReadingAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.UserApiService;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.FollowCountResponse;
import com.example.storyhub.models.IsFollowResponse;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.ReadingHistoryResponse;
import com.example.storyhub.models.SubscribeResponse;
import com.example.storyhub.models.UserResponse;
import com.example.storyhub.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    ImageView btnBack, imgProfile;
    TextView txtName, txtUsername, txtBio, txtFollowers, txtFollowing;
    Button btnFollow;

    TextView txtEmptyNovel;
    TextView txtEmptyHistory;
    TextView txtEmptyFavorite;
    TextView txtSubscribeStatus;

    LinearLayout sectionNovels;
    RecyclerView recyclerArts;

    LinearLayout layoutContinueReadingOther;
    RecyclerView recyclerContinueReadingOther;

    LinearLayout layoutFavoriteOther;
    RecyclerView recyclerFavoriteOther;

    TextView txtNotFollowing;

    String username;
    int targetUserId = -1;
    boolean isFollowing = false;
    boolean isFollower = false;

    TokenManager tokenManager;
    UserApiService userService;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tokenManager = new TokenManager(this);
        userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);

        username = getIntent().getStringExtra("username");

        initViews();
        loadUserProfile();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgProfile);
        txtName = findViewById(R.id.txtName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        btnFollow = findViewById(R.id.btnFollow);

        txtEmptyNovel = findViewById(R.id.txtEmptyNovel);
        txtEmptyHistory = findViewById(R.id.txtEmptyHistory);
        txtEmptyFavorite = findViewById(R.id.txtEmptyFavorite);
        txtSubscribeStatus = findViewById(R.id.txtSubscribeStatus);

        sectionNovels = findViewById(R.id.sectionNovels);
        recyclerArts = findViewById(R.id.recyclerArts);

        layoutContinueReadingOther = findViewById(R.id.layoutContinueReadingOther);
        recyclerContinueReadingOther = findViewById(R.id.recyclerContinueReadingOther);

        layoutFavoriteOther = findViewById(R.id.layoutFavoriteOther);
        recyclerFavoriteOther = findViewById(R.id.recyclerFavoriteOther);

        txtNotFollowing = findViewById(R.id.txtNotFollowing);

        sectionNovels.setVisibility(View.GONE);
        layoutContinueReadingOther.setVisibility(View.GONE);
        layoutFavoriteOther.setVisibility(View.GONE);
        txtNotFollowing.setVisibility(View.GONE);

        recyclerArts.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        recyclerContinueReadingOther.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        recyclerFavoriteOther.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        btnBack.setOnClickListener(v -> finish());

        txtFollowers.setOnClickListener(v -> {
            if (targetUserId == -1) return;
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("type", "followers");
            intent.putExtra("username", username);
            intent.putExtra("user_id", targetUserId);
            startActivity(intent);
        });

        txtFollowing.setOnClickListener(v -> {
            if (targetUserId == -1) return;
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("type", "following");
            intent.putExtra("username", username);
            intent.putExtra("user_id", targetUserId);
            startActivity(intent);
        });

        btnFollow.setOnClickListener(v -> toggleFollow());

        if (!tokenManager.isLoggedIn()) {
            btnFollow.setVisibility(View.GONE);
        }
    }

    private void loadUserProfile() {
        userService.getUserByUsername(username).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().user != null) {

                    var user = response.body().user;
                    targetUserId = user.id;

                    txtName.setText(user.name != null ? user.name : user.username);
                    txtUsername.setText("@" + user.username);
                    txtBio.setText(user.bio != null ? user.bio : "");

                    Glide.with(UserProfileActivity.this)
                            .load(RetrofitClient.IMAGE_AUTH_URL + user.profileImg)
                            .placeholder(R.drawable.placeholder_png)
                            .circleCrop()
                            .into(imgProfile);

                    loadFollowCounts();
                    checkTargetSubscription();

                    boolean isSelf = tokenManager.isLoggedIn()
                            && tokenManager.getUserId() == targetUserId;

                    if (isSelf) {
                        btnFollow.setVisibility(View.GONE);
                        showFollowedContent();
                    } else if (tokenManager.isLoggedIn()) {
                        checkIsFollowing();
                    } else {
                        btnFollow.setVisibility(View.GONE);
                        showNotFollowingContent();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this,
                        "Gagal load profil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowCounts() {
        userService.getFollowerCount(targetUserId).enqueue(new Callback<FollowCountResponse>() {
            @Override
            public void onResponse(Call<FollowCountResponse> call,
                                   Response<FollowCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtFollowers.setText(response.body().total_follower + "\nFollowers");
                }
            }

            @Override
            public void onFailure(Call<FollowCountResponse> call, Throwable t) {
            }
        });

        userService.getFollowingCount(targetUserId).enqueue(new Callback<FollowCountResponse>() {
            @Override
            public void onResponse(Call<FollowCountResponse> call,
                                   Response<FollowCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtFollowing.setText(response.body().total_following + "\nFollowing");
                }
            }

            @Override
            public void onFailure(Call<FollowCountResponse> call, Throwable t) {
            }
        });
    }

    private void checkIsFollowing() {
        userService.isFollowing(targetUserId).enqueue(new Callback<IsFollowResponse>() {
            @Override
            public void onResponse(Call<IsFollowResponse> call,
                                   Response<IsFollowResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFollowing = response.body().isFollowing;
                    isFollower = response.body().isFollower;

                    updateFollowButton();

                    if (isFollowing) {
                        showFollowedContent();
                    } else {
                        showNotFollowingContent();
                    }
                }
            }

            @Override
            public void onFailure(Call<IsFollowResponse> call, Throwable t) {
            }
        });
    }

    private void checkTargetSubscription() {
        userService.isSubscribe(targetUserId).enqueue(new Callback<SubscribeResponse>() {
            @Override
            public void onResponse(Call<SubscribeResponse> call,
                                   Response<SubscribeResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSubscribe) {
                    txtSubscribeStatus.setText("👑 Premium Member");
                    txtSubscribeStatus.setVisibility(View.VISIBLE);
                } else {
                    txtSubscribeStatus.setVisibility(View.GONE);
                }
            }
            @Override public void onFailure(Call<SubscribeResponse> call, Throwable t) {}
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("Unfollow");
            btnFollow.setBackgroundResource(R.drawable.chip_background);
            btnFollow.setTextColor(getResources().getColor(R.color.primary));
        } else if (isFollower) {
            btnFollow.setText("Follback");
            btnFollow.setBackgroundResource(R.drawable.button_gradient);
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        } else {
            btnFollow.setText("Follow");
            btnFollow.setBackgroundResource(R.drawable.button_gradient);
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void showFollowedContent() {
        txtNotFollowing.setVisibility(View.GONE);
        sectionNovels.setVisibility(View.VISIBLE);
        layoutContinueReadingOther.setVisibility(View.VISIBLE);
        layoutFavoriteOther.setVisibility(View.VISIBLE);

        loadUserArts();
        loadOtherUserContinueReading();
        loadOtherUserFavorite();
    }

    private void showNotFollowingContent() {
        txtNotFollowing.setVisibility(View.VISIBLE);
        sectionNovels.setVisibility(View.GONE);
        layoutContinueReadingOther.setVisibility(View.GONE);
        layoutFavoriteOther.setVisibility(View.GONE);
    }

    private void toggleFollow() {
        if (isFollowing) {
            userService.unfollowUser(targetUserId).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call,
                                       Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this,
                                "Unfollow berhasil.", Toast.LENGTH_SHORT).show();
                        isFollowing = false;
                        updateFollowButton();
                        loadFollowCounts();
                        showNotFollowingContent();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                }
            });
        } else {
            userService.followUser(targetUserId).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call,
                                       Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this,
                                "Follow berhasil!", Toast.LENGTH_SHORT).show();
                        isFollowing = true;
                        updateFollowButton();
                        loadFollowCounts();
                        showFollowedContent();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                }
            });
        }
    }

    private void loadUserArts() {
        boolean isSelf = tokenManager.isLoggedIn()
                && tokenManager.getUserId() == targetUserId;

        Call<ArtResponse> call = isSelf
                ? artService.getMyArts("published")
                : artService.getUserLainArts("published", username);

        call.enqueue(new Callback<ArtResponse>() {
            @Override
            public void onResponse(Call<ArtResponse> callR, Response<ArtResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    ArtAdapter adapter = new ArtAdapter(UserProfileActivity.this,
                            response.body().data, art -> {
                        Intent intent = new Intent(UserProfileActivity.this,
                                DetailContentActivity.class);
                        intent.putExtra("art_id", art.id);
                        startActivity(intent);
                    });
                    recyclerArts.setAdapter(adapter);
                    recyclerArts.setVisibility(View.VISIBLE);
                    txtEmptyNovel.setVisibility(View.GONE);

                } else {
                    recyclerArts.setVisibility(View.GONE);
                    txtEmptyNovel.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<ArtResponse> callR, Throwable t) {
                recyclerArts.setVisibility(View.GONE);
            }
        });
    }

    private void loadOtherUserContinueReading() {
        boolean isSelf = tokenManager.isLoggedIn()
                && tokenManager.getUserId() == targetUserId;

        Call<ReadingHistoryResponse> call = isSelf
                ? artService.getContinueReading()
                : artService.getContinueReadingUserLain(username);

        call.enqueue(new Callback<ReadingHistoryResponse>() {
            @Override
            public void onResponse(Call<ReadingHistoryResponse> callR,
                                   Response<ReadingHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    ContinueReadingAdapter adapter = new ContinueReadingAdapter(
                            UserProfileActivity.this, response.body().data, history -> {
                        Intent intent = new Intent(UserProfileActivity.this,
                                DetailContentActivity.class);
                        intent.putExtra("art_id", history.artId);
                        intent.putExtra("from_continue_reading", true);
                        intent.putExtra("latest_chapter_id", history.latestChapterId);
                        startActivity(intent);
                    });
                    recyclerContinueReadingOther.setAdapter(adapter);
                    recyclerContinueReadingOther.setVisibility(View.VISIBLE);
                    txtEmptyHistory.setVisibility(View.GONE);

                } else {
                    recyclerContinueReadingOther.setVisibility(View.GONE);
                    txtEmptyHistory.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<ReadingHistoryResponse> callR, Throwable t) {
                recyclerContinueReadingOther.setVisibility(View.GONE);
            }
        });
    }

    private void loadOtherUserFavorite() {
        boolean isSelf = tokenManager.isLoggedIn()
                && tokenManager.getUserId() == targetUserId;

        Call<ArtResponse> call = isSelf
                ? artService.getArts(1, 10, null, null, null, null,
                "ratingArtUser", "DESC", "true")
                : artService.getArtsUserLain(1, 10, null, null, null, null,
                "ratingArtUser", "DESC", "true", username);

        call.enqueue(new Callback<ArtResponse>() {
            @Override
            public void onResponse(Call<ArtResponse> callR, Response<ArtResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    ArtAdapter adapter = new ArtAdapter(UserProfileActivity.this,
                            response.body().data, art -> {
                        Intent intent = new Intent(UserProfileActivity.this,
                                DetailContentActivity.class);
                        intent.putExtra("art_id", art.id);
                        startActivity(intent);
                    });
                    recyclerFavoriteOther.setAdapter(adapter);
                    recyclerFavoriteOther.setVisibility(View.VISIBLE);
                    txtEmptyFavorite.setVisibility(View.GONE);

                } else {
                    recyclerFavoriteOther.setVisibility(View.GONE);
                    txtEmptyFavorite.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<ArtResponse> callR, Throwable t) {
                recyclerFavoriteOther.setVisibility(View.GONE);
            }
        });
    }
}