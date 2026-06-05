package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.UserAdapter;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.UserApiService;
import com.example.storyhub.models.FollowListResponse;
import com.example.storyhub.models.User;
import com.example.storyhub.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView txtTitle;
    RecyclerView recyclerUsers;

    TokenManager tokenManager;
    UserApiService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        tokenManager = new TokenManager(this);
        userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);

        String type = getIntent().getStringExtra("type"); // followers/following
        int userId = getIntent().getIntExtra("user_id", -1);

        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        String targetUsername = getIntent().getStringExtra("username");

        if (type != null && userId != -1) {
            String judul = type.equals("followers") ? "Followers" : "Following";
            if (targetUsername != null && !targetUsername.isEmpty()) {
                judul = judul + " " + targetUsername;
            }
            txtTitle.setText(judul);
            loadUsers(type, userId);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUsers(String type, int userId) {
        if (type.equals("followers")) {
            userService.getFollowers(userId).enqueue(new Callback<FollowListResponse>() {
                @Override
                public void onResponse(Call<FollowListResponse> call,
                                       Response<FollowListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<User> list = response.body().data;
                        if (list == null) list = response.body().follower;
                        showUsers(list);
                    }
                }
                @Override public void onFailure(Call<FollowListResponse> call, Throwable t) {}
            });
        } else {
            userService.getFollowing(userId).enqueue(new Callback<FollowListResponse>() {
                @Override
                public void onResponse(Call<FollowListResponse> call,
                                       Response<FollowListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<User> list = response.body().data;
                        if (list == null) list = response.body().following;
                        showUsers(list);
                    }
                }
                @Override public void onFailure(Call<FollowListResponse> call, Throwable t) {}
            });
        }
    }

    private void showUsers(List<User> users) {
        if (users == null) return;
        UserAdapter adapter = new UserAdapter(this, users, user -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("username", user.username);
            startActivity(intent);
        });
        recyclerUsers.setAdapter(adapter);
    }
}