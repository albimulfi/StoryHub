package com.example.storyhub.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class GradientText extends AppCompatTextView {

    public GradientText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        getPaint().setShader(
                new LinearGradient(
                        0, 0,
                        getPaint().measureText(getText().toString()),
                        getTextSize(),
                        new int[]{
                                Color.parseColor("#A855F7"),
                                Color.parseColor("#3B82F6")
                        },
                        null,
                        Shader.TileMode.CLAMP
                )
        );
    }
}