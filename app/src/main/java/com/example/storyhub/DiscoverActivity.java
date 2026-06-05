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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.ArtAdapter;
import com.example.storyhub.adapters.ChapterListAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.FilterApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.ChapterListResponse;
import com.example.storyhub.models.Genre;
import com.example.storyhub.models.GenreResponse;
import com.example.storyhub.models.Mood;
import com.example.storyhub.models.MoodResponse;
import com.example.storyhub.models.Tag;
import com.example.storyhub.models.TagResponse;
import com.example.storyhub.utils.TokenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoverActivity extends AppCompatActivity {

    // nav bar
    LinearLayout navHome, navDiscover, navMyList, navBookmark, navProfile;

    // tab
    Button btnTabArt, btnTabChapter;
    RecyclerView recyclerArts, recyclerChapters;
    ArtAdapter artAdapter;
    ChapterListAdapter chapterAdapter;

    // filter panel
    LinearLayout layoutFilterPanel;
    Button btnOpenFilter, btnApplyFilter, btnResetFilter;

    // filter category
    CheckBox cbBook, cbNovel, cbManhwa, cbManga, cbComic,
            cbGame, cbVisualNovel, cbStoryGame;

    // filter sort
    RadioGroup radioSort, radioOrder;
    RadioButton rbSortTitle, rbSortRatingArt, rbSortReviewersArt,
            rbSortRatingCh, rbSortChapters, rbSortPublished;
    RadioButton rbOrderAsc, rbOrderDesc;

    // filter genre, tag, mood
    com.google.android.flexbox.FlexboxLayout containerFilterGenre, containerFilterTag, containerFilterMood;

    // perhalamanan
    LinearLayout layoutPagination;
    Button btnPrevPage, btnNextPage;
    TextView txtPageInfo;
    TextView txtEmptyArt, txtEmptyChapter;

    // state pilihan (selected)
    int currentPage = 1;
    int totalPages = 1;
    String currentTab = "art";

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
    boolean filterPanelOpen = false;

    TokenManager tokenManager;
    ArtApiService artService;
    FilterApiService filterService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);
        filterService = RetrofitClient.getInstance().create(FilterApiService.class);

        initViews();
        setupNav();
        setupTabs();
        loadFilterOptions();
        loadArts();
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navDiscover = findViewById(R.id.navDiscover);
        navMyList = findViewById(R.id.navMyList);
        navBookmark = findViewById(R.id.navBookmark);
        navProfile = findViewById(R.id.navProfile);

        btnTabArt = findViewById(R.id.btnTabArt);
        btnTabChapter = findViewById(R.id.btnTabChapter);

        txtEmptyArt = findViewById(R.id.txtEmptyArt);
        txtEmptyChapter = findViewById(R.id.txtEmptyChapter);

        recyclerArts = findViewById(R.id.recyclerArts);

        if (recyclerArts != null) {
            recyclerArts.setLayoutManager(new GridLayoutManager(this, 2));
        }

        recyclerChapters = findViewById(R.id.recyclerChapters);

        if (recyclerChapters != null) {
            recyclerChapters.setLayoutManager(new LinearLayoutManager(this));
        }

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

        radioSort = findViewById(R.id.radioSort);
        radioOrder = findViewById(R.id.radioOrder);
        rbSortTitle = findViewById(R.id.rbSortTitle);
        rbSortRatingArt = findViewById(R.id.rbSortRatingArt);
        rbSortReviewersArt = findViewById(R.id.rbSortReviewersArt);
        rbSortRatingCh = findViewById(R.id.rbSortRatingCh);
        rbSortChapters = findViewById(R.id.rbSortChapters);
        rbSortPublished = findViewById(R.id.rbSortPublished);
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

        btnOpenFilter.setOnClickListener(v -> toggleFilterPanel());

        btnApplyFilter.setOnClickListener(v -> {
            collectFilters();
            currentPage = 1;
            toggleFilterPanel();
            if (currentTab.equals("art")) loadArts();
            else loadChapters();
        });

        btnResetFilter.setOnClickListener(v -> resetFilters());

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                if (currentTab.equals("art")) loadArts();
                else loadChapters();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                if (currentTab.equals("art")) loadArts();
                else loadChapters();
            }
        });

        // search bar
        findViewById(R.id.searchBar).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));
    }

    private void toggleFilterPanel() {
        filterPanelOpen = !filterPanelOpen;
        layoutFilterPanel.setVisibility(filterPanelOpen ? View.VISIBLE : View.GONE);
        btnOpenFilter.setText(filterPanelOpen ? "✕ Tutup Filter" : "⚙ Filter & Sort");
    }

    private void setupTabs() {
        btnTabArt.setOnClickListener(v -> {
            currentTab = "art";
            currentPage = 1;
            txtEmptyChapter.setVisibility(View.GONE);
            recyclerArts.setVisibility(View.VISIBLE);
            recyclerChapters.setVisibility(View.GONE);
            btnTabArt.setTextColor(getResources().getColor(R.color.primary));
            btnTabChapter.setTextColor(getResources().getColor(R.color.black));
            loadArts();
        });

        btnTabChapter.setOnClickListener(v -> {
            currentTab = "chapter";
            currentPage = 1;
            txtEmptyArt.setVisibility(View.GONE);
            recyclerArts.setVisibility(View.GONE);
            recyclerChapters.setVisibility(View.VISIBLE);
            btnTabArt.setTextColor(getResources().getColor(R.color.black));
            btnTabChapter.setTextColor(getResources().getColor(R.color.primary));
            loadChapters();
        });
    }

    private void setupNav() {
        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        navMyList.setOnClickListener(v ->
                startActivity(new Intent(this, MyListActivity.class)));

        navBookmark.setOnClickListener(v ->
                startActivity(new Intent(this, BookmarkActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
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

    private void collectFilters() {
        selectedCategories.clear();
        if (cbBook != null && cbBook.isChecked()) selectedCategories.add("Book");
        if (cbNovel != null && cbNovel.isChecked()) selectedCategories.add("Novel");
        if (cbManhwa != null && cbManhwa.isChecked()) selectedCategories.add("Manhwa");
        if (cbManga != null && cbManga.isChecked()) selectedCategories.add("Manga");
        if (cbComic != null && cbComic.isChecked()) selectedCategories.add("Comic");
        if (cbGame != null && cbGame.isChecked()) selectedCategories.add("Game");
        if (cbVisualNovel != null && cbVisualNovel.isChecked()) selectedCategories.add("Visual Novel");
        if (cbStoryGame != null && cbStoryGame.isChecked()) selectedCategories.add("Story Game");

        int sortId = radioSort.getCheckedRadioButtonId();
        if (sortId == R.id.rbSortTitle) selectedSort = "title";
        else if (sortId == R.id.rbSortRatingArt) selectedSort = "ratingArt";
        else if (sortId == R.id.rbSortReviewersArt) selectedSort = "reviewersArt";
        else if (sortId == R.id.rbSortRatingCh) selectedSort = "ratingChapter";
        else if (sortId == R.id.rbSortChapters) selectedSort = "chapters";
        else if (sortId == R.id.rbSortPublished) selectedSort = "published";

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

        cbBook.setChecked(false); cbNovel.setChecked(false);
        cbManhwa.setChecked(false); cbManga.setChecked(false);
        cbComic.setChecked(false); cbGame.setChecked(false);
        cbVisualNovel.setChecked(false); cbStoryGame.setChecked(false);

        rbSortTitle.setChecked(true);
        rbOrderAsc.setChecked(true);

        containerSelectedChips.removeAllViews();
        etSearchFilter.setText("");
        layoutSearchPreview.setVisibility(View.GONE);

        currentPage = 1;
        if (currentTab.equals("art")) loadArts();
        else loadChapters();
    }

    private void loadArts() {
        String category = selectedCategories.isEmpty() ? null :
                String.join(",", selectedCategories);
        String genre = selectedGenres.isEmpty() ? null :
                String.join(",", selectedGenres);
        String tag = selectedTags.isEmpty() ? null :
                String.join(",", selectedTags);
        String mood = selectedMoods.isEmpty() ? null :
                String.join(",", selectedMoods);

        artService.getArts(currentPage, 20, category, genre, tag, mood, selectedSort, selectedOrder, null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call, Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().meta != null) {
                                totalPages = response.body().meta.total_pages;
                            }

                            if (response.body().data == null || response.body().data.isEmpty()) {

                                recyclerArts.setVisibility(View.GONE);

                                txtEmptyArt.setVisibility(View.VISIBLE);
                                txtEmptyChapter.setVisibility(View.GONE);

                                updatePagination();

                                return;
                            } else {

                                recyclerArts.setVisibility(View.VISIBLE);

                                txtEmptyArt.setVisibility(View.GONE);
                                txtEmptyChapter.setVisibility(View.GONE);
                            }

                            updatePagination();

                            artAdapter = new ArtAdapter(DiscoverActivity.this,
                                    response.body().data, art -> {
                                Intent intent = new Intent(DiscoverActivity.this,
                                        DetailContentActivity.class);
                                intent.putExtra("art_id", art.id);
                                startActivity(intent);
                            });
                            recyclerArts.setAdapter(artAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {
                        Toast.makeText(DiscoverActivity.this,
                                "Gagal load data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadChapters() {
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
                            if (response.body().meta != null) {
                                totalPages = response.body().meta.total_pages;
                            }

                            if (response.body().data == null || response.body().data.isEmpty()) {

                                recyclerChapters.setVisibility(View.GONE);

                                txtEmptyChapter.setVisibility(View.VISIBLE);
                                txtEmptyArt.setVisibility(View.GONE);

                                updatePagination();

                                return;
                            } else {

                                recyclerChapters.setVisibility(View.VISIBLE);

                                txtEmptyChapter.setVisibility(View.GONE);
                                txtEmptyArt.setVisibility(View.GONE);
                            }

                            updatePagination();

                            chapterAdapter = new ChapterListAdapter(DiscoverActivity.this,
                                    response.body().data, chapter -> {
                                Intent intent = new Intent(DiscoverActivity.this,
                                        ChapterReaderActivity.class);
                                intent.putExtra("art_id", chapter.artId);
                                intent.putExtra("chapter_number", chapter.chapterNumber);
                                intent.putExtra("chapter_id", chapter.id);
                                intent.putExtra("art_title", chapter.artTitle);
                                startActivity(intent);
                            });
                            recyclerChapters.setAdapter(chapterAdapter);
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