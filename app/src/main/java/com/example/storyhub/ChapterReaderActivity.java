package com.example.storyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.ComicPageAdapter;
import com.example.storyhub.adapters.CommentAdapter;
import com.example.storyhub.adapters.ReviewAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.api.ReviewApiService;
import com.example.storyhub.models.Chapter;
import com.example.storyhub.models.ChapterDetailResponse;
import com.example.storyhub.models.ChapterPageResponse;
import com.example.storyhub.models.ChapterResponse;
import com.example.storyhub.models.Comment;
import com.example.storyhub.models.CommentRequest;
import com.example.storyhub.models.CommentResponse;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.models.ReadingHistoryRequest;
import com.example.storyhub.models.Review;
import com.example.storyhub.models.ReviewRequest;
import com.example.storyhub.models.ReviewResponse;
import com.example.storyhub.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterReaderActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView txtChapterTitle, txtArtTitle, txtNovelContent;
    AppCompatImageButton btnToggleNav;
    ScrollView scrollNovel;
    RecyclerView recyclerComicPages;
    LinearLayout layoutLoading, layoutNavTop;
    AppCompatButton btnPrevTop, btnNextTop, btnScrollTop, btnScrollBottom;

    androidx.core.widget.NestedScrollView scrollComic;

    LinearLayout layoutNavBottomNovel;
    LinearLayout layoutNavBottomComic;

    AppCompatButton btnPrevBottomNovel;
    AppCompatButton btnNextBottomNovel;
    AppCompatButton btnScrollTopBottomNovel;
    AppCompatButton btnScrollBottomBottomNovel;

    AppCompatButton btnPrevBottomComic;
    AppCompatButton btnNextBottomComic;
    AppCompatButton btnScrollTopBottomComic;
    AppCompatButton btnScrollBottomBottomComic;

    AppCompatButton btnChapterTabReviewNovel, btnChapterTabCommentNovel;
    AppCompatButton btnChapterTabReviewComic, btnChapterTabCommentComic;
    LinearLayout layoutChapterReviewsNovel, layoutChapterCommentsNovel;
    LinearLayout layoutChapterReviewsComic, layoutChapterCommentsComic;
    AppCompatButton btnAddChapterReviewNovel, btnAddChapterCommentNovel;
    AppCompatButton btnAddChapterReviewComic, btnAddChapterCommentComic;
    TextView txtTotalChapterReviewsNovel, txtTotalChapterCommentsNovel;
    TextView txtTotalChapterReviewsComic, txtTotalChapterCommentsComic;
    RecyclerView recyclerChapterReviewsNovel, recyclerChapterCommentsNovel;
    RecyclerView recyclerChapterReviewsComic, recyclerChapterCommentsComic;
    ReviewApiService reviewService;

    int artId, chapterNumber, chapterId;
    boolean isNovel = false;
    boolean navVisible = false;
    boolean isSubscribed = false;
    List<Chapter> allChapters = null;

    TokenManager tokenManager;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reader);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);

        artId = getIntent().getIntExtra("art_id", -1);
        chapterNumber = getIntent().getIntExtra("chapter_number", 1);
        chapterId = getIntent().getIntExtra("chapter_id", -1);

        if (artId == -1) { finish(); return; }

        initViews();
        checkSubscriptionThenLoad();
        loadAllChapters();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtChapterTitle = findViewById(R.id.txtChapterTitle);
        txtArtTitle = findViewById(R.id.txtArtTitle);
        txtNovelContent = findViewById(R.id.txtNovelContent);
        scrollNovel = findViewById(R.id.scrollNovel);
        scrollComic = findViewById(R.id.scrollComic);
        recyclerComicPages = findViewById(R.id.recyclerComicPages);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutNavTop = findViewById(R.id.layoutNavTop);
        btnToggleNav = findViewById(R.id.btnToggleNav);

        btnPrevTop = findViewById(R.id.btnPrevTop);
        btnNextTop = findViewById(R.id.btnNextTop);
        btnScrollTop = findViewById(R.id.btnScrollTop);
        btnScrollBottom = findViewById(R.id.btnScrollBottom);

        layoutNavBottomNovel = findViewById(R.id.layoutNavBottomNovel);
        btnPrevBottomNovel = findViewById(R.id.btnPrevBottomNovel);
        btnNextBottomNovel = findViewById(R.id.btnNextBottomNovel);
        btnScrollTopBottomNovel = findViewById(R.id.btnScrollTopBottomNovel);
        btnScrollBottomBottomNovel = findViewById(R.id.btnScrollBottomBottomNovel);

        layoutNavBottomComic = findViewById(R.id.layoutNavBottomComic);
        btnPrevBottomComic = findViewById(R.id.btnPrevBottomComic);
        btnNextBottomComic = findViewById(R.id.btnNextBottomComic);
        btnScrollTopBottomComic = findViewById(R.id.btnScrollTopBottomComic);
        btnScrollBottomBottomComic = findViewById(R.id.btnScrollBottomBottomComic);

        recyclerComicPages.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());

        btnToggleNav.setOnClickListener(v -> {
            navVisible = !navVisible;
            layoutNavTop.setVisibility(navVisible ? View.VISIBLE : View.GONE);
        });

        View.OnClickListener scrollNovelToTop = v -> scrollNovel.smoothScrollTo(0, 0);
        View.OnClickListener scrollNovelToBottom = v ->
                scrollNovel.post(() -> scrollNovel.smoothScrollTo(
                        0, scrollNovel.getChildAt(0).getHeight()));
        View.OnClickListener scrollComicToTop = v -> scrollComic.smoothScrollTo(0, 0);
        View.OnClickListener scrollComicToBottom = v ->
                scrollComic.post(() -> scrollComic.smoothScrollTo(
                        0, scrollComic.getChildAt(0).getHeight()));

        btnScrollTop.setOnClickListener(v -> {
            if (isNovel) scrollNovelToTop.onClick(v);
            else scrollComicToTop.onClick(v);
        });
        btnScrollBottom.setOnClickListener(v -> {
            if (isNovel) scrollNovelToBottom.onClick(v);
            else scrollComicToBottom.onClick(v);
        });

        btnScrollTopBottomNovel.setOnClickListener(scrollNovelToTop);
        btnScrollBottomBottomNovel.setOnClickListener(scrollNovelToBottom);
        btnPrevBottomNovel.setOnClickListener(v -> goToPrevChapter());
        btnNextBottomNovel.setOnClickListener(v -> goToNextChapter());

        btnScrollTopBottomComic.setOnClickListener(scrollComicToTop);
        btnScrollBottomBottomComic.setOnClickListener(scrollComicToBottom);
        btnPrevBottomComic.setOnClickListener(v -> goToPrevChapter());
        btnNextBottomComic.setOnClickListener(v -> goToNextChapter());

        btnPrevTop.setOnClickListener(v -> goToPrevChapter());
        btnNextTop.setOnClickListener(v -> goToNextChapter());

        reviewService = RetrofitClient.getAuthInstance(tokenManager).create(ReviewApiService.class);

        btnChapterTabReviewNovel = findViewById(R.id.btnChapterTabReviewNovel);
        btnChapterTabCommentNovel = findViewById(R.id.btnChapterTabCommentNovel);
        btnChapterTabReviewComic = findViewById(R.id.btnChapterTabReviewComic);
        btnChapterTabCommentComic = findViewById(R.id.btnChapterTabCommentComic);

        layoutChapterReviewsNovel = findViewById(R.id.layoutChapterReviewsNovel);
        layoutChapterCommentsNovel = findViewById(R.id.layoutChapterCommentsNovel);
        layoutChapterReviewsComic = findViewById(R.id.layoutChapterReviewsComic);
        layoutChapterCommentsComic = findViewById(R.id.layoutChapterCommentsComic);

        btnAddChapterReviewNovel = findViewById(R.id.btnAddChapterReviewNovel);
        btnAddChapterCommentNovel = findViewById(R.id.btnAddChapterCommentNovel);
        btnAddChapterReviewComic = findViewById(R.id.btnAddChapterReviewComic);
        btnAddChapterCommentComic = findViewById(R.id.btnAddChapterCommentComic);

        txtTotalChapterReviewsNovel = findViewById(R.id.txtTotalChapterReviewsNovel);
        txtTotalChapterCommentsNovel = findViewById(R.id.txtTotalChapterCommentsNovel);
        txtTotalChapterReviewsComic = findViewById(R.id.txtTotalChapterReviewsComic);
        txtTotalChapterCommentsComic = findViewById(R.id.txtTotalChapterCommentsComic);

        recyclerChapterReviewsNovel = findViewById(R.id.recyclerChapterReviewsNovel);
        recyclerChapterCommentsNovel = findViewById(R.id.recyclerChapterCommentsNovel);
        recyclerChapterReviewsComic = findViewById(R.id.recyclerChapterReviewsComic);
        recyclerChapterCommentsComic = findViewById(R.id.recyclerChapterCommentsComic);

        recyclerChapterReviewsNovel.setLayoutManager(new LinearLayoutManager(this));
        recyclerChapterReviewsNovel.setNestedScrollingEnabled(false);
        recyclerChapterCommentsNovel.setLayoutManager(new LinearLayoutManager(this));
        recyclerChapterCommentsNovel.setNestedScrollingEnabled(false);
        recyclerChapterReviewsComic.setLayoutManager(new LinearLayoutManager(this));
        recyclerChapterReviewsComic.setNestedScrollingEnabled(false);
        recyclerChapterCommentsComic.setLayoutManager(new LinearLayoutManager(this));
        recyclerChapterCommentsComic.setNestedScrollingEnabled(false);

        btnChapterTabReviewNovel.setOnClickListener(v -> {
            layoutChapterReviewsNovel.setVisibility(View.VISIBLE);
            layoutChapterCommentsNovel.setVisibility(View.GONE);
            setTabStyle(btnChapterTabReviewNovel, true);
            setTabStyle(btnChapterTabCommentNovel, false);
        });
        btnChapterTabCommentNovel.setOnClickListener(v -> {
            layoutChapterReviewsNovel.setVisibility(View.GONE);
            layoutChapterCommentsNovel.setVisibility(View.VISIBLE);
            setTabStyle(btnChapterTabReviewNovel, false);
            setTabStyle(btnChapterTabCommentNovel, true);
        });

        btnChapterTabReviewComic.setOnClickListener(v -> {
            layoutChapterReviewsComic.setVisibility(View.VISIBLE);
            layoutChapterCommentsComic.setVisibility(View.GONE);
            setTabStyle(btnChapterTabReviewComic, true);
            setTabStyle(btnChapterTabCommentComic, false);
        });
        btnChapterTabCommentComic.setOnClickListener(v -> {
            layoutChapterReviewsComic.setVisibility(View.GONE);
            layoutChapterCommentsComic.setVisibility(View.VISIBLE);
            setTabStyle(btnChapterTabReviewComic, false);
            setTabStyle(btnChapterTabCommentComic, true);
        });

        btnAddChapterReviewNovel.setOnClickListener(v -> showChapterReviewDialog());
        btnAddChapterCommentNovel.setOnClickListener(v -> showChapterCommentDialog());
        btnAddChapterReviewComic.setOnClickListener(v -> showChapterReviewDialog());
        btnAddChapterCommentComic.setOnClickListener(v -> showChapterCommentDialog());

        if (!tokenManager.isLoggedIn()) {
            btnAddChapterReviewNovel.setVisibility(View.GONE);
            btnAddChapterCommentNovel.setVisibility(View.GONE);
            btnAddChapterReviewComic.setVisibility(View.GONE);
            btnAddChapterCommentComic.setVisibility(View.GONE);
        }
    }

    private void setTabStyle(AppCompatButton button, boolean active) {
        if (active) {
            button.setBackgroundResource(R.drawable.button_gradient);
            button.setTextColor(getResources().getColor(R.color.white));
        } else {
            button.setBackgroundResource(R.drawable.chip_background);
            button.setTextColor(getResources().getColor(R.color.primary));
        }
    }

    private void checkSubscriptionThenLoad() {
        if (!tokenManager.isLoggedIn()) {
            isSubscribed = false;
            loadChapter();
            return;
        }

        com.example.storyhub.api.UserApiService userService =
                RetrofitClient.getAuthInstance(tokenManager)
                        .create(com.example.storyhub.api.UserApiService.class);

        userService.isSubscribe(tokenManager.getUserId())
                .enqueue(new Callback<com.example.storyhub.models.SubscribeResponse>() {
                    @Override
                    public void onResponse(Call<com.example.storyhub.models.SubscribeResponse> call,
                                           Response<com.example.storyhub.models.SubscribeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            isSubscribed = response.body().isSubscribe;
                        }
                        loadChapter();
                    }
                    @Override
                    public void onFailure(Call<com.example.storyhub.models.SubscribeResponse> call,
                                          Throwable t) {
                        loadChapter();
                    }
                });
    }

    private void loadAllChapters() {
        artService.getChapters(artId, "Published").enqueue(new Callback<ChapterResponse>() {
            @Override
            public void onResponse(Call<ChapterResponse> call, Response<ChapterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allChapters = response.body().data;
                }
            }
            @Override public void onFailure(Call<ChapterResponse> call, Throwable t) {}
        });
    }

    private void goToPrevChapter() {
        int prevNumber = Integer.MIN_VALUE;
        boolean found = false;

        if (allChapters != null) {
            for (Chapter ch : allChapters) {
                if (ch.chapterNumber < chapterNumber) {
                    if (!found || ch.chapterNumber > prevNumber) {
                        prevNumber = ch.chapterNumber;
                        found = true;
                    }
                }
            }
        }

        if (!found) {
            Toast.makeText(this, "Ini sudah chapter pertama.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allChapters != null) {
            for (Chapter ch : allChapters) {
                if (ch.chapterNumber == prevNumber && ch.isPremium == 1 && !isSubscribed) {
                    Toast.makeText(this,
                            "Kamu perlu langganan untuk membuka chapter ini.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        navigateToChapter(prevNumber);
    }

    private void goToNextChapter() {
        int nextNumber = -1;
        if (allChapters != null) {
            for (Chapter ch : allChapters) {
                if (ch.chapterNumber > chapterNumber) {
                    if (nextNumber == -1 || ch.chapterNumber < nextNumber) {
                        nextNumber = ch.chapterNumber;
                    }
                }
            }
        } else {
            nextNumber = chapterNumber + 1;
        }

        if (nextNumber == -1) {
            Toast.makeText(this, "Ini sudah chapter terakhir.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allChapters != null) {
            boolean found = false;
            for (Chapter ch : allChapters) {
                if (ch.chapterNumber == nextNumber) {
                    found = true;
                    if (ch.isPremium == 1 && !isSubscribed) {
                        Toast.makeText(this,
                                "Kamu perlu langganan untuk membuka chapter ini.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                }
            }
            if (!found) {
                Toast.makeText(this, "Ini sudah chapter terakhir.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        navigateToChapter(nextNumber);
    }

    private void navigateToChapter(int targetChapterNumber) {
        int targetChapterId = -1;
        if (allChapters != null) {
            for (Chapter ch : allChapters) {
                if (ch.chapterNumber == targetChapterNumber) {
                    targetChapterId = ch.id;
                    break;
                }
            }
        }

        android.content.Intent intent = new android.content.Intent(this, ChapterReaderActivity.class);
        intent.putExtra("art_id", artId);
        intent.putExtra("chapter_number", targetChapterNumber);
        intent.putExtra("chapter_id", targetChapterId);
        intent.putExtra("art_title", getIntent().getStringExtra("art_title"));
        startActivity(intent);
        finish();
    }

    private void loadChapter() {
        layoutLoading.setVisibility(View.VISIBLE);

        artService.getNovelChapter(artId, chapterNumber)
                .enqueue(new Callback<ChapterDetailResponse>() {
                    @Override
                    public void onResponse(Call<ChapterDetailResponse> call,
                                           Response<ChapterDetailResponse> response) {
                        layoutLoading.setVisibility(View.GONE);

                        if (response.code() == 403) {
                            Toast.makeText(ChapterReaderActivity.this,
                                    "Kamu harus berlangganan untuk membuka chapter ini.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            Chapter chapter = response.body().data;

                            if (chapter != null && chapter.isiChapterNovel != null) {
                                isNovel = true;
                                String artTitle = getIntent().getStringExtra("art_title");
                                if (artTitle != null) txtArtTitle.setText(artTitle);
                                txtChapterTitle.setText("Chapter " + chapter.chapterNumber
                                        + " - " + chapter.title);

                                txtNovelContent.setText(chapter.isiChapterNovel);
                                scrollNovel.setVisibility(View.VISIBLE);
                                scrollComic.setVisibility(View.GONE);
                                layoutNavBottomNovel.setVisibility(View.VISIBLE);
                                loadChapterReviews();
                                loadChapterComments();

                                if (tokenManager.isLoggedIn()) {
                                    saveReadingHistory(0);
                                    scrollNovel.getViewTreeObserver()
                                            .addOnScrollChangedListener(() -> {
                                                if (tokenManager.isLoggedIn()) {
                                                    saveReadingHistory(scrollNovel.getScrollY());
                                                }
                                            });
                                }
                            } else {
                                loadComicChapter();
                            }
                        } else {
                            loadComicChapter();
                        }
                    }

                    @Override
                    public void onFailure(Call<ChapterDetailResponse> call, Throwable t) {
                        layoutLoading.setVisibility(View.GONE);
                        loadComicChapter();
                    }
                });
    }

    private void loadChapterReviews() {
        reviewService.getReviews("chapters", chapterId).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                String totalText = "0 Review";
                List<Review> reviews = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    reviews = response.body().data;
                    totalText = response.body().totalReviews + " Review";
                }

                String finalTotal = totalText;
                List<Review> finalReviews = reviews;

                ReviewAdapter adapter = new ReviewAdapter(
                        ChapterReaderActivity.this, finalReviews, "chapters", chapterId);

                runOnUiThread(() -> {
                    txtTotalChapterReviewsNovel.setText(finalTotal);
                    txtTotalChapterReviewsComic.setText(finalTotal);
                    recyclerChapterReviewsNovel.setAdapter(adapter);
                    recyclerChapterReviewsComic.setAdapter(adapter);
                });
            }
            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {}
        });
    }

    private void loadChapterComments() {
        reviewService.getComments("chapters", chapterId).enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                String totalText = "0 Komentar";
                List<Comment> comments = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null) {
                    comments = response.body().data;
                    int total = response.body().totalComments != null
                            ? response.body().totalComments.total_comments : comments.size();
                    totalText = total + " Komentar";
                }

                String finalTotal = totalText;
                List<Comment> finalComments = comments;

                CommentAdapter adapter = new CommentAdapter(
                        ChapterReaderActivity.this, finalComments, "chapters", chapterId);

                runOnUiThread(() -> {
                    txtTotalChapterCommentsNovel.setText(finalTotal);
                    txtTotalChapterCommentsComic.setText(finalTotal);
                    recyclerChapterCommentsNovel.setAdapter(adapter);
                    recyclerChapterCommentsComic.setAdapter(adapter);
                });
            }
            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {}
        });
    }

    private void showChapterReviewDialog() {
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Login dulu untuk review.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tulis Review Chapter");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        RatingBar ratingBarTop = dialogView.findViewById(R.id.ratingBarTop);
        RatingBar ratingBarBottom = dialogView.findViewById(R.id.ratingBarBottom);
        EditText etKomentar = dialogView.findViewById(R.id.etKomentar);

        ratingBarTop.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            if (fromUser) ratingBarBottom.setRating(0);
        });
        ratingBarBottom.setOnRatingBarChangeListener((rb, rating, fromUser) -> {
            if (fromUser && rating > 0) ratingBarTop.setRating(5);
        });

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            int rating = (int)(ratingBarTop.getRating() + ratingBarBottom.getRating());
            String komentar = etKomentar.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Rating harus diisi (1-10).", Toast.LENGTH_SHORT).show();
                return;
            }

            reviewService.postReview("chapters", chapterId, new ReviewRequest(rating, komentar))
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call,
                                               Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ChapterReaderActivity.this,
                                        "Review berhasil!", Toast.LENGTH_SHORT).show();
                                loadChapterReviews();
                            } else if (response.code() == 403) {
                                Toast.makeText(ChapterReaderActivity.this,
                                        "Kamu sudah pernah review chapter ini.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showChapterCommentDialog() {
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Login dulu untuk berkomentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tulis Komentar");

        EditText etInput = new EditText(this);
        etInput.setHint("Tulis komentarmu...");
        etInput.setPadding(40, 20, 40, 20);
        builder.setView(etInput);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String text = etInput.getText().toString().trim();
            if (text.isEmpty()) return;

            reviewService.postComment("chapters", chapterId, new CommentRequest(text))
                    .enqueue(new Callback<MessageResponse>() {
                        @Override
                        public void onResponse(Call<MessageResponse> call,
                                               Response<MessageResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ChapterReaderActivity.this,
                                        "Komentar berhasil!", Toast.LENGTH_SHORT).show();
                                loadChapterComments();
                            }
                        }
                        @Override
                        public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void loadComicChapter() {
        layoutLoading.setVisibility(View.VISIBLE);

        artService.getComicChapter(artId, chapterNumber)
                .enqueue(new Callback<ChapterPageResponse>() {
                    @Override
                    public void onResponse(Call<ChapterPageResponse> call,
                                           Response<ChapterPageResponse> response) {
                        layoutLoading.setVisibility(View.GONE);

                        if (response.code() == 403) {
                            Toast.makeText(ChapterReaderActivity.this,
                                    "Kamu harus berlangganan untuk membuka chapter ini.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null
                                && response.body().data != null) {
                            isNovel = false;
                            scrollNovel.setVisibility(View.GONE);
                            scrollComic.setVisibility(View.VISIBLE);
                            layoutNavBottomComic.setVisibility(View.VISIBLE);
                            loadChapterReviews();
                            loadChapterComments();

                            String artTitle = getIntent().getStringExtra("art_title");
                            if (artTitle != null) txtArtTitle.setText(artTitle);

                            if (txtChapterTitle.getText().toString().isEmpty()) {
                                txtChapterTitle.setText("Chapter " + chapterNumber);
                            }

                            ComicPageAdapter adapter = new ComicPageAdapter(
                                    ChapterReaderActivity.this, response.body().data);
                            recyclerComicPages.setAdapter(adapter);
                            loadChapterReviews();
                            loadChapterComments();

                            if (tokenManager.isLoggedIn()) {
                                saveReadingHistory(0);
                                recyclerComicPages.addOnScrollListener(
                                        new RecyclerView.OnScrollListener() {
                                            @Override
                                            public void onScrolled(@NonNull RecyclerView rv,
                                                                   int dx, int dy) {
                                                if (tokenManager.isLoggedIn()) {
                                                    LinearLayoutManager lm =
                                                            (LinearLayoutManager) rv.getLayoutManager();
                                                    int pos = lm != null
                                                            ? lm.findFirstVisibleItemPosition() : 0;
                                                    saveReadingHistory(pos);
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(ChapterReaderActivity.this,
                                    "Chapter tidak ditemukan.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ChapterPageResponse> call, Throwable t) {
                        layoutLoading.setVisibility(View.GONE);
                        Toast.makeText(ChapterReaderActivity.this,
                                "Gagal load chapter.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean historySaved = false;
    private long lastSaveTime = 0;

    private void saveReadingHistory(int scrollY) {
        long now = System.currentTimeMillis();
        if (historySaved && now - lastSaveTime < 3000) return;
        lastSaveTime = now;

        ReadingHistoryRequest request = new ReadingHistoryRequest(1, scrollY);

        if (!historySaved) {
            historySaved = true;
            artService.postReadingHistory(artId, chapterId, request)
                    .enqueue(new Callback<MessageResponse>() {
                        @Override public void onResponse(Call<MessageResponse> call,
                                                         Response<MessageResponse> response) {}
                        @Override public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        } else {
            artService.updateReadingHistory(artId, chapterId, request)
                    .enqueue(new Callback<MessageResponse>() {
                        @Override public void onResponse(Call<MessageResponse> call,
                                                         Response<MessageResponse> response) {}
                        @Override public void onFailure(Call<MessageResponse> call, Throwable t) {}
                    });
        }
    }
}