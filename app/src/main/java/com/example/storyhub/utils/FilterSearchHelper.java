package com.example.storyhub.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.storyhub.R;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

public class FilterSearchHelper {

    public interface OnSelectionChanged {
        void onChanged(List<String> selected);
    }

    private final Context context;
    private final EditText etSearch;
    private final CardView cardPreview;
    private final LinearLayout containerPreview;
    private final FlexboxLayout containerSelected;
    private final List<String> allItems;
    private final List<String> selectedItems;
    private OnSelectionChanged listener;

    public FilterSearchHelper(
            Context context,
            EditText etSearch,
            CardView cardPreview,
            LinearLayout containerPreview,
            FlexboxLayout containerSelected,
            List<String> allItems,
            List<String> selectedItems
    ) {
        this.context = context;
        this.etSearch = etSearch;
        this.cardPreview = cardPreview;
        this.containerPreview = containerPreview;
        this.containerSelected = containerSelected;
        this.allItems = allItems;
        this.selectedItems = selectedItems;

        setup();
    }

    public void setOnSelectionChanged(OnSelectionChanged listener) {
        this.listener = listener;
    }

    private void setup() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) {
                    cardPreview.setVisibility(View.GONE);
                    containerPreview.removeAllViews();
                } else {
                    showPreview(query);
                }
            }
        });

        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                cardPreview.setVisibility(View.GONE);
            }
        });
    }

    private void showPreview(String query) {
        containerPreview.removeAllViews();

        List<String> filtered = new ArrayList<>();
        for (String item : allItems) {
            if (item.toLowerCase().contains(query) && !selectedItems.contains(item)) {
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            cardPreview.setVisibility(View.GONE);
            return;
        }

        cardPreview.setVisibility(View.VISIBLE);

        for (int i = 0; i < filtered.size(); i++) {
            String item = filtered.get(i);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            row.setClickable(true);
            row.setFocusable(true);

            TextView tvName = new TextView(context);
            tvName.setText(item);
            tvName.setTextSize(13f);
            tvName.setTextColor(context.getResources().getColor(android.R.color.black));
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameParams);

            TextView tvAdd = new TextView(context);
            tvAdd.setText("+");
            tvAdd.setTextSize(18f);
            tvAdd.setTextColor(context.getResources().getColor(R.color.primary));
            tvAdd.setPadding(dp(8), 0, 0, 0);

            row.addView(tvName);
            row.addView(tvAdd);

            row.setOnClickListener(v -> {
                addChip(item);
                etSearch.setText("");
                cardPreview.setVisibility(View.GONE);
            });

            containerPreview.addView(row);

            if (i < filtered.size() - 1) {
                View divider = new View(context);
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divParams.setMargins(dp(12), 0, dp(12), 0);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(0xFFEEEEEE);
                containerPreview.addView(divider);
            }
        }
    }

    private void addChip(String item) {
        if (selectedItems.contains(item)) return;
        selectedItems.add(item);
        renderChip(item);
        if (listener != null) listener.onChanged(new ArrayList<>(selectedItems));
    }

    private void removeChip(String item) {
        selectedItems.remove(item);
        rebuildChips();
        if (listener != null) listener.onChanged(new ArrayList<>(selectedItems));
    }

    private void renderChip(String item) {
        android.widget.FrameLayout chipWrapper = new android.widget.FrameLayout(context);

        android.widget.TextView chip = new android.widget.TextView(context);
        chip.setText(item + "  ");
        chip.setTextSize(12f);
        chip.setTextColor(context.getResources().getColor(R.color.primary));
        chip.setBackgroundResource(R.drawable.chip_background);
        chip.setPadding(dp(10), dp(6), dp(24), dp(6));

        android.widget.TextView tvX = new android.widget.TextView(context);
        tvX.setText("✕");
        tvX.setTextSize(10f);
        tvX.setTextColor(context.getResources().getColor(R.color.primary));
        android.widget.FrameLayout.LayoutParams xParams = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        xParams.gravity = android.view.Gravity.END | android.view.Gravity.TOP;
        xParams.setMargins(0, dp(2), dp(4), 0);
        tvX.setLayoutParams(xParams);
        tvX.setClickable(true);
        tvX.setFocusable(true);
        tvX.setOnClickListener(v -> removeChip(item));

        chipWrapper.addView(chip);
        chipWrapper.addView(tvX);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dp(8), dp(8));
        chipWrapper.setLayoutParams(params);

        containerSelected.addView(chipWrapper);
    }

    private void rebuildChips() {
        containerSelected.removeAllViews();
        for (String item : selectedItems) {
            renderChip(item);
        }
    }

    public void reset() {
        selectedItems.clear();
        containerSelected.removeAllViews();
        etSearch.setText("");
        cardPreview.setVisibility(View.GONE);
    }

    public void setAllItems(List<String> items) {
        allItems.clear();
        allItems.addAll(items);
    }

    private int dp(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}