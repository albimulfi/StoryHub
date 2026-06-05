package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.ArtAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookmarkActivity extends AppCompatActivity {

    RecyclerView recyclerBookmark;
    LinearLayout layoutNotLoggedIn;
    HorizontalScrollView layoutFilter;
    LinearLayout navHome, navDiscover, navMyList, navBookmark, navProfile;
    Button btnLogin, btnRegister;
    TextView txtEmptyBookmark;
    Button btnFilterAll, btnFilterNovel, btnFilterManga,
            btnFilterManhwa, btnFilterComic, btnFilterGame, btnFilterChapter;

    TokenManager tokenManager;
    ArtApiService artService;
    String currentFilter = "art";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);

        initViews();
        setupNav();

        if (tokenManager.isLoggedIn()) {
            layoutNotLoggedIn.setVisibility(View.GONE);
            layoutFilter.setVisibility(View.VISIBLE);
            recyclerBookmark.setVisibility(View.VISIBLE);
            loadBookmarks("art");
        } else {
            layoutNotLoggedIn.setVisibility(View.VISIBLE);
            layoutFilter.setVisibility(View.GONE);
            recyclerBookmark.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        recyclerBookmark = findViewById(R.id.recyclerBookmark);
        recyclerBookmark.setLayoutManager(new GridLayoutManager(this, 2));

        layoutNotLoggedIn = findViewById(R.id.layoutNotLoggedIn);
        layoutFilter = findViewById(R.id.layoutFilter);
        navHome = findViewById(R.id.navHome);
        navDiscover = findViewById(R.id.navDiscover);
        navMyList = findViewById(R.id.navMyList);
        navBookmark = findViewById(R.id.navBookmark);
        navProfile = findViewById(R.id.navProfile);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterNovel = findViewById(R.id.btnFilterNovel);
        btnFilterManga = findViewById(R.id.btnFilterManga);
        btnFilterManhwa = findViewById(R.id.btnFilterManhwa);
        btnFilterComic = findViewById(R.id.btnFilterComic);
        btnFilterGame = findViewById(R.id.btnFilterGame);
        btnFilterChapter = findViewById(R.id.btnFilterChapter);
        txtEmptyBookmark = findViewById(R.id.txtEmptyBookmark);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        setupFilterButtons();
    }

    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "art";
            loadBookmarks("art");
            highlightFilter(btnFilterAll);
        });
        btnFilterNovel.setOnClickListener(v -> {
            currentFilter = "novel";
            loadBookmarks("novel");
            highlightFilter(btnFilterNovel);
        });
        btnFilterManga.setOnClickListener(v -> {
            currentFilter = "manga";
            loadBookmarks("manga");
            highlightFilter(btnFilterManga);
        });
        btnFilterManhwa.setOnClickListener(v -> {
            currentFilter = "manhwa";
            loadBookmarks("manhwa");
            highlightFilter(btnFilterManhwa);
        });
        btnFilterComic.setOnClickListener(v -> {
            currentFilter = "comic";
            loadBookmarks("comic");
            highlightFilter(btnFilterComic);
        });
        btnFilterGame.setOnClickListener(v -> {
            currentFilter = "game";
            loadBookmarks("game");
            highlightFilter(btnFilterGame);
        });
        btnFilterChapter.setOnClickListener(v -> {
            currentFilter = "chapter";
            loadBookmarks("chapter");
            highlightFilter(btnFilterChapter);
        });
    }

    private void highlightFilter(Button selected) {
        Button[] all = {btnFilterAll, btnFilterNovel, btnFilterManga,
                btnFilterManhwa, btnFilterComic, btnFilterGame, btnFilterChapter};
        for (Button btn : all) {
            btn.setBackgroundResource(R.drawable.chip_background);
            btn.setTextColor(getResources().getColor(R.color.primary));
        }
        selected.setBackgroundResource(R.drawable.button_gradient);
        selected.setTextColor(getResources().getColor(R.color.white));
    }

    private void loadBookmarks(String category) {
        txtEmptyBookmark.setVisibility(View.GONE);
        recyclerBookmark.setVisibility(View.GONE);

        artService.getBookmarks(category).enqueue(new Callback<ArtResponse>() {
            @Override
            public void onResponse(Call<ArtResponse> call, Response<ArtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().data == null || response.body().data.isEmpty()) {
                        recyclerBookmark.setVisibility(View.GONE);

                        String namaKategori = getCategoryLabel(category);
                        txtEmptyBookmark.setText("Art dengan kategori " + namaKategori
                                + " masih belum ada yang kamu bookmark.");
                        txtEmptyBookmark.setVisibility(View.VISIBLE);
                    } else {
                        txtEmptyBookmark.setVisibility(View.GONE);
                        recyclerBookmark.setVisibility(View.VISIBLE);

                        ArtAdapter adapter = new ArtAdapter(
                                BookmarkActivity.this,
                                response.body().data,
                                art -> {
                                    Intent intent = new Intent(BookmarkActivity.this,
                                            DetailContentActivity.class);
                                    intent.putExtra("art_id", art.id);
                                    startActivity(intent);
                                });
                        recyclerBookmark.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtResponse> call, Throwable t) {
                txtEmptyBookmark.setText("Gagal memuat bookmark.");
                txtEmptyBookmark.setVisibility(View.VISIBLE);
            }
        });
    }

    private String getCategoryLabel(String category) {
        switch (category) {
            case "novel": return "Novel";
            case "manga": return "Manga";
            case "manhwa": return "Manhwa";
            case "comic": return "Comic";
            case "game": return "Game";
            case "chapter": return "Chapter";
            default: return "All";
        }
    }

    private void setupNav() {
        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));
        navDiscover.setOnClickListener(v ->
                startActivity(new Intent(this, DiscoverActivity.class)));
        navMyList.setOnClickListener(v ->
                startActivity(new Intent(this, MyListActivity.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}