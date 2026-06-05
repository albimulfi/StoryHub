package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.storyhub.api.FilterApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.UserApiService;
import com.example.storyhub.models.Art;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.Genre;
import com.example.storyhub.models.GenreResponse;
import com.example.storyhub.models.Mood;
import com.example.storyhub.models.MoodResponse;
import com.example.storyhub.models.ReadingHistory;
import com.example.storyhub.models.ReadingHistoryResponse;
import com.example.storyhub.models.SubscribeResponse;
import com.example.storyhub.models.Tag;
import com.example.storyhub.models.TagResponse;
import com.example.storyhub.utils.TokenManager;

import android.widget.ImageView;
import android.widget.FrameLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    // navbar
    LinearLayout navHome, navDiscover, navMyList, navBookmark, navProfile;

    // search
    LinearLayout searchBar;

    // hero banner
    FrameLayout bannerGame, bannerComic, bannerNovel;

    // continue reading
    LinearLayout layoutContinueReading;
    RecyclerView recyclerContinueReading;

    // trending
    RecyclerView recyclerTrending;
    ArtAdapter trendingAdapter;

    // teks empty
    TextView txtEmptyCategoryResult, txtEmptyMoodResult, txtEmptyGenreResult, txtEmptyTagResult;

    // filter
    LinearLayout containerCategory,  containerMood, containerGenre, containerTag;

    RecyclerView recyclerCategoryResult, recyclerMoodResult, recyclerGenreResult, recyclerTagResult;

    TokenManager tokenManager;
    ArtApiService artService;
    FilterApiService filterService;
    String selectedFilterType = null;
    String selectedFilterValue = null;
    Button lastSelectedBtn = null;
    boolean isSubscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager)
                .create(ArtApiService.class);
        filterService = RetrofitClient.getInstance()
                .create(FilterApiService.class);

        initViews();
        setupNav();
        loadHeroBanner();
        loadTrending();
        loadCategories();
        loadMoods();
        loadGenres();
        loadTags();

        if (tokenManager.isLoggedIn()) {
            checkSubscription();
            layoutContinueReading.setVisibility(View.VISIBLE);
            loadContinueReading();
        } else {
            layoutContinueReading.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navDiscover = findViewById(R.id.navDiscover);
        navMyList = findViewById(R.id.navMyList);
        navBookmark = findViewById(R.id.navBookmark);
        navProfile = findViewById(R.id.navProfile);

        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        bannerGame = findViewById(R.id.bannerGame);
        bannerComic = findViewById(R.id.bannerComic);
        bannerNovel = findViewById(R.id.bannerNovel);

        layoutContinueReading = findViewById(R.id.layoutContinueReading);

        recyclerContinueReading = findViewById(R.id.recyclerContinueReading);
        if (recyclerContinueReading != null) {
            recyclerContinueReading.setLayoutManager(new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false));
        }

        recyclerTrending = findViewById(R.id.recyclerTrending);
        recyclerTrending.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        containerCategory = findViewById(R.id.containerCategory);
        containerMood = findViewById(R.id.containerMood);
        containerGenre = findViewById(R.id.containerGenre);
        containerTag = findViewById(R.id.containerTag);

        txtEmptyCategoryResult = findViewById(R.id.txtEmptyCategoryResult);
        txtEmptyMoodResult = findViewById(R.id.txtEmptyMoodResult);
        txtEmptyGenreResult = findViewById(R.id.txtEmptyGenreResult);
        txtEmptyTagResult = findViewById(R.id.txtEmptyTagResult);

        recyclerCategoryResult = findViewById(R.id.recyclerCategoryResult);
        recyclerCategoryResult.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        recyclerMoodResult = findViewById(R.id.recyclerMoodResult);
        recyclerMoodResult.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        recyclerGenreResult = findViewById(R.id.recyclerGenreResult);
        recyclerGenreResult.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        recyclerTagResult = findViewById(R.id.recyclerTagResult);
        recyclerTagResult.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void checkSubscription() {
        UserApiService userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);
        userService.isSubscribe(tokenManager.getUserId()).enqueue(new Callback<SubscribeResponse>() {
            @Override
            public void onResponse(Call<SubscribeResponse> call, Response<SubscribeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isSubscribed = response.body().isSubscribe;
                }
            }
            @Override public void onFailure(Call<SubscribeResponse> call, Throwable t) {}
        });
    }

    private void setupNav() {
        navDiscover.setOnClickListener(v ->
                startActivity(new Intent(this, DiscoverActivity.class)));
        navMyList.setOnClickListener(v ->
                startActivity(new Intent(this, MyListActivity.class)));
        navBookmark.setOnClickListener(v ->
                startActivity(new Intent(this, BookmarkActivity.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    // hero banner

    private void loadHeroBanner() {
        artService.getArts(1, 1, "Game,Visual Novel,Story Game",
                        null, null, null, "ratingArt", "DESC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            setupBannerCard(bannerGame,
                                    response.body().data.get(0),
                                    "Trending Game", true);
                        }
                    }
                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {}
                });

        artService.getArts(1, 1, "Manhwa,Manga,Comic",
                        null, null, null, "ratingArt", "DESC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            setupBannerCard(bannerComic,
                                    response.body().data.get(0),
                                    "Trending Comic", false);
                        }
                    }
                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {}
                });

        artService.getArts(1, 1, "Novel,Book",
                        null, null, null, "ratingArt", "DESC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {
                            setupBannerCard(bannerNovel,
                                    response.body().data.get(0),
                                    "Trending Novel", false);
                        }
                    }
                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {}
                });
    }

    private void setupBannerCard(FrameLayout cardView, Art art,
                                 String label, boolean isGame) {
        if (cardView == null || art == null) return;

        ImageView imgBanner = cardView.findViewById(R.id.imgBanner);
        TextView txtLabel = cardView.findViewById(R.id.txtLabel);
        TextView txtTitle = cardView.findViewById(R.id.txtTitle);
        TextView txtSub = cardView.findViewById(R.id.txtSub);
        Button btnAction = cardView.findViewById(R.id.btnAction);

        txtLabel.setText(label);
        txtTitle.setText(art.title);
        txtSub.setText(art.totalReviews + " Readers");
        btnAction.setText(isGame ? "Play Now" : "Read Now");

        Glide.with(this)
                .load(RetrofitClient.IMAGE_BASE_URL + art.bannerImg)
                .placeholder(R.drawable.placeholder)
                .into(imgBanner);

        View.OnClickListener goToDetail = v -> {
            Intent intent = new Intent(HomeActivity.this,
                    DetailContentActivity.class);
            intent.putExtra("art_id", art.id);
            startActivity(intent);
        };

        cardView.setOnClickListener(goToDetail);
        btnAction.setOnClickListener(goToDetail);
    }

    // continue reading

    private void loadContinueReading() {
        artService.getContinueReading().enqueue(new Callback<ReadingHistoryResponse>() {
            @Override
            public void onResponse(Call<ReadingHistoryResponse> call,
                                   Response<ReadingHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    List<ReadingHistory> list = response.body().data;
                    ContinueReadingAdapter adapter = new ContinueReadingAdapter(
                            HomeActivity.this, list, history -> {
                        Intent intent = new Intent(HomeActivity.this,
                                DetailContentActivity.class);
                        intent.putExtra("art_id", history.artId);
                        intent.putExtra("from_continue_reading", true);
                        intent.putExtra("latest_chapter_id", history.latestChapterId);
                        startActivity(intent);
                    });

                    if (recyclerContinueReading != null) {
                        recyclerContinueReading.setAdapter(adapter);
                        layoutContinueReading.setVisibility(View.VISIBLE);
                    }
                } else {
                    layoutContinueReading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ReadingHistoryResponse> call, Throwable t) {
                layoutContinueReading.setVisibility(View.GONE);
            }
        });
    }

    // trending

    private void loadTrending() {
        artService.getTrending(5, "reviewersArt", "DESC")
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null) {
                            trendingAdapter = new ArtAdapter(
                                    HomeActivity.this,
                                    response.body().data,
                                    art -> {
                                        Intent intent = new Intent(HomeActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                            recyclerTrending.setAdapter(trendingAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {
                        Toast.makeText(HomeActivity.this,
                                "Gagal load trending.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // filter category, tag, genre, mood

    private void loadCategories() {
        String[] categories = {
                "Visual Novel", "Manga", "Novel", "Comic", "Manhwa",
                "Story Game", "Game", "Book"
        };

        containerCategory.removeAllViews();

        Button firstBtn = null;

        for (String cat : categories) {
            Button btn = makeChipButton(cat);
            btn.setOnClickListener(v ->
                    handleChipClick(btn, containerCategory, "category", cat));

            containerCategory.addView(btn);

            if (firstBtn == null) {
                firstBtn = btn;
            }
        }

        if (firstBtn != null) {
            handleChipClick(firstBtn, containerCategory, "category", categories[0]);
        }
    }

    private void loadFilteredByCategory(String category) {
        recyclerCategoryResult.setVisibility(View.GONE);
        txtEmptyCategoryResult.setVisibility(View.GONE);

        artService.getArts(1, 12, category, null, null, null, "ratingArt", "DESC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty()) {

                            ArtAdapter adapter = new ArtAdapter(
                                    HomeActivity.this,
                                    response.body().data,
                                    art -> {
                                        Intent intent = new Intent(HomeActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                            recyclerCategoryResult.setAdapter(adapter);
                            recyclerCategoryResult.setVisibility(View.VISIBLE);
                            txtEmptyCategoryResult.setVisibility(View.GONE);
                        } else {
                            recyclerCategoryResult.setVisibility(View.GONE);
                            txtEmptyCategoryResult.setText(
                                    "Art dengan kategori " + category + " masih belum tercipta.");
                            txtEmptyCategoryResult.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {
                        Toast.makeText(HomeActivity.this,
                                "Gagal load data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMoods() {
        filterService.getMoods().enqueue(new Callback<MoodResponse>() {
            @Override
            public void onResponse(Call<MoodResponse> call,
                                   Response<MoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    buildMoodButtons(response.body().data);
                }
            }
            @Override
            public void onFailure(Call<MoodResponse> call, Throwable t) {}
        });
    }

    private void loadGenres() {
        filterService.getGenres().enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call,
                                   Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    buildGenreButtons(response.body().data);
                }
            }
            @Override
            public void onFailure(Call<GenreResponse> call, Throwable t) {}
        });
    }

    private void loadTags() {
        filterService.getTags().enqueue(new Callback<TagResponse>() {
            @Override
            public void onResponse(Call<TagResponse> call,
                                   Response<TagResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    buildTagButtons(response.body().data);
                }
            }
            @Override
            public void onFailure(Call<TagResponse> call, Throwable t) {}
        });
    }

    private void buildMoodButtons(List<Mood> moods) {
        containerMood.removeAllViews();

        Button firstBtn = null;
        String firstMood = null;

        for (Mood mood : moods) {
            Button btn = makeChipButton(mood.mood);

            btn.setOnClickListener(v ->
                    handleChipClick(btn, containerMood,
                            "mood", mood.mood));

            containerMood.addView(btn);

            if (firstBtn == null) {
                firstBtn = btn;
                firstMood = mood.mood;
            }
        }

        if (firstBtn != null) {
            handleChipClick(firstBtn, containerMood, "mood", firstMood);
        }
    }

    private void buildGenreButtons(List<Genre> genres) {
        containerGenre.removeAllViews();

        Button firstBtn = null;
        String firstGenre = null;

        for (Genre genre : genres) {
            Button btn = makeChipButton(genre.genre);

            btn.setOnClickListener(v ->
                    handleChipClick(btn, containerGenre,
                            "genre", genre.genre));

            containerGenre.addView(btn);

            if (firstBtn == null) {
                firstBtn = btn;
                firstGenre = genre.genre;
            }
        }

        if (firstBtn != null) {
            handleChipClick(firstBtn, containerGenre, "genre", firstGenre);
        }
    }

    private void buildTagButtons(List<Tag> tags) {
        containerTag.removeAllViews();

        Button firstBtn = null;
        String firstTag = null;

        for (Tag tag : tags) {
            Button btn = makeChipButton("#" + tag.tag);

            btn.setOnClickListener(v ->
                    handleChipClick(btn, containerTag,
                            "tag", tag.tag));

            containerTag.addView(btn);

            if (firstBtn == null) {
                firstBtn = btn;
                firstTag = tag.tag;
            }
        }

        if (firstBtn != null) {
            handleChipClick(firstBtn, containerTag, "tag", firstTag);
        }
    }

    private void handleChipClick(Button btn, LinearLayout container,
                                 String type, String value) {

        if (btn == lastSelectedBtn) {
            Intent intent = new Intent(this, FilterResultActivity.class);
            intent.putExtra("filter_type", type);
            intent.putExtra("filter_value", value);
            startActivity(intent);
            return;
        }

        highlightButton(container, btn);

        lastSelectedBtn = btn;
        selectedFilterType = type;
        selectedFilterValue = value;

        if (type.equals("category")) {
            loadFilteredByCategory(value);
        } else {
            loadFilteredArts(type, value);
        }
    }

    private Button makeChipButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(getResources().getColor(R.color.primary));
        btn.setBackgroundResource(R.drawable.chip_background);

        btn.setPadding(32, 16, 32, 16);

        btn.setElevation(10f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(16);
        btn.setLayoutParams(params);

        return btn;
    }

    private void highlightButton(LinearLayout container, Button selected) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof Button) {
                child.setBackgroundResource(R.drawable.chip_background);
                ((Button) child).setTextColor(
                        getResources().getColor(R.color.primary));
            }
        }
        selected.setBackgroundResource(R.drawable.button_gradient);
        selected.setTextColor(getResources().getColor(R.color.white));
    }

    private void loadFilteredArts(String filterType, String filterValue) {
        String mood = filterType.equals("mood")  ? filterValue : null;
        String genre = filterType.equals("genre") ? filterValue : null;
        String tag = filterType.equals("tag")   ? filterValue : null;

        switch (filterType) {
            case "mood":
                recyclerMoodResult.setVisibility(View.GONE);
                txtEmptyMoodResult.setVisibility(View.GONE);
                break;
            case "genre":
                recyclerGenreResult.setVisibility(View.GONE);
                txtEmptyGenreResult.setVisibility(View.GONE);
                break;
            case "tag":
                recyclerTagResult.setVisibility(View.GONE);
                txtEmptyTagResult.setVisibility(View.GONE);
                break;
        }

        artService.getArts(1, 12, null, genre, tag, mood, "ratingArt", "ASC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        boolean hasData = response.isSuccessful()
                                && response.body() != null
                                && response.body().data != null
                                && !response.body().data.isEmpty();

                        switch (filterType) {
                            case "mood":
                                if (hasData) {
                                    ArtAdapter a = new ArtAdapter(HomeActivity.this,
                                            response.body().data, art -> {
                                        Intent intent = new Intent(HomeActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                                    recyclerMoodResult.setAdapter(a);
                                    recyclerMoodResult.setVisibility(View.VISIBLE);
                                    txtEmptyMoodResult.setVisibility(View.GONE);
                                } else {
                                    recyclerMoodResult.setVisibility(View.GONE);
                                    txtEmptyMoodResult.setText(
                                            "Art dengan mood " + filterValue
                                                    + " masih belum tercipta.");
                                    txtEmptyMoodResult.setVisibility(View.VISIBLE);
                                }
                                break;

                            case "genre":
                                if (hasData) {
                                    ArtAdapter a = new ArtAdapter(HomeActivity.this,
                                            response.body().data, art -> {
                                        Intent intent = new Intent(HomeActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                                    recyclerGenreResult.setAdapter(a);
                                    recyclerGenreResult.setVisibility(View.VISIBLE);
                                    txtEmptyGenreResult.setVisibility(View.GONE);
                                } else {
                                    recyclerGenreResult.setVisibility(View.GONE);
                                    txtEmptyGenreResult.setText(
                                            "Art dengan genre " + filterValue
                                                    + " masih belum tercipta.");
                                    txtEmptyGenreResult.setVisibility(View.VISIBLE);
                                }
                                break;

                            case "tag":
                                if (hasData) {
                                    ArtAdapter a = new ArtAdapter(HomeActivity.this,
                                            response.body().data, art -> {
                                        Intent intent = new Intent(HomeActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                                    recyclerTagResult.setAdapter(a);
                                    recyclerTagResult.setVisibility(View.VISIBLE);
                                    txtEmptyTagResult.setVisibility(View.GONE);
                                } else {
                                    recyclerTagResult.setVisibility(View.GONE);
                                    txtEmptyTagResult.setText(
                                            "Art dengan tag " + filterValue
                                                    + " masih belum tercipta.");
                                    txtEmptyTagResult.setVisibility(View.VISIBLE);
                                }
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {
                        Toast.makeText(HomeActivity.this,
                                "Gagal load data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}