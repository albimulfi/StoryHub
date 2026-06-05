package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.ArtSearchAdapter;
import com.example.storyhub.adapters.ChapterListAdapter;
import com.example.storyhub.adapters.UserAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.UserApiService;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.ChapterListResponse;
import com.example.storyhub.models.UserListResponse;
import com.example.storyhub.utils.TokenManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    ImageView btnBack;
    EditText etSearch;
    LinearLayout tabArt, tabChapter, tabUser;
    TextView txtTabArt, txtTabChapter, txtTabUser;
    RecyclerView recyclerResult;
    TextView txtEmpty;

    String currentTab = "art";
    String currentQuery = "";

    TokenManager tokenManager;
    ArtApiService artService;
    UserApiService userService;

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> debounceTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);
        userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);

        initViews();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        tabArt = findViewById(R.id.tabArt);
        tabChapter = findViewById(R.id.tabChapter);
        tabUser = findViewById(R.id.tabUser);
        txtTabArt = findViewById(R.id.txtTabArt);
        txtTabChapter = findViewById(R.id.txtTabChapter);
        txtTabUser = findViewById(R.id.txtTabUser);
        recyclerResult = findViewById(R.id.recyclerResult);
        txtEmpty = findViewById(R.id.txtEmpty);

        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                debounceSearch();
            }
        });

        tabArt.setOnClickListener(v -> switchTab("art"));
        tabChapter.setOnClickListener(v -> switchTab("chapter"));
        tabUser.setOnClickListener(v -> switchTab("user"));

        switchTab("art");

        etSearch.requestFocus();
    }

    private void debounceSearch() {
        if (debounceTask != null) debounceTask.cancel(false);

        debounceTask = scheduler.schedule(() ->
                        runOnUiThread(() -> loadCurrentTab()),
                400, TimeUnit.MILLISECONDS
        );
    }

    private void switchTab(String tab) {
        currentTab = tab;

        txtTabArt.setTextColor(getResources().getColor(R.color.black));
        txtTabChapter.setTextColor(getResources().getColor(R.color.black));
        txtTabUser.setTextColor(getResources().getColor(R.color.black));

        tabArt.setBackgroundResource(0);
        tabChapter.setBackgroundResource(0);
        tabUser.setBackgroundResource(0);

        switch (tab) {
            case "art":
                txtTabArt.setTextColor(getResources().getColor(R.color.primary));
                tabArt.setBackgroundResource(R.drawable.tab_indicator);
                recyclerResult.setLayoutManager(new LinearLayoutManager(this));
                break;
            case "chapter":
                txtTabChapter.setTextColor(getResources().getColor(R.color.primary));
                tabChapter.setBackgroundResource(R.drawable.tab_indicator);
                recyclerResult.setLayoutManager(new LinearLayoutManager(this));
                break;
            case "user":
                txtTabUser.setTextColor(getResources().getColor(R.color.primary));
                tabUser.setBackgroundResource(R.drawable.tab_indicator);
                recyclerResult.setLayoutManager(new LinearLayoutManager(this));
                break;
        }

        loadCurrentTab();
    }

    private void loadCurrentTab() {
        if (currentQuery.isEmpty()) {
            loadDefault();
        } else {
            doSearch();
        }
    }

    private void loadDefault() {
        switch (currentTab) {
            case "art": loadDefaultArts(); break;
            case "chapter": loadDefaultChapters(); break;
            case "user": loadDefaultUsers(); break;
        }
    }

    private void loadDefaultArts() {
        txtEmpty.setVisibility(View.GONE);
        artService.getArts(1, 20, null, null, null, null, "ratingArt", "DESC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            txtEmpty.setVisibility(View.GONE);
                            recyclerResult.setAdapter(new ArtSearchAdapter(
                                    SearchActivity.this,
                                    response.body().data,
                                    art -> {
                                        Intent intent = new Intent(SearchActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    }));
                        }
                    }
                    @Override public void onFailure(Call<ArtResponse> call, Throwable t) {}
                });
    }

    private void loadDefaultChapters() {
        txtEmpty.setVisibility(View.GONE);
        artService.getChapterList(1, 20, null, null, null, null, "ratingChapter", "DESC")
                .enqueue(new Callback<ChapterListResponse>() {
                    @Override
                    public void onResponse(Call<ChapterListResponse> call,
                                           Response<ChapterListResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            txtEmpty.setVisibility(View.GONE);
                            recyclerResult.setAdapter(new ChapterListAdapter(
                                    SearchActivity.this,
                                    response.body().data,
                                    chapter -> {
                                        Intent intent = new Intent(SearchActivity.this,
                                                ChapterReaderActivity.class);
                                        intent.putExtra("art_id", chapter.artId);
                                        intent.putExtra("chapter_number", chapter.chapterNumber);
                                        intent.putExtra("chapter_id", chapter.id);
                                        startActivity(intent);
                                    }));
                        }
                    }
                    @Override public void onFailure(Call<ChapterListResponse> call, Throwable t) {}
                });
    }

    private void loadDefaultUsers() {
        recyclerResult.setAdapter(null);
        txtEmpty.setVisibility(View.GONE);
        userService.searchUsers("")
                .enqueue(new Callback<UserListResponse>() {
                    @Override
                    public void onResponse(Call<UserListResponse> call,
                                           Response<UserListResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            txtEmpty.setVisibility(View.GONE);
                            recyclerResult.setAdapter(new UserAdapter(
                                    SearchActivity.this,
                                    response.body().data,
                                    user -> {
                                        Intent intent = new Intent(SearchActivity.this, UserProfileActivity.class);
                                        intent.putExtra("username", user.username);
                                        startActivity(intent);
                                    }));
                        } else {
                            recyclerResult.setAdapter(null);

                            txtEmpty.setVisibility(View.VISIBLE);
                            txtEmpty.setText("Belum ada user untuk ditampilkan.");
                        }
                    }
                    @Override public void onFailure(Call<UserListResponse> call, Throwable t) {}
                });
    }

    private void doSearch() {
        switch (currentTab) {
            case "art": searchArts(); break;
            case "chapter": searchChapters(); break;
            case "user": searchUsers(); break;
        }
    }

    private void searchArts() {
        final String tabRequest = currentTab;

        artService.searchArts(currentQuery, 1, 20, "ratingArt", "DESC")
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (!tabRequest.equals(currentTab)) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            txtEmpty.setVisibility(View.GONE);

                            recyclerResult.setAdapter(new ArtSearchAdapter(
                                    SearchActivity.this,
                                    response.body().data,
                                    art -> {
                                        Intent intent = new Intent(SearchActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    }));
                        } else {
                            recyclerResult.setAdapter(null);
                            txtEmpty.setVisibility(View.VISIBLE);
                            txtEmpty.setText("Tidak ada art yang cocok dengan \""
                                    + currentQuery + "\"");
                        }
                    }
                    @Override public void onFailure(Call<ArtResponse> call, Throwable t) {}
                });
    }

    private void searchChapters() {
        final String tabRequest = currentTab;

        artService.searchChapters(currentQuery, 1, 20, "ratingChapter", "DESC")
                .enqueue(new Callback<ChapterListResponse>() {
                    @Override
                    public void onResponse(Call<ChapterListResponse> call,
                                           Response<ChapterListResponse> response) {
                        if (!tabRequest.equals(currentTab)) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            txtEmpty.setVisibility(View.GONE);

                            recyclerResult.setAdapter(new ChapterListAdapter(
                                    SearchActivity.this,
                                    response.body().data,
                                    chapter -> {
                                        Intent intent = new Intent(SearchActivity.this,
                                                ChapterReaderActivity.class);
                                        intent.putExtra("art_id", chapter.artId);
                                        intent.putExtra("chapter_number", chapter.chapterNumber);
                                        intent.putExtra("chapter_id", chapter.id);
                                        startActivity(intent);
                                    }));
                        } else {
                            recyclerResult.setAdapter(null);
                            txtEmpty.setVisibility(View.VISIBLE);
                            txtEmpty.setText("Tidak ada chapter yang cocok dengan \""
                                    + currentQuery + "\"");
                        }
                    }
                    @Override public void onFailure(Call<ChapterListResponse> call, Throwable t) {}
                });
    }

    private void searchUsers() {
        final String tabRequest = currentTab;

        userService.searchUsers(currentQuery)
                .enqueue(new Callback<UserListResponse>() {
                    @Override
                    public void onResponse(Call<UserListResponse> call,
                                           Response<UserListResponse> response) {
                        if (!tabRequest.equals(currentTab)) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            txtEmpty.setVisibility(View.GONE);

                            recyclerResult.setAdapter(new UserAdapter(
                                    SearchActivity.this,
                                    response.body().data,
                                    user -> {
                                        Intent intent = new Intent(SearchActivity.this, UserProfileActivity.class);
                                        intent.putExtra("username", user.username);
                                        startActivity(intent);
                                    }));
                        } else {
                            recyclerResult.setAdapter(null);
                            txtEmpty.setVisibility(View.VISIBLE);
                            txtEmpty.setText("Tidak ada user yang cocok dengan \""
                                    + currentQuery + "\"");
                        }
                    }
                    @Override public void onFailure(Call<UserListResponse> call, Throwable t) {}
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduler.shutdown();
    }
}