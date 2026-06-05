package com.example.storyhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.FilterApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.CreateArtResponse;
import com.example.storyhub.models.Genre;
import com.example.storyhub.models.GenreResponse;
import com.example.storyhub.models.Mood;
import com.example.storyhub.models.MoodResponse;
import com.example.storyhub.models.Tag;
import com.example.storyhub.models.TagResponse;
import com.example.storyhub.utils.TokenManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNovelActivity extends AppCompatActivity {

    ImageView btnBack;
    ImageView imgCoverPreview, imgBannerPreview;
    TextView btnPilihCover, btnPilihBanner;
    EditText etTitle, etTagline, etSynopsis;
    Spinner spinnerStatus;
    Button btnNext, btnDraft;
    ProgressBar progressBar;

    EditText etSearchGenreTagMood;
    LinearLayout layoutSearchPreview, containerSearchPreview;
    com.google.android.flexbox.FlexboxLayout containerSelectedChips;

    List<String> allGenres = new ArrayList<>();
    List<String> allTags = new ArrayList<>();
    List<String> allMoods = new ArrayList<>();
    List<String> selectedGenres = new ArrayList<>();
    List<String> selectedTags = new ArrayList<>();
    List<String> selectedMoods = new ArrayList<>();

    Uri coverUri = null;
    Uri bannerUri = null;
    boolean pickingCover = false;

    TokenManager tokenManager;
    ArtApiService artService;
    FilterApiService filterService;

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (pickingCover) {
                        coverUri = uri;
                        Glide.with(this).load(uri).centerCrop().into(imgCoverPreview);
                        imgCoverPreview.setVisibility(View.VISIBLE);
                    } else {
                        bannerUri = uri;
                        Glide.with(this).load(uri).centerCrop().into(imgBannerPreview);
                        imgBannerPreview.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_novel);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);
        filterService = RetrofitClient.getInstance().create(FilterApiService.class);

        initViews();
        loadFilterOptions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgCoverPreview = findViewById(R.id.imgCoverPreview);
        imgBannerPreview = findViewById(R.id.imgBannerPreview);
        btnPilihCover = findViewById(R.id.btnPilihCover);
        btnPilihBanner = findViewById(R.id.btnPilihBanner);
        etTitle = findViewById(R.id.etTitle);
        etTagline = findViewById(R.id.etTagline);
        etSynopsis = findViewById(R.id.etSynopsis);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnNext = findViewById(R.id.btnNext);
        btnDraft = findViewById(R.id.btnDraft);
        progressBar = findViewById(R.id.progressBar);

        etSearchGenreTagMood = findViewById(R.id.etSearchGenreTagMood);
        layoutSearchPreview = findViewById(R.id.layoutSearchPreview);
        containerSearchPreview = findViewById(R.id.containerSearchPreview);
        containerSelectedChips = findViewById(R.id.containerSelectedChips);

        String[] statusOptions = {"Ongoing", "Completed", "Hiatus"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnPilihCover.setOnClickListener(v -> {
            pickingCover = true;
            galleryLauncher.launch(new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        });

        btnPilihBanner.setOnClickListener(v -> {
            pickingCover = false;
            galleryLauncher.launch(new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        });

        etSearchGenreTagMood.addTextChangedListener(new android.text.TextWatcher() {
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

        btnNext.setOnClickListener(v -> createNovel("Published"));
        btnDraft.setOnClickListener(v -> createNovel("Draft"));
    }

    private void loadFilterOptions() {
        filterService.getGenres().enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    for (Genre g : response.body().data) allGenres.add(g.genre);
            }
            @Override public void onFailure(Call<GenreResponse> call, Throwable t) {}
        });

        filterService.getTags().enqueue(new Callback<TagResponse>() {
            @Override
            public void onResponse(Call<TagResponse> call, Response<TagResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    for (Tag t : response.body().data) allTags.add(t.tag);
            }
            @Override public void onFailure(Call<TagResponse> call, Throwable t) {}
        });

        filterService.getMoods().enqueue(new Callback<MoodResponse>() {
            @Override
            public void onResponse(Call<MoodResponse> call, Response<MoodResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    for (Mood m : response.body().data) allMoods.add(m.mood);
            }
            @Override public void onFailure(Call<MoodResponse> call, Throwable t) {}
        });
    }

    private void showSearchPreview(String query) {
        containerSearchPreview.removeAllViews();
        List<String[]> results = new ArrayList<>();

        for (String g : allGenres)
            if (!selectedGenres.contains(g) && g.toLowerCase().contains(query))
                results.add(new String[]{g, "Genre"});
        for (String t : allTags)
            if (!selectedTags.contains(t) && t.toLowerCase().contains(query))
                results.add(new String[]{t, "Tag"});
        for (String m : allMoods)
            if (!selectedMoods.contains(m) && m.toLowerCase().contains(query))
                results.add(new String[]{m, "Mood"});

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
                etSearchGenreTagMood.setText("");
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
            case "Genre": if (selectedGenres.contains(nama)) return; selectedGenres.add(nama); break;
            case "Tag":   if (selectedTags.contains(nama)) return;   selectedTags.add(nama);   break;
            case "Mood":  if (selectedMoods.contains(nama)) return;  selectedMoods.add(nama);  break;
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

    private MultipartBody.Part uriToPart(Uri uri, String fieldName, String prefix) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            String fileName = prefix + "_" + tokenManager.getUserId() + ".jpg";
            File tempFile = new File(getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            is.close();
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            return MultipartBody.Part.createFormData(fieldName, fileName, reqFile);
        } catch (Exception e) {
            return null;
        }
    }

    private RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value != null ? value : "");
    }

    private List<RequestBody> toRequestBodyList(List<String> items) {
        List<RequestBody> list = new ArrayList<>();
        for (String item : items)
            list.add(RequestBody.create(MediaType.parse("text/plain"), item));
        return list;
    }

    private void createNovel(String isPublished) {
        String title = etTitle.getText().toString().trim();
        String tagline = etTagline.getText().toString().trim();
        String synopsis = etSynopsis.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (title.isEmpty() || tagline.isEmpty() || synopsis.isEmpty()) {
            Toast.makeText(this, "Judul, tagline, dan synopsis harus diisi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (coverUri == null) {
            Toast.makeText(this, "Pilih gambar cover terlebih dahulu.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (bannerUri == null) {
            Toast.makeText(this, "Pilih gambar banner terlebih dahulu.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part coverPart = uriToPart(coverUri, "cover_img", "cover");
        MultipartBody.Part bannerPart = uriToPart(bannerUri, "banner_img", "banner");

        if (coverPart == null || bannerPart == null) {
            Toast.makeText(this, "Gagal memproses gambar.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);
        btnDraft.setEnabled(false);

        artService.createArt(
                toRequestBody(isPublished),
                toRequestBody(title),
                toRequestBody(status),
                toRequestBody("Novel"),
                toRequestBody(tagline),
                toRequestBody(synopsis),
                toRequestBodyList(selectedGenres),
                toRequestBodyList(selectedTags),
                toRequestBodyList(selectedMoods),
                coverPart,
                bannerPart
        ).enqueue(new Callback<CreateArtResponse>() {
            @Override
            public void onResponse(Call<CreateArtResponse> call,
                                   Response<CreateArtResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnNext.setEnabled(true);
                btnDraft.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateNovelActivity.this,
                            "Novel berhasil dibuat!", Toast.LENGTH_SHORT).show();
                    if (isPublished.equals("Published")) {
                        Intent intent = new Intent(CreateNovelActivity.this,
                                CreateChapterActivity.class);
                        intent.putExtra("art_id", response.body().artId);
                        intent.putExtra("art_title", title);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(CreateNovelActivity.this,
                            "Gagal membuat novel.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateArtResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnNext.setEnabled(true);
                btnDraft.setEnabled(true);
                Toast.makeText(CreateNovelActivity.this,
                        "Gagal terhubung ke server.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}