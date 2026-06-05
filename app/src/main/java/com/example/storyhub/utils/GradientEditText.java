package com.example.storyhub.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class GradientEditText extends AppCompatEditText {

    public GradientEditText(Context context) {
        super(context);
        init();
    }

    public GradientEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyGradient();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyGradient() {
        String text = getText() != null ? getText().toString() : "";

        if (text.isEmpty()) {
            getPaint().setShader(null);
            invalidate();
            return;
        }

        getPaint().setShader(
                new LinearGradient(
                        0,
                        0,
                        getPaint().measureText(text),
                        getTextSize(),
                        new int[]{
                                Color.parseColor("#A855F7"),
                                Color.parseColor("#3B82F6")
                        },
                        null,
                        Shader.TileMode.CLAMP
                )
        );

        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        applyGradient();
    }
}