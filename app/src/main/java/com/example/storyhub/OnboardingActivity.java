package com.example.storyhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> {

            Intent intent =
                    new Intent(OnboardingActivity.this,
                            HomeActivity.class);

            startActivity(intent);
        });

    }
}