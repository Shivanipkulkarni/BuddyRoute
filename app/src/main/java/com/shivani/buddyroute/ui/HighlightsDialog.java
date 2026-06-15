package com.shivani.buddyroute.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shivani.buddyroute.R;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HighlightsDialog {

    public static void show(Context context, Trip trip,
                            List<TripNote> notes, Runnable onViewTrip) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_highlights, null);
        dialog.setContentView(view);

        // Make dialog wider
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int)(context.getResources()
                            .getDisplayMetrics().widthPixels * 0.92f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Subtitle
        TextView tvSubtitle = view.findViewById(R.id.tvHighlightSubtitle);
        tvSubtitle.setText("Here's what made " + trip.name + " special");

        // Stats
        TextView tvDist = view.findViewById(R.id.tvHighlightDistance);
        TextView tvNotes = view.findViewById(R.id.tvHighlightNotes);

        if (trip.totalDistance < 1) {
            tvDist.setText((int)(trip.totalDistance * 1000) + " m");
        } else {
            tvDist.setText(String.format(
                    Locale.getDefault(), "%.1f km", trip.totalDistance));
        }
        tvNotes.setText(String.valueOf(trip.notesCount));

        // Pick top 3 highlights
        // Priority: notes with photos first, then by text length
        List<TripNote> sorted = new java.util.ArrayList<>(notes);
        sorted.sort((a, b) -> {
            int aScore = (a.photoPath != null ? 10 : 0) + a.noteText.length();
            int bScore = (b.photoPath != null ? 10 : 0) + b.noteText.length();
            return bScore - aScore; // descending
        });

        int[] moodIds = {R.id.tvMood1, R.id.tvMood2, R.id.tvMood3};
        int[] noteIds = {R.id.tvNote1, R.id.tvNote2, R.id.tvNote3};
        int[] timeIds = {R.id.tvTime1, R.id.tvTime2, R.id.tvTime3};
        int[] layoutIds = {
                R.id.highlight1Layout,
                R.id.highlight2Layout,
                R.id.highlight3Layout
        };

        SimpleDateFormat sdf = new SimpleDateFormat(
                "hh:mm a", Locale.getDefault());

        for (int i = 0; i < 3; i++) {
            LinearLayout layout = view.findViewById(layoutIds[i]);
            if (i < sorted.size()) {
                layout.setVisibility(View.VISIBLE);
                TripNote note = sorted.get(i);

                ((TextView) view.findViewById(moodIds[i]))
                        .setText(getMoodEmoji(note.mood));
                ((TextView) view.findViewById(noteIds[i]))
                        .setText(note.noteText);
                ((TextView) view.findViewById(timeIds[i]))
                        .setText(sdf.format(new Date(note.timestamp)));
            } else {
                layout.setVisibility(View.GONE);
            }
        }

        // Buttons
        view.findViewById(R.id.btnViewFullTrip).setOnClickListener(v -> {
            dialog.dismiss();
            if (onViewTrip != null) onViewTrip.run();
        });

        view.findViewById(R.id.btnDone).setOnClickListener(v ->
                dialog.dismiss());

        dialog.show();
    }

    private static String getMoodEmoji(String mood) {
        if (mood == null) return "😄";
        switch (mood) {
            case "amazed":   return "🤩";
            case "tired":    return "😴";
            case "hungry":   return "🍔";
            case "peaceful": return "😌";
            default:         return "😄";
        }
    }
}