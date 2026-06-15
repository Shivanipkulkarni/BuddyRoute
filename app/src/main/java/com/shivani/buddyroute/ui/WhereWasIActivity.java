package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WhereWasIActivity extends AppCompatActivity {

    private TripViewModel viewModel;
    private LinearLayout resultLayout;
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
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(
                Color.parseColor("#1D9E75"));
        header.setPadding(20, 50, 20, 20);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        root.addView(header);

        TextView back = new TextView(this);
        back.setText("←  ");
        back.setTextColor(Color.WHITE);
        back.setTextSize(20);
        back.setOnClickListener(v -> finish());
        header.addView(back);

        TextView title = new TextView(this);
        title.setText("📅 Where Was I?");
        title.setTextSize(20);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null,
                android.graphics.Typeface.BOLD);
        header.addView(title);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Tap any date to see where you were");
        subtitle.setTextSize(13);
        subtitle.setTextColor(
                Color.parseColor("#888888"));
        subtitle.setPadding(20, 16, 20, 8);
        root.addView(subtitle);

        // Calendar
        CalendarView calendar = new CalendarView(this);
        root.addView(calendar);

        // Result area
        resultLayout = new LinearLayout(this);
        resultLayout.setOrientation(
                LinearLayout.VERTICAL);
        resultLayout.setPadding(16, 8, 16, 40);
        root.addView(resultLayout);

        // Load all trips
        viewModel = new ViewModelProvider(this)
                .get(TripViewModel.class);

        viewModel.getAllTrips().observe(this, trips -> {
            allTrips = trips;
        });

        // Calendar date selected
        calendar.setOnDateChangeListener(
                (view, year, month, day) -> {
                    // Create date range for selected day
                    java.util.Calendar cal =
                            java.util.Calendar.getInstance();
                    cal.set(year, month, day, 0, 0, 0);
                    long startOfDay = cal.getTimeInMillis();
                    cal.set(year, month, day, 23, 59, 59);
                    long endOfDay = cal.getTimeInMillis();

                    showTripsOnDate(startOfDay, endOfDay,
                            year, month, day);
                });
    }

    private void showTripsOnDate(long startOfDay,
                                 long endOfDay, int year, int month, int day) {

        resultLayout.removeAllViews();

        if (allTrips == null || allTrips.isEmpty()) {
            addResultText("No trips recorded yet!", "#888888");
            return;
        }

        // Find trips that overlap with selected date
        List<Trip> matchingTrips = new java.util.ArrayList<>();
        for (Trip trip : allTrips) {
            long tripEnd = trip.endTime > 0 ?
                    trip.endTime :
                    System.currentTimeMillis();
            // Trip overlaps with selected day
            if (trip.startTime <= endOfDay &&
                    tripEnd >= startOfDay) {
                matchingTrips.add(trip);
            }
        }

        SimpleDateFormat displayDate =
                new SimpleDateFormat("MMMM dd, yyyy",
                        Locale.getDefault());
        java.util.Calendar cal =
                java.util.Calendar.getInstance();
        cal.set(year, month, day);

        // Date header
        TextView dateHeader = new TextView(this);
        dateHeader.setText(
                displayDate.format(cal.getTime()));
        dateHeader.setTextSize(18);
        dateHeader.setTextColor(
                Color.parseColor("#1A1A2E"));
        dateHeader.setTypeface(null,
                android.graphics.Typeface.BOLD);
        dateHeader.setPadding(0, 16, 0, 12);
        resultLayout.addView(dateHeader);

        if (matchingTrips.isEmpty()) {
            addResultText(
                    "You were home on this day! 🏠\n" +
                            "No trips recorded.", "#888888");
            return;
        }

        for (Trip trip : matchingTrips) {
            // Trip card
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.WHITE);
            card.setPadding(20, 20, 20, 20);
            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = 12;
            card.setLayoutParams(cardParams);

            // Color strip
            try {
                View strip = new View(this);
                LinearLayout.LayoutParams sp =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                8);
                strip.setLayoutParams(sp);
                strip.setBackgroundColor(
                        Color.parseColor(trip.colorTheme));
                card.addView(strip);
            } catch (Exception ignored) {}

            LinearLayout content =
                    new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(0, 12, 0, 0);
            card.addView(content);

            // Trip name
            TextView name = new TextView(this);
            name.setText(getTripEmoji(trip.tripType)
                    + " " + trip.name);
            name.setTextSize(17);
            name.setTypeface(null,
                    android.graphics.Typeface.BOLD);
            name.setTextColor(
                    Color.parseColor("#1A1A2E"));
            content.addView(name);

            // Destination
            TextView dest = new TextView(this);
            dest.setText("📍 " + trip.destination);
            dest.setTextSize(14);
            dest.setTextColor(
                    Color.parseColor("#666666"));
            dest.setPadding(0, 4, 0, 8);
            content.addView(dest);

            // Load notes for this trip
            int finalI = matchingTrips.indexOf(trip);
            viewModel.getNotesForTrip(trip.id)
                    .observe(this, notes -> {
                        // Photos count
                        int photoCount = 0;
                        if (notes != null) {
                            for (TripNote n : notes) {
                                if (n.photoPath != null)
                                    photoCount++;
                            }
                        }

                        // Stats row
                        LinearLayout statsRow =
                                new LinearLayout(this);
                        statsRow.setOrientation(
                                LinearLayout.HORIZONTAL);

                        addMiniStat(statsRow, "📸",
                                photoCount + " photos");
                        addMiniStat(statsRow, "📝",
                                trip.notesCount + " notes");
                        addMiniStat(statsRow, "🚶",
                                formatDist(trip.totalDistance));

                        content.addView(statsRow);
                    });

            // Open trip on click
            card.setOnClickListener(v -> {
                android.content.Intent intent =
                        new android.content.Intent(this,
                                TripDetailActivity.class);
                intent.putExtra("TRIP_ID", trip.id);
                startActivity(intent);
            });

            resultLayout.addView(card);
        }
    }

    private void addMiniStat(LinearLayout parent,
                             String emoji, String text) {
        TextView tv = new TextView(this);
        tv.setText(emoji + " " + text + "   ");
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#888888"));
        parent.addView(tv);
    }

    private void addResultText(String text,
                               String color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTextColor(Color.parseColor(color));
        tv.setPadding(0, 12, 0, 12);
        resultLayout.addView(tv);
    }

    private String getTripEmoji(String type) {
        if (type == null) return "🗺️";
        switch (type) {
            case "Beach": return "🏖️";
            case "Trek":  return "🥾";
            case "City":  return "🏙️";
            default:      return "🚗";
        }
    }

    private String formatDist(float km) {
        if (km < 1) return (int)(km * 1000) + " m";
        return String.format(Locale.getDefault(),
                "%.1f km", km);
    }
}