package com.example.storyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.Chapter;
import com.example.storyhub.models.ChapterDetailResponse;
import com.example.storyhub.models.CreateChapterRequest;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditChapterActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView txtChapterInfo;
    TextView txtArtTitle;
    EditText etChapterTitle, etChapterContent;
    Button btnSave;
    Spinner spinnerPublishStatus;
    ProgressBar progressBar;

    int artId, chapterId, chapterNumber;
    String artTitle;
    TokenManager tokenManager;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chapter);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager)
                .create(ArtApiService.class);

        artId = getIntent().getIntExtra("art_id", -1);
        artTitle = getIntent().getStringExtra("art_title");
        chapterId = getIntent().getIntExtra("chapter_id", -1);
        chapterNumber = getIntent().getIntExtra("chapter_number", 1);
        String chapterTitle = getIntent().getStringExtra("chapter_title");

        if (artId == -1 || chapterId == -1) { finish(); return; }

        initViews();

        initViews();

        txtChapterInfo.setText("Edit Chapter " + chapterNumber);

        loadChapterDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtChapterInfo = findViewById(R.id.txtChapterInfo);
        txtArtTitle = findViewById(R.id.txtArtTitle);
        etChapterTitle = findViewById(R.id.etChapterTitle);
        etChapterContent = findViewById(R.id.etChapterContent);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        spinnerPublishStatus = findViewById(R.id.spinnerPublishStatus);

        txtArtTitle.setText(artTitle != null ? artTitle : "Novel");

        String[] publishOptions = {"Draft", "Published"};

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        publishOptions
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerPublishStatus.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveChapter());
    }

    private void loadChapterDetail() {

        progressBar.setVisibility(View.VISIBLE);

        artService.getNovelChapter(artId, chapterNumber)
                .enqueue(new Callback<ChapterDetailResponse>() {

                    @Override
                    public void onResponse(
                            Call<ChapterDetailResponse> call,
                            Response<ChapterDetailResponse> response) {

                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().data != null) {

                            Chapter chapter = response.body().data;

                            etChapterTitle.setText(chapter.title);

                            if (chapter.isiChapterNovel != null) {
                                etChapterContent.setText(
                                        chapter.isiChapterNovel
                                );
                            }

                            if ("Published".equalsIgnoreCase(chapter.isPublished)) {
                                spinnerPublishStatus.setSelection(1);
                            } else {
                                spinnerPublishStatus.setSelection(0);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ChapterDetailResponse> call,
                            Throwable t) {

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(
                                EditChapterActivity.this,
                                "Gagal memuat chapter.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void saveChapter() {
        String title = etChapterTitle.getText().toString().trim();
        String content = etChapterContent.getText().toString().trim();
        String status = spinnerPublishStatus.getSelectedItem().toString();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Judul dan isi chapter harus diisi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        artService.updateChapter(artId, chapterId,
                        new CreateChapterRequest(title, content, status))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call,
                                           Response<MessageResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);

                        if (response.isSuccessful()) {
                            Toast.makeText(EditChapterActivity.this,
                                    "Chapter berhasil diupdate!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditChapterActivity.this,
                                    "Gagal update chapter.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                    }
                });
    }
}