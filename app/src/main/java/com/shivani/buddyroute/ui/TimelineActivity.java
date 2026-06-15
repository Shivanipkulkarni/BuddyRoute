package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.model.Waypoint;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimelineActivity extends AppCompatActivity {

    private TripViewModel viewModel;
    private int tripId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tripId = getIntent().getIntExtra("TRIP_ID", -1);
        viewModel = new ViewModelProvider(this)
                .get(TripViewModel.class);

        // Root scroll view
        android.widget.ScrollView scroll =
                new android.widget.ScrollView(this);
        scroll.setBackgroundColor(
                Color.parseColor("#0D1117"));
        setContentView(scroll);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);

        // Load trip
        viewModel.getTripById(tripId).observe(this, trip -> {
            if (trip == null) return;
            root.removeAllViews();
            buildTimeline(root, trip);
        });
    }

    private void buildTimeline(LinearLayout root, Trip trip) {
        // ── HERO SECTION ──
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(android.view.Gravity.CENTER);
        hero.setPadding(40, 80, 40, 60);
        try {
            hero.setBackgroundColor(
                    Color.parseColor(trip.colorTheme));
        } catch (Exception e) {
            hero.setBackgroundColor(
                    Color.parseColor("#1D9E75"));
        }
        root.addView(hero);

        // Back button
        TextView back = new TextView(this);
        back.setText("← Back");
        back.setTextColor(Color.WHITE);
        back.setTextSize(14);
        back.setPadding(0, 0, 0, 20);
        back.setOnClickListener(v -> finish());
        hero.addView(back);

        // Trip emoji based on type
        TextView emojiView = new TextView(this);
        emojiView.setText(getTripEmoji(trip.tripType));
        emojiView.setTextSize(56);
        emojiView.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams ep =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        emojiView.setLayoutParams(ep);
        hero.addView(emojiView);

        // Trip name
        TextView nameView = new TextView(this);
        nameView.setText(trip.name);
        nameView.setTextSize(28);
        nameView.setTextColor(Color.WHITE);
        nameView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        nameView.setGravity(android.view.Gravity.CENTER);
        nameView.setPadding(0, 12, 0, 4);
        hero.addView(nameView);

        // Destination
        TextView destView = new TextView(this);
        destView.setText("📍 " + trip.destination);
        destView.setTextSize(15);
        destView.setTextColor(Color.parseColor("#CCFFFFFF"));
        destView.setGravity(android.view.Gravity.CENTER);
        hero.addView(destView);

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMMM yyyy", Locale.getDefault());
        TextView dateView = new TextView(this);
        dateView.setText("🗓️ " +
                sdf.format(new Date(trip.startTime)));
        dateView.setTextSize(13);
        dateView.setTextColor(
                Color.parseColor("#99FFFFFF"));
        dateView.setGravity(android.view.Gravity.CENTER);
        dateView.setPadding(0, 4, 0, 0);
        hero.addView(dateView);

        // ── QUICK STATS ROW ──
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setBackgroundColor(
                Color.parseColor("#161B22"));
        statsRow.setPadding(0, 20, 0, 20);
        root.addView(statsRow);

        long durationMs = trip.endTime > 0 ?
                trip.endTime - trip.startTime : 0;
        long hours = TimeUnit.MILLISECONDS
                .toHours(durationMs);
        long mins = TimeUnit.MILLISECONDS
                .toMinutes(durationMs) % 60;
        String duration = hours > 0 ?
                hours + "h " + mins + "m" : mins + " mins";

        addStatChip(statsRow, "🚶",
                formatDist(trip.totalDistance),
                "Distance");
        addStatChip(statsRow, "📝",
                String.valueOf(trip.notesCount), "Notes");
        addStatChip(statsRow, "⏱️",
                trip.endTime > 0 ? duration : "Active",
                "Duration");

        // ── TIMELINE HEADER ──
        TextView tlHeader = new TextView(this);
        tlHeader.setText("  Your Journey");
        tlHeader.setTextSize(18);
        tlHeader.setTextColor(Color.WHITE);
        tlHeader.setTypeface(null,
                android.graphics.Typeface.BOLD);
        tlHeader.setPadding(30, 30, 30, 10);
        tlHeader.setBackgroundColor(
                Color.parseColor("#0D1117"));
        root.addView(tlHeader);

        // ── NOTES TIMELINE ──
        viewModel.getNotesForTrip(tripId)
                .observe(this, notes -> {
                    if (notes == null) return;

                    // Remove old notes views if reloading
                    if (root.getChildCount() > 4) {
                        root.removeViews(4,
                                root.getChildCount() - 4);
                    }

                    if (notes.isEmpty()) {
                        TextView noNotes = new TextView(this);
                        noNotes.setText(
                                "  No notes on this trip yet.\n" +
                                        "  Add notes during your next adventure!");
                        noNotes.setTextColor(
                                Color.parseColor("#666666"));
                        noNotes.setTextSize(14);
                        noNotes.setPadding(30, 20, 30, 20);
                        noNotes.setBackgroundColor(
                                Color.parseColor("#0D1117"));
                        root.addView(noNotes);
                    } else {
                        for (int i = 0; i < notes.size(); i++) {
                            root.addView(buildNoteCard(
                                    notes.get(i), i, notes.size(),
                                    trip.colorTheme));
                        }
                    }

                    // ── END CARD ──
                    root.addView(buildEndCard(trip, notes));
                });
    }

    private View buildNoteCard(TripNote note,
                               int index, int total, String colorTheme) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundColor(
                Color.parseColor("#0D1117"));
        card.setPadding(20, 10, 20, 10);

        // Left — timeline line + dot
        LinearLayout lineCol = new LinearLayout(this);
        lineCol.setOrientation(LinearLayout.VERTICAL);
        lineCol.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams lcp =
                new LinearLayout.LayoutParams(40,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        lineCol.setLayoutParams(lcp);
        card.addView(lineCol);

        // Dot
        View dot = new View(this);
        LinearLayout.LayoutParams dp =
                new LinearLayout.LayoutParams(18, 18);
        dp.topMargin = 16;
        dot.setLayoutParams(dp);
        try {
            android.graphics.drawable.GradientDrawable
                    circle = new android.graphics.drawable
                    .GradientDrawable();
            circle.setShape(
                    android.graphics.drawable
                            .GradientDrawable.OVAL);
            circle.setColor(
                    Color.parseColor(colorTheme));
            dot.setBackground(circle);
        } catch (Exception e) {
            dot.setBackgroundColor(
                    Color.parseColor("#1D9E75"));
        }
        lineCol.addView(dot);

        // Line below dot
        if (index < total - 1) {
            View line = new View(this);
            LinearLayout.LayoutParams llp =
                    new LinearLayout.LayoutParams(2,
                            ViewGroup.LayoutParams.MATCH_PARENT);
            llp.topMargin = 4;
            line.setLayoutParams(llp);
            line.setBackgroundColor(
                    Color.parseColor("#333333"));
            lineCol.addView(line);
        }

        // Right — content
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cp.setMarginStart(16);
        content.setLayoutParams(cp);

        // Card background
        content.setBackgroundColor(
                Color.parseColor("#161B22"));
        content.setPadding(16, 16, 16, 16);

        // Round corners via margin
        LinearLayout.LayoutParams contentCardParams =
                (LinearLayout.LayoutParams)
                        content.getLayoutParams();
        contentCardParams.bottomMargin = 12;
        content.setLayoutParams(contentCardParams);

        // Time + mood
        SimpleDateFormat sdf = new SimpleDateFormat(
                "hh:mm a", Locale.getDefault());
        TextView timeView = new TextView(this);
        timeView.setText(getMoodEmoji(note.mood) + "  " +
                sdf.format(new Date(note.timestamp)));
        timeView.setTextSize(11);
        timeView.setTextColor(
                Color.parseColor("#888888"));
        content.addView(timeView);

        // Note text
        TextView noteText = new TextView(this);
        noteText.setText(note.noteText);
        noteText.setTextSize(14);
        noteText.setTextColor(Color.WHITE);
        noteText.setPadding(0, 8, 0, 8);
        content.addView(noteText);

        // Photo
        if (note.photoPath != null) {
            ImageView photo = new ImageView(this);
            LinearLayout.LayoutParams pp =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            300);
            pp.topMargin = 8;
            photo.setLayoutParams(pp);
            photo.setScaleType(
                    ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(new File(note.photoPath))
                    .centerCrop()
                    .into(photo);
            content.addView(photo);
        }

        card.addView(content);
        return card;
    }

    private View buildEndCard(Trip trip,
                              List<TripNote> notes) {
        LinearLayout end = new LinearLayout(this);
        end.setOrientation(LinearLayout.VERTICAL);
        end.setGravity(android.view.Gravity.CENTER);
        end.setBackgroundColor(
                Color.parseColor("#161B22"));
        end.setPadding(40, 40, 40, 80);

        // Count photos
        int photoCount = 0;
        for (TripNote n : notes) {
            if (n.photoPath != null) photoCount++;
        }

        // Summary text
        TextView summary = new TextView(this);
        summary.setText(
                "\"Your " + trip.destination +
                        " adventure\"");
        summary.setTextSize(20);
        summary.setTextColor(Color.WHITE);
        summary.setTypeface(null,
                android.graphics.Typeface.BOLD_ITALIC);
        summary.setGravity(android.view.Gravity.CENTER);
        summary.setPadding(0, 0, 0, 20);
        end.addView(summary);

        // Stats summary
        String[] statLines = {
                "📍 " + trip.destination,
                "📸 " + photoCount + " photos",
                "🚶 Walked " + formatDist(trip.totalDistance),
                "📝 " + trip.notesCount + " notes written",
        };

        for (String line : statLines) {
            TextView tv = new TextView(this);
            tv.setText(line);
            tv.setTextSize(15);
            tv.setTextColor(
                    Color.parseColor("#CCCCCC"));
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(0, 6, 0, 6);
            end.addView(tv);
        }

        // BuddyRoute watermark
        TextView watermark = new TextView(this);
        watermark.setText("\nMade with BuddyRoute 🗺️");
        watermark.setTextSize(12);
        watermark.setTextColor(
                Color.parseColor("#444444"));
        watermark.setGravity(android.view.Gravity.CENTER);
        end.addView(watermark);

        return end;
    }

    private void addStatChip(LinearLayout parent,
                             String emoji, String value, String label) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.VERTICAL);
        chip.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        chip.setLayoutParams(p);

        TextView emojiView = new TextView(this);
        emojiView.setText(emoji);
        emojiView.setTextSize(20);
        emojiView.setGravity(android.view.Gravity.CENTER);
        chip.addView(emojiView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(16);
        valueView.setTextColor(Color.WHITE);
        valueView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        valueView.setGravity(android.view.Gravity.CENTER);
        chip.addView(valueView);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(10);
        labelView.setTextColor(
                Color.parseColor("#888888"));
        labelView.setGravity(android.view.Gravity.CENTER);
        chip.addView(labelView);

        parent.addView(chip);
    }

    private String getTripEmoji(String type) {
        if (type == null) return "🗺️";
        switch (type) {
            case "Beach":  return "🏖️";
            case "Trek":   return "🥾";
            case "City":   return "🏙️";
            default:       return "🚗";
        }
    }

    private String getMoodEmoji(String mood) {
        if (mood == null) return "😄";
        switch (mood) {
            case "amazed":   return "🤩";
            case "tired":    return "😴";
            case "hungry":   return "🍔";
            case "peaceful": return "😌";
            default:         return "😄";
        }
    }

    private String formatDist(float km) {
        if (km < 1) return (int)(km * 1000) + " m";
        return String.format(Locale.getDefault(),
                "%.1f km", km);
    }
}