package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyhub.adapters.MyWorksAdapter;
import com.example.storyhub.api.ArtApiService;
import com.example.storyhub.api.RetrofitClient;
import com.example.storyhub.models.ArtResponse;
import com.example.storyhub.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyWorksActivity extends AppCompatActivity {

    ImageView btnBack;
    Button btnTabPublished, btnTabDraft;
    RecyclerView recyclerWorks;
    TextView txtEmpty;
    LinearLayout layoutLoading;

    String currentTab = "published";
    TokenManager tokenManager;
    ArtApiService artService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_works);

        tokenManager = new TokenManager(this);
        artService = RetrofitClient.getAuthInstance(tokenManager)
                .create(ArtApiService.class);

        initViews();
        loadWorks("published");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnTabPublished = findViewById(R.id.btnTabPublished);
        btnTabDraft = findViewById(R.id.btnTabDraft);
        recyclerWorks = findViewById(R.id.recyclerWorks);
        txtEmpty = findViewById(R.id.txtEmpty);
        layoutLoading = findViewById(R.id.layoutLoading);

        recyclerWorks.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnTabPublished.setOnClickListener(v -> {
            currentTab = "published";
            btnTabPublished.setTextColor(getResources().getColor(R.color.white));
            btnTabPublished.setBackgroundResource(R.drawable.button_gradient);

            btnTabDraft.setTextColor(getResources().getColor(R.color.primary));
            btnTabDraft.setBackgroundResource(R.drawable.chip_background);
            loadWorks("published");
        });

        btnTabDraft.setOnClickListener(v -> {
            currentTab = "draft";
            btnTabDraft.setTextColor(getResources().getColor(R.color.white));
            btnTabDraft.setBackgroundResource(R.drawable.button_gradient);

            btnTabPublished.setTextColor(getResources().getColor(R.color.primary));
            btnTabPublished.setBackgroundResource(R.drawable.chip_background);
            loadWorks("draft");
        });
    }

    private void loadWorks(String status) {
        layoutLoading.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);
        recyclerWorks.setVisibility(View.GONE);

        artService.getMyArts(status).enqueue(new Callback<ArtResponse>() {
            @Override
            public void onResponse(Call<ArtResponse> call,
                                   Response<ArtResponse> response) {
                layoutLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && !response.body().data.isEmpty()) {

                    recyclerWorks.setVisibility(View.VISIBLE);
                    txtEmpty.setVisibility(View.GONE);

                    MyWorksAdapter adapter = new MyWorksAdapter(
                            MyWorksActivity.this,
                            response.body().data,
                            art -> {
                                Intent intent = new Intent(MyWorksActivity.this,
                                        EditNovelActivity.class);
                                intent.putExtra("art_id", art.id);
                                intent.putExtra("art_title", art.title);
                                startActivity(intent);
                            },
                            art -> {
                                Intent intent = new Intent(MyWorksActivity.this,
                                        CreateChapterActivity.class);
                                intent.putExtra("art_id", art.id);
                                intent.putExtra("art_title", art.title);
                                startActivity(intent);
                            }
                    );
                    recyclerWorks.setAdapter(adapter);

                } else {
                    recyclerWorks.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                    txtEmpty.setText(status.equals("published") ?
                            "Belum ada novel yang dipublish." :
                            "Belum ada novel draft.");
                }
            }

            @Override
            public void onFailure(Call<ArtResponse> call, Throwable t) {
                layoutLoading.setVisibility(View.GONE);
                txtEmpty.setVisibility(View.VISIBLE);
                txtEmpty.setText("Gagal load data.");
            }
        });
    }
}