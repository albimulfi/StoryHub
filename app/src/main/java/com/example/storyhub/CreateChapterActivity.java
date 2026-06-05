package com.example.storyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.CreateChapterRequest;
import com.example.storyhub.models.MessageResponse;
import com.example.storyhub.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateChapterActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView txtArtTitle;
    EditText etChapterTitle, etChapterContent;
    Button btnPublish, btnDraft;
    ProgressBar progressBar;

    int artId;
    String artTitle;
    TokenManager tokenManager;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chapter);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);

        artId = getIntent().getIntExtra("art_id", -1);
        artTitle = getIntent().getStringExtra("art_title");

        if (artId == -1) { finish(); return; }

        initViews();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtArtTitle = findViewById(R.id.txtArtTitle);
        etChapterTitle = findViewById(R.id.etChapterTitle);
        etChapterContent = findViewById(R.id.etChapterContent);
        btnPublish = findViewById(R.id.btnPublish);
        btnDraft = findViewById(R.id.btnDraft);
        progressBar = findViewById(R.id.progressBar);

        txtArtTitle.setText(artTitle != null ? artTitle : "Novel");

        btnBack.setOnClickListener(v -> finish());
        btnPublish.setOnClickListener(v -> createChapter("Published"));
        btnDraft.setOnClickListener(v -> createChapter("Draft"));
    }

    private void createChapter(String status) {
        String title = etChapterTitle.getText().toString().trim();
        String content = etChapterContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Judul dan isi chapter harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPublish.setEnabled(false);
        btnDraft.setEnabled(false);

        artService.createChapter(artId, new CreateChapterRequest(title, content, status))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call,
                                           Response<MessageResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        btnPublish.setEnabled(true);
                        btnDraft.setEnabled(true);

                        if (response.isSuccessful()) {
                            Toast.makeText(CreateChapterActivity.this,
                                    "Chapter berhasil dipublikasi!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateChapterActivity.this,
                                    "Gagal membuat chapter.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnPublish.setEnabled(true);
                        btnDraft.setEnabled(true);
                        Toast.makeText(CreateChapterActivity.this,
                                "Gagal terhubung ke server.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}