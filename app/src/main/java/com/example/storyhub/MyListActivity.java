package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.MyArtListAdapter;
import com.example.storyhub.adapters.MyChapterListAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.FilterApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.Genre;
import com.example.storyhub.models.Tag;
import com.example.storyhub.models.Mood;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.ChapterListResponse;
import com.example.storyhub.models.GenreResponse;
import com.example.storyhub.models.MoodResponse;
import com.example.storyhub.models.TagResponse;
import com.example.storyhub.utils.TokenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListActivity extends AppCompatActivity {
    LinearLayout navHome, navDiscover, navMyList, navBookmark, navProfile;

    // tabs
    Button btnTabMyArt, btnTabMyChapter;
    RecyclerView recyclerMyArt, recyclerMyChapter;
    MyArtListAdapter myArtAdapter;
    MyChapterListAdapter myChapterAdapter;

    // kalau belum login (info disuruh login)
    LinearLayout layoutNotLoggedIn;
    Button btnLogin, btnRegister;

    // panel filter
    LinearLayout layoutFilterPanel;
    Button btnOpenFilter, btnApplyFilter, btnResetFilter;

    // filter category
    CheckBox cbBook, cbNovel, cbManhwa, cbManga, cbComic,
            cbGame, cbVisualNovel, cbStoryGame;

    // filter sudah direview user atau belum
    RadioGroup radioReviewStatus;
    RadioButton rbReviewAll, rbReviewSudah, rbReviewBelum;

    // sort
    RadioGroup radioSort, radioOrder;

    // pilihan sort
    RadioButton rbSortTitle, rbSortRatingArt, rbSortReviewersArt,
            rbSortRatingCh, rbSortChapters, rbSortPublished,
            rbSortRatingArtUser, rbSortReviewersArtUser,
            rbSortRatingChUser, rbSortReviewersChUser;
    RadioButton rbOrderAsc, rbOrderDesc;

    // filter name
    com.google.android.flexbox.FlexboxLayout containerFilterGenre,
            containerFilterTag, containerFilterMood;

    // perhalamanan/pagination
    LinearLayout layoutPagination;
    Button btnPrevPage, btnNextPage;
    TextView txtPageInfo;

    // yang dipilih (selected)
    int currentPage = 1;
    int totalPages = 1;
    String currentTab = "art";

    TextView txtEmptyMyArt, txtEmptyMyChapter;

    EditText etSearchFilter;
    LinearLayout layoutSearchPreview, containerSearchPreview;
    com.google.android.flexbox.FlexboxLayout containerSelectedChips;

    List<String> allGenres = new ArrayList<>();
    List<String> allTags = new ArrayList<>();
    List<String> allMoods = new ArrayList<>();
    List<String> selectedGenres = new ArrayList<>();
    List<String> selectedTags = new ArrayList<>();
    List<String> selectedMoods = new ArrayList<>();
    Map<String, String> itemTypeMap = new HashMap<>();

    List<String> selectedCategories = new ArrayList<>();
    String selectedSort = "title";
    String selectedOrder = "ASC";
    String selectedReviewStatus = "all";
    boolean filterPanelOpen = false;

    TokenManager tokenManager;
    ArtApiService artService;
    FilterApiService filterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);
        filterService = RetrofitClient.getInstance().create(FilterApiService.class);

        initViews();
        setupNav();
        setupTabs();

        if (tokenManager.isLoggedIn()) {
            layoutNotLoggedIn.setVisibility(View.GONE);
            loadFilterOptions();
            loadMyArtList();
        } else {
            layoutNotLoggedIn.setVisibility(View.VISIBLE);
            layoutFilterPanel.setVisibility(View.GONE);
            recyclerMyArt.setVisibility(View.GONE);
            recyclerMyChapter.setVisibility(View.GONE);
            layoutPagination.setVisibility(View.GONE);
            btnTabMyArt.setVisibility(View.GONE);
            btnTabMyChapter.setVisibility(View.GONE);
            btnOpenFilter.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navDiscover = findViewById(R.id.navDiscover);
        navMyList = findViewById(R.id.navMyList);
        navBookmark = findViewById(R.id.navBookmark);
        navProfile = findViewById(R.id.navProfile);

        btnTabMyArt = findViewById(R.id.btnTabMyArt);
        btnTabMyChapter = findViewById(R.id.btnTabMyChapter);

        txtEmptyMyArt = findViewById(R.id.txtEmptyMyArt);
        txtEmptyMyChapter = findViewById(R.id.txtEmptyMyChapter);

        recyclerMyArt = findViewById(R.id.recyclerMyArt);
        recyclerMyArt.setLayoutManager(new LinearLayoutManager(this));

        recyclerMyChapter = findViewById(R.id.recyclerMyChapter);
        recyclerMyChapter.setLayoutManager(new LinearLayoutManager(this));

        layoutNotLoggedIn = findViewById(R.id.layoutNotLoggedIn);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        layoutFilterPanel = findViewById(R.id.layoutFilterPanel);
        btnOpenFilter = findViewById(R.id.btnOpenFilter);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnResetFilter = findViewById(R.id.btnResetFilter);

        cbBook = findViewById(R.id.cbBook);
        cbNovel = findViewById(R.id.cbNovel);
        cbManhwa = findViewById(R.id.cbManhwa);
        cbManga = findViewById(R.id.cbManga);
        cbComic = findViewById(R.id.cbComic);
        cbGame = findViewById(R.id.cbGame);
        cbVisualNovel = findViewById(R.id.cbVisualNovel);
        cbStoryGame = findViewById(R.id.cbStoryGame);

        radioReviewStatus = findViewById(R.id.radioReviewStatus);
        rbReviewAll = findViewById(R.id.rbReviewAll);
        rbReviewSudah = findViewById(R.id.rbReviewSudah);
        rbReviewBelum = findViewById(R.id.rbReviewBelum);

        radioSort = findViewById(R.id.radioSort);
        radioOrder = findViewById(R.id.radioOrder);
        rbSortTitle = findViewById(R.id.rbSortTitle);
        rbSortRatingArt = findViewById(R.id.rbSortRatingArt);
        rbSortReviewersArt = findViewById(R.id.rbSortReviewersArt);
        rbSortRatingCh = findViewById(R.id.rbSortRatingCh);
        rbSortChapters = findViewById(R.id.rbSortChapters);
        rbSortPublished = findViewById(R.id.rbSortPublished);
        rbSortRatingArtUser = findViewById(R.id.rbSortRatingArtUser);
        rbSortReviewersArtUser = findViewById(R.id.rbSortReviewersArtUser);
        rbSortRatingChUser = findViewById(R.id.rbSortRatingChUser);
        rbSortReviewersChUser = findViewById(R.id.rbSortReviewersChUser);
        rbOrderAsc = findViewById(R.id.rbOrderAsc);
        rbOrderDesc = findViewById(R.id.rbOrderDesc);

        etSearchFilter = findViewById(R.id.etSearchFilter);
        layoutSearchPreview = findViewById(R.id.layoutSearchPreview);
        containerSearchPreview = findViewById(R.id.containerSearchPreview);
        containerSelectedChips = findViewById(R.id.containerSelectedChips);

        etSearchFilter.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) {
                    layoutSearchPreview.setVisibility(View.GONE);
                    return;
                }
                showSearchPreview(query);
            }
        });

        layoutPagination = findViewById(R.id.layoutPagination);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        txtPageInfo = findViewById(R.id.txtPageInfo);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnOpenFilter.setOnClickListener(v -> toggleFilterPanel());

        btnApplyFilter.setOnClickListener(v -> {
            collectFilters();
            currentPage = 1;
            toggleFilterPanel();
            if (currentTab.equals("art")) loadMyArtList();
            else loadMyChapterList();
        });

        btnResetFilter.setOnClickListener(v -> resetFilters());

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                if (currentTab.equals("art")) loadMyArtList();
                else loadMyChapterList();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                if (currentTab.equals("art")) loadMyArtList();
                else loadMyChapterList();
            }
        });
    }

    private void toggleFilterPanel() {
        filterPanelOpen = !filterPanelOpen;
        layoutFilterPanel.setVisibility(filterPanelOpen ? View.VISIBLE : View.GONE);
        btnOpenFilter.setText(filterPanelOpen ? "✕ Tutup" : "⚙ Filter & Sort");
    }

    private void setupTabs() {
        btnTabMyArt.setOnClickListener(v -> {
            currentTab = "art";
            currentPage = 1;
            txtEmptyMyChapter.setVisibility(View.GONE);
            recyclerMyArt.setVisibility(View.VISIBLE);
            recyclerMyChapter.setVisibility(View.GONE);
            btnTabMyArt.setTextColor(getResources().getColor(R.color.white));
            btnTabMyArt.setBackgroundResource(R.drawable.button_gradient);
            btnTabMyChapter.setTextColor(getResources().getColor(R.color.primary));
            btnTabMyChapter.setBackgroundResource(R.drawable.chip_background);

            updateSortOptions("art");
            if (tokenManager.isLoggedIn()) loadMyArtList();
        });

        btnTabMyChapter.setOnClickListener(v -> {
            currentTab = "chapter";
            currentPage = 1;
            txtEmptyMyArt.setVisibility(View.GONE);
            recyclerMyArt.setVisibility(View.GONE);
            recyclerMyChapter.setVisibility(View.VISIBLE);
            btnTabMyArt.setTextColor(getResources().getColor(R.color.primary));
            btnTabMyArt.setBackgroundResource(R.drawable.chip_background);
            btnTabMyChapter.setTextColor(getResources().getColor(R.color.white));
            btnTabMyChapter.setBackgroundResource(R.drawable.button_gradient);
            updateSortOptions("chapter");
            if (tokenManager.isLoggedIn()) loadMyChapterList();
        });
    }

    private void updateSortOptions(String tab) {
        // ngapus sort yang tidak sesuai jenis art/chapter
        if (tab.equals("art")) {
            rbSortChapters.setVisibility(View.VISIBLE);
            rbSortRatingArtUser.setVisibility(View.VISIBLE);
            rbSortReviewersArtUser.setVisibility(View.VISIBLE);
        } else {
            int sortId = radioSort.getCheckedRadioButtonId();
            if (sortId == R.id.rbSortChapters) {
                rbSortTitle.setChecked(true);
                selectedSort = "title";
            }
        }
    }

    private void setupNav() {
        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));
        navDiscover.setOnClickListener(v ->
                startActivity(new Intent(this, DiscoverActivity.class)));
        navBookmark.setOnClickListener(v ->
                startActivity(new Intent(this, BookmarkActivity.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadFilterOptions() {
        filterService.getGenres().enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Genre g : response.body().data) {
                        allGenres.add(g.genre);
                        itemTypeMap.put(g.genre, "Genre");
                    }
                }
            }
            @Override public void onFailure(Call<GenreResponse> call, Throwable t) {}
        });

        filterService.getTags().enqueue(new Callback<TagResponse>() {
            @Override
            public void onResponse(Call<TagResponse> call, Response<TagResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Tag t : response.body().data) {
                        allTags.add(t.tag);
                        itemTypeMap.put(t.tag, "Tag");
                    }
                }
            }
            @Override public void onFailure(Call<TagResponse> call, Throwable t) {}
        });

        filterService.getMoods().enqueue(new Callback<MoodResponse>() {
            @Override
            public void onResponse(Call<MoodResponse> call, Response<MoodResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Mood m : response.body().data) {
                        allMoods.add(m.mood);
                        itemTypeMap.put(m.mood, "Mood");
                    }
                }
            }
            @Override public void onFailure(Call<MoodResponse> call, Throwable t) {}
        });
    }

    private void showSearchPreview(String query) {
        containerSearchPreview.removeAllViews();

        List<String[]> results = new ArrayList<>();

        for (String g : allGenres) {
            if (!selectedGenres.contains(g) && g.toLowerCase().contains(query))
                results.add(new String[]{g, "Genre"});
        }
        for (String t : allTags) {
            if (!selectedTags.contains(t) && t.toLowerCase().contains(query))
                results.add(new String[]{t, "Tag"});
        }
        for (String m : allMoods) {
            if (!selectedMoods.contains(m) && m.toLowerCase().contains(query))
                results.add(new String[]{m, "Mood"});
        }

        if (results.isEmpty()) {
            layoutSearchPreview.setVisibility(View.GONE);
            return;
        }

        for (int i = 0; i < results.size(); i++) {
            String nama = results.get(i)[0];
            String tipe = results.get(i)[1];

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(32, 24, 32, 24);

            LinearLayout labelWrapper = new LinearLayout(this);
            labelWrapper.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            labelWrapper.setLayoutParams(wrapperParams);

            TextView txtNama = new TextView(this);
            txtNama.setText(nama);
            txtNama.setTextSize(13f);
            txtNama.setTextColor(getResources().getColor(android.R.color.black));
            labelWrapper.addView(txtNama);

            TextView txtTipe = new TextView(this);
            txtTipe.setText(tipe);
            txtTipe.setTextSize(10f);
            txtTipe.setTextColor(getResources().getColor(R.color.primary));
            labelWrapper.addView(txtTipe);

            row.addView(labelWrapper);

            TextView btnAdd = new TextView(this);
            btnAdd.setText("+");
            btnAdd.setTextSize(20f);
            btnAdd.setTextColor(getResources().getColor(R.color.primary));
            btnAdd.setPadding(16, 0, 8, 0);
            btnAdd.setOnClickListener(v -> {
                addSelectedChip(nama, tipe);
                etSearchFilter.setText("");
                layoutSearchPreview.setVisibility(View.GONE);
            });
            row.addView(btnAdd);

            containerSearchPreview.addView(row);

            if (i < results.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divParams.setMargins(32, 0, 32, 0);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(0xFFEEEEEE);
                containerSearchPreview.addView(divider);
            }
        }

        layoutSearchPreview.setVisibility(View.VISIBLE);
    }

    private void addSelectedChip(String nama, String tipe) {
        switch (tipe) {
            case "Genre": if (!selectedGenres.contains(nama)) selectedGenres.add(nama); break;
            case "Tag":   if (!selectedTags.contains(nama))   selectedTags.add(nama);   break;
            case "Mood":  if (!selectedMoods.contains(nama))  selectedMoods.add(nama);  break;
        }

        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setBackgroundResource(R.drawable.chip_background);
        chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
        chip.setPadding(20, 8, 8, 8);

        com.google.android.flexbox.FlexboxLayout.LayoutParams chipParams =
                new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                        com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT);
        chipParams.setMargins(0, 0, 12, 12);
        chip.setLayoutParams(chipParams);

        TextView txtChip = new TextView(this);
        txtChip.setText(nama);
        txtChip.setTextSize(12f);
        txtChip.setTextColor(getResources().getColor(R.color.primary));
        chip.addView(txtChip);

        TextView btnRemove = new TextView(this);
        btnRemove.setText(" ✕");
        btnRemove.setTextSize(11f);
        btnRemove.setTextColor(getResources().getColor(R.color.primary));
        btnRemove.setPadding(4, 0, 12, 0);
        btnRemove.setOnClickListener(v -> {
            switch (tipe) {
                case "Genre": selectedGenres.remove(nama); break;
                case "Tag":   selectedTags.remove(nama);   break;
                case "Mood":  selectedMoods.remove(nama);  break;
            }
            containerSelectedChips.removeView(chip);
        });
        chip.addView(btnRemove);

        containerSelectedChips.addView(chip);
    }

    private void buildFilterChips(com.google.android.flexbox.FlexboxLayout container,
                                  List<String> items, List<String> selectedList) {
        runOnUiThread(() -> {
            container.removeAllViews();
            for (String item : items) {
                Button chip = new Button(this);
                chip.setText(item);
                chip.setTextSize(11f);
                chip.setBackgroundResource(R.drawable.chip_background);
                chip.setTextColor(getResources().getColor(R.color.primary));

                com.google.android.flexbox.FlexboxLayout.LayoutParams params =
                        new com.google.android.flexbox.FlexboxLayout.LayoutParams(
                                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                com.google.android.flexbox.FlexboxLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 12, 12);
                chip.setLayoutParams(params);

                chip.setOnClickListener(v -> {
                    if (selectedList.contains(item)) {
                        selectedList.remove(item);
                        chip.setBackgroundResource(R.drawable.chip_background);
                        chip.setTextColor(getResources().getColor(R.color.primary));
                    } else {
                        selectedList.add(item);
                        chip.setBackgroundResource(R.drawable.button_gradient);
                        chip.setTextColor(getResources().getColor(R.color.white));
                    }
                });

                container.addView(chip);
            }
        });
    }

    private void collectFilters() {
        selectedCategories.clear();
        if (cbBook.isChecked()) selectedCategories.add("Book");
        if (cbNovel.isChecked()) selectedCategories.add("Novel");
        if (cbManhwa.isChecked()) selectedCategories.add("Manhwa");
        if (cbManga.isChecked()) selectedCategories.add("Manga");
        if (cbComic.isChecked()) selectedCategories.add("Comic");
        if (cbGame.isChecked()) selectedCategories.add("Game");
        if (cbVisualNovel.isChecked()) selectedCategories.add("Visual Novel");
        if (cbStoryGame.isChecked()) selectedCategories.add("Story Game");

        int reviewId = radioReviewStatus.getCheckedRadioButtonId();
        if (reviewId == R.id.rbReviewSudah) selectedReviewStatus = "sudah";
        else if (reviewId == R.id.rbReviewBelum) selectedReviewStatus = "belum";
        else selectedReviewStatus = "all";

        int sortId = radioSort.getCheckedRadioButtonId();
        if (sortId == R.id.rbSortTitle) selectedSort = "title";
        else if (sortId == R.id.rbSortRatingArt) selectedSort = "ratingArt";
        else if (sortId == R.id.rbSortReviewersArt) selectedSort = "reviewersArt";
        else if (sortId == R.id.rbSortRatingCh) selectedSort = "ratingChapter";
        else if (sortId == R.id.rbSortChapters) selectedSort = "chapters";
        else if (sortId == R.id.rbSortPublished) selectedSort = "published";
        else if (sortId == R.id.rbSortRatingArtUser) selectedSort = "ratingArtUser";
        else if (sortId == R.id.rbSortReviewersArtUser) selectedSort = "reviewersArtUser";
        else if (sortId == R.id.rbSortRatingChUser) selectedSort = "ratingChapterUser";
        else if (sortId == R.id.rbSortReviewersChUser) selectedSort = "reviewersChapterUser";

        int orderId = radioOrder.getCheckedRadioButtonId();
        selectedOrder = (orderId == R.id.rbOrderDesc) ? "DESC" : "ASC";
    }

    private void resetFilters() {
        selectedCategories.clear();
        selectedGenres.clear();
        selectedTags.clear();
        selectedMoods.clear();
        selectedSort = "title";
        selectedOrder = "ASC";
        selectedReviewStatus = "all";

        cbBook.setChecked(false); cbNovel.setChecked(false);
        cbManhwa.setChecked(false); cbManga.setChecked(false);
        cbComic.setChecked(false); cbGame.setChecked(false);
        cbVisualNovel.setChecked(false); cbStoryGame.setChecked(false);
        rbReviewAll.setChecked(true);
        rbSortTitle.setChecked(true);
        rbOrderAsc.setChecked(true);

        containerSelectedChips.removeAllViews();
        etSearchFilter.setText("");
        layoutSearchPreview.setVisibility(View.GONE);

        currentPage = 1;
        if (currentTab.equals("art")) loadMyArtList();
        else loadMyChapterList();
    }

    private void resetChips(com.google.android.flexbox.FlexboxLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof Button) {
                child.setBackgroundResource(R.drawable.chip_background);
                ((Button) child).setTextColor(getResources().getColor(R.color.primary));
            }
        }
    }

    private void loadMyArtList() {
        String category = selectedCategories.isEmpty() ? null :
                String.join(",", selectedCategories);
        String genre = selectedGenres.isEmpty() ? null :
                String.join(",", selectedGenres);
        String tag = selectedTags.isEmpty() ? null :
                String.join(",", selectedTags);
        String mood = selectedMoods.isEmpty() ? null :
                String.join(",", selectedMoods);
        String sudahDiReview = selectedReviewStatus.equals("sudah") ? "true" :
                selectedReviewStatus.equals("belum") ? "false" : null;

        artService.getArts(currentPage, 20, category, genre, tag, mood,
                        selectedSort, selectedOrder, sudahDiReview)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            totalPages = response.body().meta != null ?
                                    response.body().meta.total_pages : 1;

                            if (response.body().data == null || response.body().data.isEmpty()) {
                                recyclerMyArt.setVisibility(View.GONE);
                                txtEmptyMyArt.setVisibility(View.VISIBLE);

                                txtEmptyMyChapter.setVisibility(View.GONE);
                                return;
                            } else {
                                recyclerMyArt.setVisibility(View.VISIBLE);
                                txtEmptyMyArt.setVisibility(View.GONE);
                            }

                            updatePagination();

                            myArtAdapter = new MyArtListAdapter(
                                    MyListActivity.this,
                                    response.body().data,
                                    art -> {
                                        Intent intent = new Intent(MyListActivity.this,
                                                DetailContentActivity.class);
                                        intent.putExtra("art_id", art.id);
                                        startActivity(intent);
                                    });
                            recyclerMyArt.setAdapter(myArtAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {
                        Toast.makeText(MyListActivity.this,
                                "Gagal load data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyChapterList() {
        String category = selectedCategories.isEmpty() ? null :
                String.join(",", selectedCategories);
        String genre = selectedGenres.isEmpty() ? null :
                String.join(",", selectedGenres);
        String tag = selectedTags.isEmpty() ? null :
                String.join(",", selectedTags);
        String mood = selectedMoods.isEmpty() ? null :
                String.join(",", selectedMoods);

        artService.getChapterList(currentPage, 20, category, genre, tag, mood,
                        selectedSort, selectedOrder)
                .enqueue(new Callback<ChapterListResponse>() {
                    @Override
                    public void onResponse(Call<ChapterListResponse> call,
                                           Response<ChapterListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            totalPages = response.body().meta != null ?
                                    response.body().meta.total_pages : 1;

                            if (response.body().data == null || response.body().data.isEmpty()) {
                                recyclerMyChapter.setVisibility(View.GONE);
                                txtEmptyMyChapter.setVisibility(View.VISIBLE);

                                txtEmptyMyArt.setVisibility(View.GONE);
                                return;
                            } else {
                                recyclerMyChapter.setVisibility(View.VISIBLE);
                                txtEmptyMyChapter.setVisibility(View.GONE);
                            }

                            updatePagination();

                            myChapterAdapter = new MyChapterListAdapter(
                                    MyListActivity.this,
                                    response.body().data,
                                    chapter -> {
                                        Intent intent = new Intent(MyListActivity.this,
                                                ChapterReaderActivity.class);
                                        intent.putExtra("art_id", chapter.artId);
                                        intent.putExtra("chapter_number", chapter.chapterNumber);
                                        intent.putExtra("chapter_id", chapter.id);
                                        intent.putExtra("art_title", chapter.artTitle);
                                        startActivity(intent);
                                    });
                            recyclerMyChapter.setAdapter(myChapterAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ChapterListResponse> call, Throwable t) {}
                });
    }

    private void updatePagination() {
        txtPageInfo.setText("Halaman " + currentPage + " / " + totalPages);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
    }
}