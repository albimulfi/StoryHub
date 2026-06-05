package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
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
import com.example.storyhub.models.ReadingHistory;
import com.example.storyhub.models.ReadingHistoryResponse;
import com.example.storyhub.models.SubscribeRequest;
import com.example.storyhub.models.SubscribeResponse;
import com.example.storyhub.models.User;
import com.example.storyhub.models.UserResponse;
import com.example.storyhub.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgProfile;
    TextView txtName, txtUsername, txtBio;
    TextView txtFollowers, txtFollowing, txtBookmarkCount, txtReadCount;
    TextView txtSubscribeStatus;
    LinearLayout sectionMyArts, sectionFavorite, sectionFollowInfo;
    LinearLayout navHome, navDiscover, navMyList, navBookmark, navProfile;
    LinearLayout layoutSubscribeBanner, layoutContinueReading;
    LinearLayout layoutNotLoggedIn;
    Button btnLogin, btnRegister, btnLogout, btnCreateNovel;
    AppCompatImageButton btnEditProfile;
    RecyclerView recyclerContinueReading, recyclerMyArts, recyclerFavorite;

    TokenManager tokenManager;
    UserApiService userService;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tokenManager = new TokenManager(this);
        userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);

        initViews();
        setupNav();

        if (tokenManager.isLoggedIn()) {
            showLoggedInUI();
            loadProfile();
            loadContinueReading();
            loadFavoriteArts();
            loadMyArts();
            checkSubscription();
        } else {
            showGuestUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager.isLoggedIn()) {
            loadProfile();
        }
    }

    private void initViews() {
        imgProfile = findViewById(R.id.imgProfile);
        txtName = findViewById(R.id.txtName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        txtFollowers = findViewById(R.id.txtFollowers);
        txtFollowing = findViewById(R.id.txtFollowing);
        txtSubscribeStatus = findViewById(R.id.txtSubscribeStatus);
        navHome = findViewById(R.id.navHome);
        navDiscover = findViewById(R.id.navDiscover);
        navMyList = findViewById(R.id.navMyList);
        navBookmark = findViewById(R.id.navBookmark);
        // txtBookmarkCount = findViewById(R.id.txtBookmarkCount);
        // txtReadCount = findViewById(R.id.txtReadCount);
        navProfile = findViewById(R.id.navProfile);
        layoutSubscribeBanner = findViewById(R.id.layoutSubscribeBanner);
        layoutContinueReading = findViewById(R.id.layoutContinueReading);
        layoutNotLoggedIn = findViewById(R.id.layoutNotLoggedIn);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogout = findViewById(R.id.btnLogout);
        btnCreateNovel = findViewById(R.id.btnCreateNovel);
        recyclerContinueReading = findViewById(R.id.recyclerContinueReading);
        recyclerFavorite = findViewById(R.id.recyclerFavorite);
        recyclerMyArts = findViewById(R.id.recyclerMyArts);
        sectionMyArts = findViewById(R.id.sectionMyArts);
        sectionFavorite = findViewById(R.id.sectionFavorite);
        sectionFollowInfo = findViewById(R.id.sectionFollowInfo);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        recyclerContinueReading.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        recyclerFavorite.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
        recyclerMyArts.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        txtFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("type", "followers");
            intent.putExtra("user_id", tokenManager.getUserId());
            intent.putExtra("username", tokenManager.getUsername());
            startActivity(intent);
        });

        txtFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("type", "following");
            intent.putExtra("user_id", tokenManager.getUserId());
            intent.putExtra("username", tokenManager.getUsername());
            startActivity(intent);
        });

        TextView btnLihatWorks = findViewById(R.id.btnLihatWorks);
        btnLihatWorks.setOnClickListener(v ->
                startActivity(new Intent(this, MyWorksActivity.class)));

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        layoutSubscribeBanner.setOnClickListener(v -> showSubscribeDialog());

        btnCreateNovel.setOnClickListener(v ->
                startActivity(new Intent(this, CreateNovelActivity.class)));

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void setupNav() {
        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        navDiscover.setOnClickListener(v ->
                startActivity(new Intent(this, DiscoverActivity.class)));

        navMyList.setOnClickListener(v ->
                startActivity(new Intent(this, MyListActivity.class)));

        navBookmark.setOnClickListener(v ->
                startActivity(new Intent(this, BookmarkActivity.class)));
    }

    private void showLoggedInUI() {
        layoutNotLoggedIn.setVisibility(View.GONE);
        layoutContinueReading.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.VISIBLE);
        btnCreateNovel.setVisibility(View.VISIBLE);
        btnEditProfile.setVisibility(View.VISIBLE);
    }

    private void showGuestUI() {
        layoutNotLoggedIn.setVisibility(View.VISIBLE);
        layoutContinueReading.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);
        btnCreateNovel.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.GONE);
        layoutSubscribeBanner.setVisibility(View.GONE);
        recyclerMyArts.setVisibility(View.GONE);
        recyclerFavorite.setVisibility(View.GONE);
        txtFollowers.setVisibility(View.GONE);
        txtFollowing.setVisibility(View.GONE);
        txtSubscribeStatus.setVisibility(View.GONE);

        sectionMyArts.setVisibility(View.GONE);
        sectionFavorite.setVisibility(View.GONE);
        sectionFollowInfo.setVisibility(View.GONE);
    }

    private void loadProfile() {
        String localUsername = tokenManager.getUsername();
        if (localUsername != null) {
            txtUsername.setText("@" + localUsername);
            txtName.setText(localUsername);
        }
        userService.getProfile().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                    User user = response.body().user;

                    txtName.setText(user.name != null ? user.name : user.username);
                    txtUsername.setText("@" + user.username);
                    txtBio.setText(user.bio != null ? user.bio : "");

                    Glide.with(ProfileActivity.this)
                            .load(RetrofitClient.IMAGE_AUTH_URL + user.profileImg
                                    + "?t=" + System.currentTimeMillis())
                            .placeholder(R.drawable.placeholder_png)
                            .into(imgProfile);

                    loadFollowCounts(user.id);
                } else {
                    String username = tokenManager.getUsername();
                    if (username != null) {
                        txtName.setText(username);
                        txtUsername.setText("@" + username);
                    }
                    loadFollowCounts(tokenManager.getUserId());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Gagal load profil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowCounts(int userId) {
        userService.getFollowerCount(userId).enqueue(new Callback<FollowCountResponse>() {
            @Override
            public void onResponse(Call<FollowCountResponse> call, Response<FollowCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtFollowers.setText(response.body().total_follower + "\nFollowers");
                }
            }
            @Override
            public void onFailure(Call<FollowCountResponse> call, Throwable t) {}
        });

        userService.getFollowingCount(userId).enqueue(new Callback<FollowCountResponse>() {
            @Override
            public void onResponse(Call<FollowCountResponse> call, Response<FollowCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    txtFollowing.setText(response.body().total_following + "\nFollowing");
                }
            }
            @Override
            public void onFailure(Call<FollowCountResponse> call, Throwable t) {}
        });
    }

    private void loadContinueReading() {
        artService.getContinueReading().enqueue(new Callback<ReadingHistoryResponse>() {
            @Override
            public void onResponse(Call<ReadingHistoryResponse> call, Response<ReadingHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReadingHistory> list = response.body().data;

                    if (list != null && !list.isEmpty()) {
                        ContinueReadingAdapter adapter = new ContinueReadingAdapter(
                                ProfileActivity.this, list, history -> {
                            Intent intent = new Intent(ProfileActivity.this, DetailContentActivity.class);
                            intent.putExtra("art_id", history.artId);
                            intent.putExtra("from_continue_reading", true);
                            intent.putExtra("latest_chapter_id", history.latestChapterId);
                            startActivity(intent);
                        });
                        recyclerContinueReading.setAdapter(adapter);
                        layoutContinueReading.setVisibility(View.VISIBLE);
                    } else {
                        layoutContinueReading.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReadingHistoryResponse> call, Throwable t) {}
        });
    }

    private void loadFavoriteArts() {
        // art rating tertinggi yang direview si user
        artService.getArts(1, 5, null, null, null, null, "ratingArtUser", "DESC", "true")
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call, Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ArtAdapter adapter = new ArtAdapter(
                                    ProfileActivity.this,
                                    response.body().data,
                                    art -> {
                                        Intent intent = new Intent(ProfileActivity.this, DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                            recyclerFavorite.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {}
                });
    }

    private void loadMyArts() {
        artService.getMyArts("published").enqueue(new Callback<ArtResponse>() {
            @Override
            public void onResponse(Call<ArtResponse> call, Response<ArtResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    recyclerMyArts.setVisibility(View.VISIBLE);

                    ArtAdapter adapter = new ArtAdapter(
                            ProfileActivity.this,
                            response.body().data,
                            art -> {
                                Intent intent = new Intent(ProfileActivity.this, DetailContentActivity.class);
                                intent.putExtra("art_id", art.id);
                                startActivity(intent);
                            });
                    recyclerMyArts.setAdapter(adapter);

                } else {
                    recyclerMyArts.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ArtResponse> call, Throwable t) {}
        });
    }

    private void checkSubscription() {
        int userId = tokenManager.getUserId();
        userService.isSubscribe(userId).enqueue(new Callback<SubscribeResponse>() {
            @Override
            public void onResponse(Call<SubscribeResponse> call, Response<SubscribeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSubscribe) {
                        layoutSubscribeBanner.setVisibility(View.GONE);
                        txtSubscribeStatus.setText("👑 Premium Member");
                        txtSubscribeStatus.setVisibility(View.VISIBLE);
                    } else {
                        layoutSubscribeBanner.setVisibility(View.VISIBLE);
                        txtSubscribeStatus.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<SubscribeResponse> call, Throwable t) {}
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin logout?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    tokenManager.clear();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showSubscribeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_subscribe, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        AppCompatButton btnMonthly = dialogView.findViewById(R.id.btnMonthly);
        AppCompatButton btnYearly = dialogView.findViewById(R.id.btnYearly);
        TextView btnBatal = dialogView.findViewById(R.id.btnBatal);

        btnMonthly.setOnClickListener(v -> {
            dialog.dismiss();
            doSubscribe("monthly");
        });

        btnYearly.setOnClickListener(v -> {
            dialog.dismiss();
            doSubscribe("yearly");
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void doSubscribe(String plan) {
        userService.subscribe(new SubscribeRequest(plan))
                .enqueue(new Callback<SubscribeResponse>() {
                    @Override
                    public void onResponse(Call<SubscribeResponse> call, Response<SubscribeResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this,
                                    "Kamu sudah berlangganan. Selamat menikmati fitur premium King! 👑",
                                    Toast.LENGTH_LONG).show();
                            layoutSubscribeBanner.setVisibility(View.GONE);
                            txtSubscribeStatus.setText("👑 Premium Member");
                            txtSubscribeStatus.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override public void onFailure(Call<SubscribeResponse> call, Throwable t) {}
                });
    }
}