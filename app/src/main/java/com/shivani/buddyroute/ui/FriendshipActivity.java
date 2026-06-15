package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.util.List;
import java.util.Locale;

public class FriendshipActivity extends AppCompatActivity {

    private TripViewModel viewModel;
    private LinearLayout resultLayout;
    private EditText etFriendName;
    private List<Trip> allTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(
                Color.parseColor("#F0F4F8"));
        setContentView(scroll);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setBackgroundColor(
                Color.parseColor("#D4537E"));
        header.setPadding(20, 50, 20, 20);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
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
        title.setText("👫 Friendship Score");
        title.setTextSize(20);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null,
                android.graphics.Typeface.BOLD);
        titleCol.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "See how compatible you are as travel buddies!");
        subtitle.setTextSize(12);
        subtitle.setTextColor(
                Color.parseColor("#FFCCDD"));
        titleCol.addView(subtitle);

        // Input section
        LinearLayout inputSection =
                new LinearLayout(this);
        inputSection.setOrientation(
                LinearLayout.VERTICAL);
        inputSection.setPadding(24, 24, 24, 8);
        inputSection.setBackgroundColor(Color.WHITE);
        root.addView(inputSection);

        TextView yourName = new TextView(this);
        yourName.setText("Your name");
        yourName.setTextSize(13);
        yourName.setTextColor(
                Color.parseColor("#888888"));
        inputSection.addView(yourName);

        EditText etYourName = new EditText(this);
        etYourName.setHint("e.g. Shivani");
        etYourName.setTextSize(15);
        LinearLayout.LayoutParams ep =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        ep.bottomMargin = 16;
        etYourName.setLayoutParams(ep);
        inputSection.addView(etYourName);

        TextView friendLabel = new TextView(this);
        friendLabel.setText("Friend's name");
        friendLabel.setTextSize(13);
        friendLabel.setTextColor(
                Color.parseColor("#888888"));
        inputSection.addView(friendLabel);

        etFriendName = new EditText(this);
        etFriendName.setHint("e.g. Sneha");
        etFriendName.setTextSize(15);
        etFriendName.setLayoutParams(ep);
        inputSection.addView(etFriendName);

        // How many trips together
        TextView tripsLabel = new TextView(this);
        tripsLabel.setText(
                "How many trips have you done together?");
        tripsLabel.setTextSize(13);
        tripsLabel.setTextColor(
                Color.parseColor("#888888"));
        inputSection.addView(tripsLabel);

        EditText etTripsCount = new EditText(this);
        etTripsCount.setHint("e.g. 5");
        etTripsCount.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER);
        etTripsCount.setTextSize(15);
        etTripsCount.setLayoutParams(ep);
        inputSection.addView(etTripsCount);

        // Calculate button
        Button btnCalc = new Button(this);
        btnCalc.setText("💕 Calculate Friendship Score");
        btnCalc.setTextColor(Color.WHITE);
        btnCalc.setBackgroundColor(
                Color.parseColor("#D4537E"));
        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 130);
        bp.topMargin = 8;
        btnCalc.setLayoutParams(bp);
        inputSection.addView(btnCalc);

        // Result layout
        resultLayout = new LinearLayout(this);
        resultLayout.setOrientation(
                LinearLayout.VERTICAL);
        resultLayout.setPadding(16, 16, 16, 40);
        root.addView(resultLayout);

        viewModel = new ViewModelProvider(this)
                .get(TripViewModel.class);
        viewModel.getAllTrips().observe(this,
                trips -> allTrips = trips);

        btnCalc.setOnClickListener(v -> {
            String yourN = etYourName.getText()
                    .toString().trim();
            String friendN = etFriendName.getText()
                    .toString().trim();
            String tripsStr = etTripsCount.getText()
                    .toString().trim();

            if (yourN.isEmpty()) yourN = "You";
            if (friendN.isEmpty()) {
                etFriendName.setError(
                        "Enter friend's name!");
                return;
            }
            int sharedTrips = tripsStr.isEmpty() ?
                    1 : Integer.parseInt(tripsStr);

            calculateAndShow(yourN, friendN,
                    sharedTrips);
        });
    }

    private void calculateAndShow(String yourName,
                                  String friendName, int sharedTrips) {

        resultLayout.removeAllViews();

        int totalTrips = allTrips != null ?
                allTrips.size() : 0;
        float totalKm = 0;
        int totalNotes = 0;
        int totalPhotos = 0;

        if (allTrips != null) {
            for (Trip t : allTrips) {
                totalKm += t.totalDistance;
                totalNotes += t.notesCount;
            }
        }

        // Calculate compatibility score
        // Based on shared trips, notes, distance
        int baseScore = 50;
        if (sharedTrips >= 1) baseScore += 10;
        if (sharedTrips >= 3) baseScore += 10;
        if (sharedTrips >= 5) baseScore += 10;
        if (totalNotes > 10) baseScore += 5;
        if (totalKm > 50) baseScore += 5;

        // Add name compatibility (fun algorithm)
        int nameScore = (yourName.length() +
                friendName.length()) % 20;
        baseScore = Math.min(99, baseScore + nameScore);

        // Score label
        String scoreLabel;
        String scoreEmoji;
        String scoreColor;
        String description;

        if (baseScore >= 85) {
            scoreLabel = "Soulmate Travellers! 💕";
            scoreEmoji = "💕";
            scoreColor = "#D4537E";
            description = "You two are made for travelling together. Same energy, same vibe, same adventure!";
        } else if (baseScore >= 70) {
            scoreLabel = "Adventure Duo! 🌟";
            scoreEmoji = "🌟";
            scoreColor = "#BA7517";
            description = "Great compatibility! You motivate each other to explore more. Keep going!";
        } else if (baseScore >= 55) {
            scoreLabel = "Travel Buddies 🤝";
            scoreEmoji = "🤝";
            scoreColor = "#1D9E75";
            description = "You work well together! A few more trips and you'll be inseparable.";
        } else {
            scoreLabel = "New Friends 🌱";
            scoreEmoji = "🌱";
            scoreColor = "#534AB7";
            description = "Every great friendship starts somewhere! Your first adventure together awaits.";
        }

        // Big score circle display
        LinearLayout scoreSection =
                new LinearLayout(this);
        scoreSection.setOrientation(
                LinearLayout.VERTICAL);
        scoreSection.setGravity(
                android.view.Gravity.CENTER);
        scoreSection.setBackgroundColor(Color.WHITE);
        scoreSection.setPadding(20, 40, 20, 40);
        LinearLayout.LayoutParams ssp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        ssp.bottomMargin = 12;
        scoreSection.setLayoutParams(ssp);
        resultLayout.addView(scoreSection);

        // Names header
        TextView namesView = new TextView(this);
        namesView.setText(
                yourName + " + " + friendName);
        namesView.setTextSize(22);
        namesView.setTextColor(
                Color.parseColor("#1A1A2E"));
        namesView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        namesView.setGravity(
                android.view.Gravity.CENTER);
        namesView.setPadding(0, 0, 0, 20);
        scoreSection.addView(namesView);

        // Score number
        TextView scoreNum = new TextView(this);
        scoreNum.setText(baseScore + "%");
        scoreNum.setTextSize(64);
        scoreNum.setTextColor(
                Color.parseColor(scoreColor));
        scoreNum.setTypeface(null,
                android.graphics.Typeface.BOLD);
        scoreNum.setGravity(
                android.view.Gravity.CENTER);
        scoreSection.addView(scoreNum);

        // Score bar
        ProgressBar bar = new ProgressBar(this,
                null,
                android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(baseScore);
        bar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.parseColor(scoreColor)));
        LinearLayout.LayoutParams barp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 20);
        barp.topMargin = 12;
        barp.bottomMargin = 16;
        bar.setLayoutParams(barp);
        scoreSection.addView(bar);

        // Score label
        TextView scoreLabelView = new TextView(this);
        scoreLabelView.setText(scoreLabel);
        scoreLabelView.setTextSize(18);
        scoreLabelView.setTextColor(
                Color.parseColor(scoreColor));
        scoreLabelView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        scoreLabelView.setGravity(
                android.view.Gravity.CENTER);
        scoreSection.addView(scoreLabelView);

        // Description
        TextView descView = new TextView(this);
        descView.setText(description);
        descView.setTextSize(14);
        descView.setTextColor(
                Color.parseColor("#666666"));
        descView.setGravity(
                android.view.Gravity.CENTER);
        descView.setPadding(20, 8, 20, 0);
        scoreSection.addView(descView);

        // Stats grid
        LinearLayout statsGrid =
                new LinearLayout(this);
        statsGrid.setOrientation(
                LinearLayout.VERTICAL);
        statsGrid.setBackgroundColor(Color.WHITE);
        statsGrid.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams sgp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        sgp.bottomMargin = 12;
        statsGrid.setLayoutParams(sgp);
        resultLayout.addView(statsGrid);

        addStatRow(statsGrid, "🗺️ Trips Together",
                String.valueOf(sharedTrips));
        addStatRow(statsGrid, "📏 Total Distance",
                String.format(Locale.getDefault(),
                        "%.1f km", totalKm));
        addStatRow(statsGrid, "📝 Memories Made",
                String.valueOf(totalNotes));
        addStatRow(statsGrid,
                "🧭 Travel Compatibility",
                baseScore + "%");

        // Share card
        LinearLayout shareCard =
                new LinearLayout(this);
        shareCard.setOrientation(
                LinearLayout.VERTICAL);
        shareCard.setGravity(
                android.view.Gravity.CENTER);
        shareCard.setBackgroundColor(
                Color.parseColor(scoreColor));
        shareCard.setPadding(20, 30, 20, 30);
        resultLayout.addView(shareCard);

        TextView shareText = new TextView(this);
        shareText.setText(
                scoreEmoji + " " + yourName +
                        " + " + friendName + "\n" +
                        "Travel Compatibility: " +
                        baseScore + "%\n" + scoreLabel +
                        "\n\n#BuddyRoute #TravelBuddies");
        shareText.setTextSize(15);
        shareText.setTextColor(Color.WHITE);
        shareText.setGravity(
                android.view.Gravity.CENTER);
        shareText.setPadding(0, 0, 0, 16);
        shareCard.addView(shareText);

        Button shareBtn = new Button(this);
        shareBtn.setText("📤 Share Screenshot");
        shareBtn.setTextColor(
                Color.parseColor(scoreColor));
        shareBtn.setBackgroundColor(Color.WHITE);
        shareCard.addView(shareBtn);

        final int finalBaseScore = baseScore;

        shareBtn.setOnClickListener(v -> {
            // Share as text
            android.content.Intent shareIntent =
                    new android.content.Intent(
                            android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(
                    android.content.Intent.EXTRA_TEXT,
                    scoreEmoji + " " + yourName +
                            " + " + friendName +
                            " Travel Compatibility: " +
                            finalBaseScore + "%!\n" + scoreLabel +
                            "\n\nTracked with BuddyRoute 🗺️");
            startActivity(
                    android.content.Intent.createChooser(
                            shareIntent, "Share your score!"));
        });
    }

    private void addStatRow(LinearLayout parent,
                            String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 10, 0, 10);
        LinearLayout.LayoutParams rp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(rp);

        // Divider
        View divider = new View(this);
        divider.setBackgroundColor(
                Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams dp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
        dp.bottomMargin = 2;

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(14);
        labelView.setTextColor(
                Color.parseColor("#555555"));
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelView.setLayoutParams(lp);
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(14);
        valueView.setTextColor(
                Color.parseColor("#1A1A2E"));
        valueView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        row.addView(valueView);

        parent.addView(row);
    }
}