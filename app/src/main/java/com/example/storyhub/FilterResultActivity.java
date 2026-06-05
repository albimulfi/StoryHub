package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.ArtAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterResultActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView txtPageTitle;
    RecyclerView recyclerArts;
    AppCompatButton btnPrevPage, btnNextPage;
    TextView txtPageInfo;
    TextView txtResultCount;
    TextView txtEmpty;
    LinearLayout layoutPagination;

    String filterType;
    String filterValue;
    int currentPage = 1;
    int totalPages = 1;

    TokenManager tokenManager;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_result);

        filterType = getIntent().getStringExtra("filter_type");
        filterValue = getIntent().getStringExtra("filter_value");

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager).create(ArtApiService.class);

        initViews();
        loadArts();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtPageTitle = findViewById(R.id.txtPageTitle);
        txtResultCount = findViewById(R.id.txtResultCount);
        txtEmpty = findViewById(R.id.txtEmpty);
        recyclerArts = findViewById(R.id.recyclerArts);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        txtPageInfo = findViewById(R.id.txtPageInfo);
        layoutPagination = findViewById(R.id.layoutPagination);

        recyclerArts.setLayoutManager(new GridLayoutManager(this, 2));

        btnBack.setOnClickListener(v -> finish());

        String label = "";
        switch (filterType != null ? filterType : "") {
            case "category": label = "Kategori " + filterValue; break;
            case "genre": label = "Genre " + filterValue; break;
            case "tag": label = "Tag " + filterValue; break;
            case "mood": label = "Mood " + filterValue; break;
            default: label = filterValue != null ? filterValue : ""; break;
        }
        txtPageTitle.setText(label);

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadArts();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadArts();
            }
        });
    }

    private void loadArts() {
        String category = filterType.equals("category") ? filterValue : null;
        String genre = filterType.equals("genre") ? filterValue : null;
        String tag = filterType.equals("tag") ? filterValue : null;
        String mood = filterType.equals("mood") ? filterValue : null;

        artService.getArts(currentPage, 8, category, genre, tag, mood,
                        "ratingArt", "DESC", null)
                .enqueue(new Callback<ArtResponse>() {
                    @Override
                    public void onResponse(Call<ArtResponse> call,
                                           Response<ArtResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().meta != null) {
                                totalPages = response.body().meta.total_pages;

                                if (currentPage == 1) {
                                    txtResultCount.setVisibility(View.VISIBLE);
                                    txtResultCount.setText(
                                            response.body().meta.total + " Art Ditemukan"
                                    );
                                } else {
                                    txtResultCount.setVisibility(View.GONE);
                                }
                            }
                            updatePagination();

                            if (response.body().data == null || response.body().data.isEmpty()) {

                                recyclerArts.setVisibility(View.GONE);
                                layoutPagination.setVisibility(View.GONE);
                                txtResultCount.setVisibility(View.GONE);
                                txtEmpty.setVisibility(View.VISIBLE);

                                String labelType;

                                switch (filterType) {
                                    case "category":
                                        labelType = "kategori";
                                        break;
                                    case "genre":
                                        labelType = "genre";
                                        break;
                                    case "tag":
                                        labelType = "tag";
                                        break;
                                    case "mood":
                                        labelType = "mood";
                                        break;
                                    default:
                                        labelType = "filter";
                                        break;
                                }

                                txtEmpty.setText(
                                        "Art dengan " + labelType + " \"" +
                                                filterValue + "\" tidak ada."
                                );

                            } else {

                                recyclerArts.setVisibility(View.VISIBLE);
                                layoutPagination.setVisibility(View.VISIBLE);
                                txtEmpty.setVisibility(View.GONE);

                                ArtAdapter adapter = new ArtAdapter(
                                        FilterResultActivity.this,
                                        response.body().data,
                                        art -> {
                                            Intent intent = new Intent(
                                                    FilterResultActivity.this,
                                                    DetailContentActivity.class);
                                            intent.putExtra("art_id", art.id);
                                            startActivity(intent);
                                        });

                                recyclerArts.setAdapter(adapter);
                            }
                        } else {
                            Toast.makeText(FilterResultActivity.this,
                                    "Tidak ada data.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArtResponse> call, Throwable t) {
                        Toast.makeText(FilterResultActivity.this,
                                "Gagal load data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePagination() {
        txtPageInfo.setText("Halaman " + currentPage + " / " + totalPages);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
    }
}