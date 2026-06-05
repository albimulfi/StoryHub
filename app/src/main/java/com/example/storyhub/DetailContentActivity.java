package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyhub.adapters.ChapterAdapter;
import com.example.storyhub.adapters.CommentAdapter;
import com.example.storyhub.adapters.ReviewAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.ReviewApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.UserApiService;
import com.example.storyhub.models.Art;
import com.example.storyhub.models.ArtDetailResponse;
import com.example.storyhub.models.ArtGenreTagMoodResponse;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.models.BookmarkResponse;
import com.example.storyhub.models.Chapter;
import com.example.storyhub.models.ChapterResponse;
import com.example.storyhub.models.Comment;
import com.example.storyhub.models.CommentRequest;
import com.example.storyhub.models.CommentResponse;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.Review;
import com.example.storyhub.models.ReviewRequest;
import com.example.storyhub.models.ReviewResponse;
import com.example.storyhub.models.SubscribeResponse;
import com.example.storyhub.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailContentActivity extends AppCompatActivity {

    ImageView imgBanner, imgCover, btnBack, btnBookmark;
    TextView txtTitle, txtTagline, txtRating, txtReviewers,
            txtChapters, txtSynopsis, txtAuthor, txtArtist,
            txtStatus, txtPublished, txtCategory;
    Button btnContinueReading, btnTabOverview, btnTabChapters, btnTabReviews;
    TextView txtEmptyReview, txtEmptyComment;
    LinearLayout layoutOverview, layoutChapters, layoutReviews;
    LinearLayout containerGenres, containerTags, containerMoods;
    LinearLayout layoutScreenshots, containerScreenshots;
    RecyclerView recyclerChapters, recyclerReviews;
    Button btnAddReview;

    Button btnTabComments;
    LinearLayout layoutComments;
    RecyclerView recyclerComments;
    Button btnAddComment;
    TextView txtTotalReviews, txtTotalComments;

    int artId;
    Art currentArt;
    boolean isBookmarked = false;
    TokenManager tokenManager;
    ArtApiService artService;
    ReviewApiService reviewService;
    boolean fromContinueReading = false;
    boolean isSubscribed = false;
    int latestChapterId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_content);

        tokenManager = new TokenManager(this);
        artId = getIntent().getIntExtra("art_id", -1);

        fromContinueReading = getIntent().getBooleanExtra("from_continue_reading", false);
        latestChapterId = getIntent().getIntExtra("latest_chapter_id", -1);

        if (artId == -1) {
            finish();
            return;
        }

        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);
        reviewService = RetrofitClient.getAuthInstance(tokenManager).create(ReviewApiService.class);

        initViews();
        setupTabs();
        loadArtDetail();
        loadGenreTagMood();
        checkSubscriptionThenLoadChapters();
        loadReviews();
        loadComments();
        checkBookmarkStatus();
    }

    private void initViews() {
        imgBanner = findViewById(R.id.imgBanner);
        imgCover = findViewById(R.id.imgCover);
        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);
        txtTitle = findViewById(R.id.txtTitle);
        txtTagline = findViewById(R.id.txtTagline);
        txtRating = findViewById(R.id.txtRating);
        txtReviewers = findViewById(R.id.txtReviewers);
        txtChapters = findViewById(R.id.txtChapters);
        txtSynopsis = findViewById(R.id.txtSynopsis);
        txtAuthor = findViewById(R.id.txtAuthor);
        txtArtist = findViewById(R.id.txtArtist);
        txtStatus = findViewById(R.id.txtStatus);
        txtPublished = findViewById(R.id.txtPublished);
        txtCategory = findViewById(R.id.txtCategory);
        txtEmptyReview = findViewById(R.id.txtEmptyReview);
        txtEmptyComment = findViewById(R.id.txtEmptyComment);

        containerGenres = findViewById(R.id.containerGenres);
        containerTags = findViewById(R.id.containerTags);
        containerMoods = findViewById(R.id.containerMoods);

        layoutScreenshots = findViewById(R.id.layoutScreenshots);
        containerScreenshots = findViewById(R.id.containerScreenshots);

        btnContinueReading = findViewById(R.id.btnContinueReading);
        btnTabOverview = findViewById(R.id.btnTabOverview);
        btnTabChapters = findViewById(R.id.btnTabChapters);
        btnTabReviews = findViewById(R.id.btnTabReviews);
        layoutOverview = findViewById(R.id.layoutOverview);
        layoutChapters = findViewById(R.id.layoutChapters);
        layoutReviews = findViewById(R.id.layoutReviews);
        recyclerChapters = findViewById(R.id.recyclerChapters);
        recyclerReviews = findViewById(R.id.recyclerReviews);
        btnAddReview = findViewById(R.id.btnAddReview);

        recyclerChapters.setLayoutManager(new LinearLayoutManager(this));
        recyclerChapters.setNestedScrollingEnabled(false);

        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());

        btnBookmark.setOnClickListener(v -> toggleBookmark());

        btnAddReview.setOnClickListener(v -> showReviewDialog());

        if (!tokenManager.isLoggedIn()) {
            btnAddReview.setVisibility(View.GONE);
        }

        btnTabComments = findViewById(R.id.btnTabComments);
        layoutComments = findViewById(R.id.layoutComments);
        recyclerComments = findViewById(R.id.recyclerComments);
        btnAddComment = findViewById(R.id.btnAddComment);
        txtTotalReviews = findViewById(R.id.txtTotalReviews);
        txtTotalComments = findViewById(R.id.txtTotalComments);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setNestedScrollingEnabled(false);

        btnAddComment.setOnClickListener(v -> showCommentDialog("arts", artId));

        if (!tokenManager.isLoggedIn()) {
            btnAddComment.setVisibility(View.GONE);
        }
    }

    private void checkSubscriptionThenLoadChapters() {
        if (!tokenManager.isLoggedIn()) {
            isSubscribed = false;
            loadChapters();
            return;
        }

        int userId = tokenManager.getUserId();
        UserApiService userService = RetrofitClient.getAuthInstance(tokenManager).create(UserApiService.class);
        userService.isSubscribe(userId).enqueue(new Callback<SubscribeResponse>() {
            @Override
            public void onResponse(Call<SubscribeResponse> call, Response<SubscribeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isSubscribed = response.body().isSubscribe;
                }
                loadChapters();
            }
            @Override
            public void onFailure(Call<SubscribeResponse> call, Throwable t) {
                loadChapters();
            }
        });
    }

    private void checkBookmarkStatus() {
        if (!tokenManager.isLoggedIn()) return;

        artService.getBookmarks("art").enqueue(new Callback<ArtResponse>() {
            @Override
            public void onResponse(Call<ArtResponse> call, Response<ArtResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    for (Art art : response.body().data) {
                        if (art.id == artId) {
                            isBookmarked = true;
                            btnBookmark.setColorFilter(
                                    getResources().getColor(R.color.primary));
                            return;
                        }
                    }
                    isBookmarked = false;
                    btnBookmark.setColorFilter(
                            getResources().getColor(android.R.color.white));
                }
            }

            @Override
            public void onFailure(Call<ArtResponse> call, Throwable t) {}
        });
    }

    private void setupTabs() {
        showTab("overview");

        btnTabOverview.setOnClickListener(v -> showTab("overview"));
        btnTabChapters.setOnClickListener(v -> showTab("chapters"));
        btnTabReviews.setOnClickListener(v -> showTab("reviews"));
        btnTabComments.setOnClickListener(v -> showTab("comments"));
    }

    private void showTab(String tab) {
        layoutOverview.setVisibility(tab.equals("overview") ? View.VISIBLE : View.GONE);
        layoutChapters.setVisibility(tab.equals("chapters") ? View.VISIBLE : View.GONE);
        layoutReviews.setVisibility(tab.equals("reviews") ? View.VISIBLE : View.GONE);
        layoutComments.setVisibility(tab.equals("comments") ? View.VISIBLE : View.GONE);

        setTabStyle(btnTabOverview, tab.equals("overview"));
        setTabStyle(btnTabChapters, tab.equals("chapters"));
        setTabStyle(btnTabReviews, tab.equals("reviews"));
        setTabStyle(btnTabComments, tab.equals("comments"));
    }

    private void setTabStyle(Button button, boolean active) {

        if (active) {
            button.setBackgroundResource(R.drawable.button_gradient);
            button.setTextColor(getResources().getColor(R.color.white));
        } else {
            button.setBackgroundResource(R.drawable.chip_background);
            button.setTextColor(getResources().getColor(R.color.primary));
        }
    }

    private void loadArtDetail() {
        artService.getArtById(artId).enqueue(new Callback<ArtDetailResponse>() {
            @Override
            public void onResponse(Call<ArtDetailResponse> call, Response<ArtDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    Art art = response.body().data;
                    currentArt = art;

                    txtTitle.setText(art.title);
                    txtTagline.setText(art.tagline);
                    txtCategory.setText(art.category);
                    txtRating.setText("⭐ " + String.format("%.1f", art.rata2Rating));
                    txtReviewers.setText("👥 " + art.totalReviews + " Reviews");
                    txtChapters.setText("📖 " + (art.jumlahChapter != null ? art.jumlahChapter : 0) + " Chapters");
                    txtSynopsis.setText(art.synopsis);
                    txtAuthor.setText(art.authorOrDev);
                    txtArtist.setText(art.artist != null ? art.artist : "-");
                    txtStatus.setText(art.status);
                    txtPublished.setText(art.publishedAt);

                    Glide.with(DetailContentActivity.this)
                            .load(RetrofitClient.IMAGE_BASE_URL + art.bannerImg)
                            .placeholder(R.drawable.placeholder)
                            .into(imgBanner);

                    Glide.with(DetailContentActivity.this)
                            .load(RetrofitClient.IMAGE_BASE_URL + art.coverImg)
                            .placeholder(R.drawable.placeholder)
                            .into(imgCover);

                    String cat = art.category != null ? art.category.toLowerCase() : "";
                    boolean isGame = cat.equals("game") || cat.equals("story game") || cat.equals("visual novel");

                    if (isGame) {
                        btnTabChapters.setVisibility(View.GONE);
                        layoutChapters.setVisibility(View.GONE);
                        txtChapters.setVisibility(View.GONE);
                        loadScreenshots(art);

                        btnContinueReading.setText("▶ Play Now");

                        btnContinueReading.setOnClickListener(v -> {
                            if (art.playUrl != null && !art.playUrl.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        android.net.Uri.parse(art.playUrl));
                                startActivity(intent);
                            } else {
                                Toast.makeText(DetailContentActivity.this,
                                        "Link belum tersedia.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    Toast.makeText(DetailContentActivity.this,
                            "Art tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ArtDetailResponse> call, Throwable t) {
                Toast.makeText(DetailContentActivity.this,
                        "Gagal load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGenreTagMood() {
        // genre2
        artService.getArtGenres(artId).enqueue(new Callback<ArtGenreTagMoodResponse>() {
            @Override
            public void onResponse(Call<ArtGenreTagMoodResponse> call,
                                   Response<ArtGenreTagMoodResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    containerGenres.removeAllViews();
                    for (ArtGenreTagMoodResponse.Item item : response.body().data) {
                        addChip(containerGenres, item.genre);
                    }
                }
            }
            @Override
            public void onFailure(Call<ArtGenreTagMoodResponse> call, Throwable t) {}
        });

        // tag2
        artService.getArtTags(artId).enqueue(new Callback<ArtGenreTagMoodResponse>() {
            @Override
            public void onResponse(Call<ArtGenreTagMoodResponse> call,
                                   Response<ArtGenreTagMoodResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    if (containerTags != null) containerTags.removeAllViews();
                    for (ArtGenreTagMoodResponse.Item item : response.body().data) {
                        if (containerTags != null) addChip(containerTags, "#" + item.tag);
                    }
                }
            }
            @Override
            public void onFailure(Call<ArtGenreTagMoodResponse> call, Throwable t) {}
        });

        // mood2
        artService.getArtMoods(artId).enqueue(new Callback<ArtGenreTagMoodResponse>() {
            @Override
            public void onResponse(Call<ArtGenreTagMoodResponse> call,
                                   Response<ArtGenreTagMoodResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    if (containerMoods != null) containerMoods.removeAllViews();
                    for (ArtGenreTagMoodResponse.Item item : response.body().data) {
                        if (containerMoods != null) addChip(containerMoods, item.mood);
                    }
                }
            }
            @Override
            public void onFailure(Call<ArtGenreTagMoodResponse> call, Throwable t) {}
        });
    }

    private void loadScreenshots(Art art) {
        List<String> shots = new ArrayList<>();
        if (art.ss1Img != null && !art.ss1Img.isEmpty()) shots.add(art.ss1Img);
        if (art.ss2Img != null && !art.ss2Img.isEmpty()) shots.add(art.ss2Img);
        if (art.ss3Img != null && !art.ss3Img.isEmpty()) shots.add(art.ss3Img);

        if (shots.isEmpty()) return;

        layoutScreenshots.setVisibility(View.VISIBLE);
        containerScreenshots.removeAllViews();

        for (String ssUrl : shots) {
            ImageView img = new ImageView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (200 * getResources().getDisplayMetrics().density),
                    (int) (130 * getResources().getDisplayMetrics().density)
            );
            params.setMarginEnd((int) (10 * getResources().getDisplayMetrics().density));
            img.setLayoutParams(params);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setBackgroundResource(R.drawable.card_background);

            Glide.with(this)
                    .load(RetrofitClient.IMAGE_BASE_URL + ssUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(img);

            img.setOnClickListener(v -> showFullscreenImage(ssUrl));

            containerScreenshots.addView(img);
        }
    }

    private void showFullscreenImage(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);

        FrameLayout imageWrapper = new FrameLayout(this);
        FrameLayout.LayoutParams wrapperParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        imageWrapper.setLayoutParams(wrapperParams);

        ImageView imgFull = new ImageView(this);
        FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        imgParams.gravity = android.view.Gravity.CENTER;
        imgFull.setLayoutParams(imgParams);
        imgFull.setAdjustViewBounds(true);
        imgFull.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(this)
                .load(RetrofitClient.IMAGE_BASE_URL + imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(imgFull);

        TextView btnClose = new TextView(this);
        btnClose.setText("✕");
        btnClose.setTextColor(0xFFFFFFFF);
        btnClose.setTextSize(22f);
        btnClose.setTypeface(null, android.graphics.Typeface.BOLD);

        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        closeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
        closeParams.setMargins(
                (int)(20 * getResources().getDisplayMetrics().density),
                (int)(40 * getResources().getDisplayMetrics().density),
                0, 0
        );
        btnClose.setLayoutParams(closeParams);
        btnClose.setPadding(8, 8, 8, 8);

        imageWrapper.addView(imgFull);
        root.addView(imageWrapper);
        root.addView(btnClose);

        builder.setView(root);
        AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        }
    }

    private void addChip(LinearLayout container, String text) {
        if (text == null || text.isEmpty()) return;

        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextSize(12f);
        chip.setTextColor(getResources().getColor(R.color.primary));
        chip.setBackgroundResource(R.drawable.chip_background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(8);
        chip.setPadding(16, 8, 16, 8);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            String type;
            if (container.getId() == R.id.containerGenres) type = "genre";
            else if (container.getId() == R.id.containerTags) type = "tag";
            else if (container.getId() == R.id.containerMoods) type = "mood";
            else return;

            String value = text.startsWith("#") ? text.substring(1) : text;

            Intent intent = new Intent(this, FilterResultActivity.class);
            intent.putExtra("filter_type", type);
            intent.putExtra("filter_value", value);
            startActivity(intent);
        });

        container.addView(chip);
    }

    private void loadChapters() {
        artService.getChapters(artId, "Published").enqueue(new Callback<ChapterResponse>() {
            @Override
            public void onResponse(Call<ChapterResponse> call, Response<ChapterResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    List<Chapter> chapters = response.body().data;

                    if (chapters != null && !chapters.isEmpty()) {
                        ChapterAdapter adapter = new ChapterAdapter(chapters, chapter -> {

                            if (chapter.isPremium == 1 && !isSubscribed) {
                                Toast.makeText(DetailContentActivity.this,
                                        "Kamu harus berlangganan untuk membuka chapter ini.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Intent intent = new Intent(DetailContentActivity.this, ChapterReaderActivity.class);
                            intent.putExtra("art_id", artId);
                            intent.putExtra("chapter_number", chapter.chapterNumber);
                            intent.putExtra("chapter_id", chapter.id);
                            intent.putExtra("art_title", currentArt != null ? currentArt.title : "");
                            startActivity(intent);
                        }, isSubscribed);

                        recyclerChapters.setAdapter(adapter);

                        if (txtChapters != null) {
                            txtChapters.setText("📖 " + chapters.size() + " Chapters");
                        }

                        if (btnContinueReading.getText().toString().contains("Play")) return;

                        Chapter targetChapter = chapters.get(0);
                        if (fromContinueReading && latestChapterId != -1) {
                            for (Chapter ch : chapters) {
                                if (ch.id == latestChapterId) {
                                    targetChapter = ch;
                                    break;
                                }
                            }
                        }

                        final Chapter chapterToOpen = targetChapter;

                        btnContinueReading.setOnClickListener(v -> {
                            Intent intent = new Intent(DetailContentActivity.this, ChapterReaderActivity.class);
                            intent.putExtra("art_id", artId);
                            intent.putExtra("chapter_number", chapterToOpen.chapterNumber);
                            intent.putExtra("chapter_id", chapterToOpen.id);
                            intent.putExtra("art_title", currentArt != null ? currentArt.title : "");
                            startActivity(intent);
                        });

                        if (fromContinueReading) {
                            fromContinueReading = false;
                            runOnUiThread(() -> btnContinueReading.performClick());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ChapterResponse> call, Throwable t) {
                Toast.makeText(DetailContentActivity.this, "Gagal load chapters.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReviews() {
        reviewService.getReviews("arts", artId).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    List<Review> reviews = response.body().data;

                    txtTotalReviews.setText(response.body().totalReviews + " Review");

                    txtEmptyReview.setVisibility(View.GONE);
                    recyclerReviews.setVisibility(View.VISIBLE);

                    ReviewAdapter adapter =
                            new ReviewAdapter(DetailContentActivity.this, reviews, "arts", artId);

                    recyclerReviews.setAdapter(adapter);

                } else {

                    txtTotalReviews.setText("0 Review");
                    recyclerReviews.setAdapter(null);
                    recyclerReviews.setVisibility(View.GONE);
                    txtEmptyReview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {}
        });
    }

    private void loadComments() {
        reviewService.getComments("arts", artId).enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    List<Comment> comments = response.body().data;

                    int total = response.body().totalComments != null
                            ? response.body().totalComments.total_comments
                            : comments.size();

                    txtTotalComments.setText(total + " Komentar");

                    txtEmptyComment.setVisibility(View.GONE);
                    recyclerComments.setVisibility(View.VISIBLE);

                    CommentAdapter adapter =
                            new CommentAdapter(DetailContentActivity.this, comments, "arts", artId);

                    recyclerComments.setAdapter(adapter);

                } else {

                    txtTotalComments.setText("0 Komentar");
                    recyclerComments.setAdapter(null);
                    recyclerComments.setVisibility(View.GONE);
                    txtEmptyComment.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {}
        });
    }

    private void toggleBookmark() {
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Login dulu untuk bookmark.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBookmarked) {
            artService.removeBookmarkArt(artId).enqueue(new Callback<BookmarkResponse>() {
                @Override
                public void onResponse(Call<BookmarkResponse> call, Response<BookmarkResponse> response) {
                    if (response.isSuccessful()) {
                        isBookmarked = false;
                        btnBookmark.setColorFilter(
                                getResources().getColor(android.R.color.white));
                        Toast.makeText(DetailContentActivity.this,
                                "Dihapus dari bookmark.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BookmarkResponse> call, Throwable t) {}
            });
        } else {
            artService.addBookmarkArt(artId).enqueue(new Callback<BookmarkResponse>() {
                @Override
                public void onResponse(Call<BookmarkResponse> call, Response<BookmarkResponse> response) {
                    if (response.isSuccessful()) {
                        isBookmarked = true;
                        btnBookmark.setColorFilter(
                                getResources().getColor(R.color.primary));
                        Toast.makeText(DetailContentActivity.this,
                                "Ditambah ke bookmark!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BookmarkResponse> call, Throwable t) {}
            });
        }
    }

    private void showCommentDialog(String targetType, int targetId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tulis Komentar");

        EditText etInput = new EditText(this);
        etInput.setHint("Tulis komentarmu...");
        etInput.setPadding(40, 20, 40, 20);
        builder.setView(etInput);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String text = etInput.getText().toString().trim();
            if (text.isEmpty()) return;

            reviewService.postComment(targetType, targetId, new CommentRequest(text))
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call,
                                               Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DetailContentActivity.this,
                                        "Komentar berhasil dikirim!", Toast.LENGTH_SHORT).show();
                                loadComments();
                            }
                        }
                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tulis Review");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        RatingBar ratingBarTop = dialogView.findViewById(R.id.ratingBarTop);
        RatingBar ratingBarBottom = dialogView.findViewById(R.id.ratingBarBottom);
        EditText etKomentar = dialogView.findViewById(R.id.etKomentar);

        ratingBarTop.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

            if (!fromUser) return;

            ratingBarBottom.setRating(0);
        });

        ratingBarBottom.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

            if (!fromUser) return;

            if (rating > 0) {
                ratingBarTop.setRating(5);
            }
        });

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            int rating = (int) (
                    ratingBarTop.getRating() + ratingBarBottom.getRating()
            );
            String komentar = etKomentar.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Rating harus diisi (1-10).", Toast.LENGTH_SHORT).show();
                return;
            }

            reviewService.postReview("arts", artId, new ReviewRequest(rating, komentar))
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DetailContentActivity.this,
                                        "Review berhasil dikirim!", Toast.LENGTH_SHORT).show();
                                loadReviews();
                            } else if (response.code() == 403) {
                                Toast.makeText(DetailContentActivity.this,
                                        "Kamu sudah pernah review ini.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    public void setArtData(Art art) {
        this.currentArt = art;

        txtTitle.setText(art.title);
        txtTagline.setText(art.tagline);
        txtCategory.setText(art.category);
        txtRating.setText("⭐ " + String.format("%.1f", art.rata2Rating));
        txtReviewers.setText("👥 " + art.totalReviews);
        txtChapters.setText("📖 " + (art.jumlahChapter != null ? art.jumlahChapter : 0) + " Chapters");
        txtSynopsis.setText(art.synopsis);
        txtAuthor.setText(art.authorOrDev);
        txtArtist.setText(art.artist != null ? art.artist : "-");
        txtStatus.setText(art.status);
        txtPublished.setText(art.publishedAt);

        Glide.with(this)
                .load(RetrofitClient.IMAGE_BASE_URL + art.bannerImg)
                .placeholder(R.drawable.placeholder)
                .into(imgBanner);

        Glide.with(this)
                .load(RetrofitClient.IMAGE_BASE_URL + art.coverImg)
                .placeholder(R.drawable.placeholder)
                .into(imgCover);
    }
}