package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScratchMapActivity extends AppCompatActivity {

    // Indian states and major cities
    private static final String[][] INDIA_PLACES = {
            {"Goa", "Beach"},
            {"Mumbai", "City"},
            {"Delhi", "City"},
            {"Bangalore", "City"},
            {"Chennai", "City"},
            {"Kolkata", "City"},
            {"Hyderabad", "City"},
            {"Pune", "City"},
            {"Jaipur", "City"},
            {"Agra", "City"},
            {"Varanasi", "City"},
            {"Udaipur", "City"},
            {"Mysuru", "City"},
            {"Ooty", "Trek"},
            {"Manali", "Trek"},
            {"Shimla", "Trek"},
            {"Darjeeling", "Trek"},
            {"Munnar", "Trek"},
            {"Coorg", "Trek"},
            {"Ladakh", "Trek"},
            {"Rishikesh", "Trek"},
            {"Andaman", "Beach"},
            {"Kerala", "Beach"},
            {"Udupi", "Beach"},
            {"Pondicherry", "Beach"},
            {"Vizag", "Beach"},
            {"Leh", "Trek"},
            {"Spiti", "Trek"},
            {"Hampi", "City"},
            {"Ajanta", "City"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(
                Color.parseColor("#0D1B2A"));
        setContentView(scroll);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setBackgroundColor(
                Color.parseColor("#1B4332"));
        header.setPadding(20, 50, 20, 20);
        header.setGravity(
                android.view.Gravity.CENTER_VERTICAL);
        root.addView(header);

        TextView back = new TextView(this);
        back.setText("←  ");
        back.setTextColor(Color.WHITE);
        back.setTextSize(20);
        back.setOnClickListener(v -> finish());
        header.addView(back);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        header.addView(titleCol);

        TextView title = new TextView(this);
        title.setText("🗺️ India Scratch Map");
        title.setTextSize(20);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null,
                android.graphics.Typeface.BOLD);
        titleCol.addView(title);

        TextView sub = new TextView(this);
        sub.setText(
                "Places you've visited light up green!");
        sub.setTextSize(12);
        sub.setTextColor(
                Color.parseColor("#86EFAC"));
        titleCol.addView(sub);

        // Progress bar section
        LinearLayout progressSection =
                new LinearLayout(this);
        progressSection.setOrientation(
                LinearLayout.VERTICAL);
        progressSection.setBackgroundColor(
                Color.parseColor("#112D1B"));
        progressSection.setPadding(20, 16, 20, 16);
        root.addView(progressSection);

        TextView progressLabel = new TextView(this);
        progressLabel.setTextSize(13);
        progressLabel.setTextColor(
                Color.parseColor("#86EFAC"));
        progressSection.addView(progressLabel);

        android.widget.ProgressBar progressBar =
                new android.widget.ProgressBar(this, null,
                        android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(INDIA_PLACES.length);
        LinearLayout.LayoutParams pbp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 16);
        pbp.topMargin = 8;
        progressBar.setLayoutParams(pbp);
        progressBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#1D9E75")));
        progressSection.addView(progressBar);

        // Legend
        LinearLayout legend = new LinearLayout(this);
        legend.setOrientation(LinearLayout.HORIZONTAL);
        legend.setPadding(20, 12, 20, 4);
        root.addView(legend);

        addLegendItem(legend, "#1D9E75", "Visited");
        addLegendItem(legend, "#1A3A2A", "Not yet");

        // Grid of places
        // Wrap grid in a flow layout using
        // multiple horizontal LinearLayouts
        LinearLayout gridContainer =
                new LinearLayout(this);
        gridContainer.setOrientation(
                LinearLayout.VERTICAL);
        gridContainer.setPadding(12, 8, 12, 40);
        root.addView(gridContainer);

        // Load trips to find visited destinations
        TripViewModel viewModel =
                new ViewModelProvider(this)
                        .get(TripViewModel.class);

        viewModel.getAllTrips().observe(this, trips -> {
            // Build set of visited places
            Map<String, Integer> visitCount =
                    new HashMap<>();

            if (trips != null) {
                for (Trip trip : trips) {
                    if (trip.destination == null)
                        continue;
                    String dest = trip.destination
                            .toLowerCase();
                    // Match against known places
                    for (String[] place : INDIA_PLACES) {
                        if (dest.contains(
                                place[0].toLowerCase())) {
                            visitCount.put(place[0],
                                    visitCount.getOrDefault(
                                            place[0], 0) + 1);
                        }
                    }
                }
            }

            int visitedCount = visitCount.size();
            progressBar.setProgress(visitedCount);
            progressLabel.setText(
                    "🌍 " + visitedCount + " of " +
                            INDIA_PLACES.length +
                            " places explored  (" +
                            (visitedCount * 100 /
                                    INDIA_PLACES.length) + "%)");

            // Build place grid
            gridContainer.removeAllViews();
            LinearLayout currentRow = null;
            int itemsInRow = 0;
            int itemsPerRow = 2;

            for (String[] place : INDIA_PLACES) {
                if (currentRow == null ||
                        itemsInRow >= itemsPerRow) {
                    currentRow = new LinearLayout(this);
                    currentRow.setOrientation(
                            LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams rp =
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    rp.bottomMargin = 8;
                    currentRow.setLayoutParams(rp);
                    gridContainer.addView(currentRow);
                    itemsInRow = 0;
                }

                boolean visited = visitCount
                        .containsKey(place[0]);
                int count = visitCount
                        .getOrDefault(place[0], 0);

                currentRow.addView(
                        buildPlaceCard(place[0],
                                place[1], visited, count));
                itemsInRow++;
            }
        });
    }

    private android.view.View buildPlaceCard(
            String place, String type,
            boolean visited, int count) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cp.setMarginEnd(8);
        card.setLayoutParams(cp);

        // Background color
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable
                        .GradientDrawable();
        bg.setShape(android.graphics.drawable
                .GradientDrawable.RECTANGLE);
        bg.setCornerRadius(16);

        if (visited) {
            bg.setColor(Color.parseColor("#1B4332"));
            bg.setStroke(2, Color.parseColor("#1D9E75"));
        } else {
            bg.setColor(Color.parseColor("#1A2A1A"));
            bg.setStroke(1, Color.parseColor("#2A3A2A"));
        }
        card.setBackground(bg);

        // Type emoji
        String typeEmoji = "🏙️";
        if ("Beach".equals(type)) typeEmoji = "🏖️";
        else if ("Trek".equals(type)) typeEmoji = "⛰️";

        TextView emojiView = new TextView(this);
        emojiView.setText(visited ? "✅" : typeEmoji);
        emojiView.setTextSize(24);
        emojiView.setGravity(
                android.view.Gravity.CENTER);
        card.addView(emojiView);

        // Place name
        TextView nameView = new TextView(this);
        nameView.setText(place);
        nameView.setTextSize(13);
        nameView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        nameView.setTextColor(visited ?
                Color.WHITE :
                Color.parseColor("#555555"));
        nameView.setGravity(
                android.view.Gravity.CENTER);
        card.addView(nameView);

        // Visit count
        if (visited) {
            TextView countView = new TextView(this);
            countView.setText(
                    count + " trip" +
                            (count > 1 ? "s" : ""));
            countView.setTextSize(11);
            countView.setTextColor(
                    Color.parseColor("#86EFAC"));
            countView.setGravity(
                    android.view.Gravity.CENTER);
            card.addView(countView);
        } else {
            TextView lockView = new TextView(this);
            lockView.setText("Not visited yet");
            lockView.setTextSize(10);
            lockView.setTextColor(
                    Color.parseColor("#444444"));
            lockView.setGravity(
                    android.view.Gravity.CENTER);
            card.addView(lockView);
        }

        return card;
    }

    private void addLegendItem(LinearLayout parent,
                               String color, String label) {
        View dot = new View(this);
        android.graphics.drawable.GradientDrawable d =
                new android.graphics.drawable
                        .GradientDrawable();
        d.setShape(android.graphics.drawable
                .GradientDrawable.OVAL);
        d.setColor(Color.parseColor(color));
        dot.setBackground(d);
        LinearLayout.LayoutParams dp =
                new LinearLayout.LayoutParams(20, 20);
        dp.setMarginEnd(6);
        dp.topMargin = 2;
        dot.setLayoutParams(dp);
        parent.addView(dot);

        TextView tv = new TextView(this);
        tv.setText(label + "   ");
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#AAAAAA"));
        parent.addView(tv);
    }
}