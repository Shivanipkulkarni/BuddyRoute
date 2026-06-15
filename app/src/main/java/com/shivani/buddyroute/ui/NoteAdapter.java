package com.shivani.buddyroute.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.shivani.buddyroute.R;
import com.shivani.buddyroute.model.TripNote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<TripNote> notes = new ArrayList<>();

    public void setNotes(List<TripNote> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        TripNote note = notes.get(position);

        holder.tvNoteText.setText(note.noteText);
        holder.tvMoodEmoji.setText(getMoodEmoji(note.mood));

        SimpleDateFormat sdf = new SimpleDateFormat(
                "hh:mm a, dd MMM", Locale.getDefault());
        holder.tvNoteTime.setText(sdf.format(new Date(note.timestamp)));

        // Photo
        if (note.photoPath != null) {
            holder.ivNotePhoto.setVisibility(View.VISIBLE);
            try {
                com.bumptech.glide.Glide.with(holder.ivNotePhoto.getContext())
                        .load(android.net.Uri.parse(note.photoPath))
                        .centerCrop()
                        .into(holder.ivNotePhoto);
            } catch (Exception e) {
                holder.ivNotePhoto.setVisibility(View.GONE);
            }
        } else {
            holder.ivNotePhoto.setVisibility(View.GONE);
        }

        // Voice note
        if (note.voiceNotePath != null) {
            holder.voiceLayout.setVisibility(View.VISIBLE);
            holder.btnPlay.setOnClickListener(v -> {
                playVoice(note.voiceNotePath, holder.btnPlay);
            });
        } else {
            holder.voiceLayout.setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoodEmoji, tvNoteTime, tvNoteText, btnPlay;
        ImageView ivNotePhoto;
        android.view.View voiceLayout;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodEmoji = itemView.findViewById(R.id.tvMoodEmoji);
            tvNoteTime = itemView.findViewById(R.id.tvNoteTime);
            tvNoteText = itemView.findViewById(R.id.tvNoteText);
            ivNotePhoto = itemView.findViewById(R.id.ivNotePhoto);
            voiceLayout = itemView.findViewById(R.id.voiceNoteLayout);
            btnPlay = itemView.findViewById(R.id.btnPlayVoice);
        }
    }
    private android.media.MediaPlayer currentPlayer = null;

    private void playVoice(String path, TextView btnPlay) {
        // Stop any existing playback
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.release();
            currentPlayer = null;
        }

        try {
            currentPlayer = new android.media.MediaPlayer();
            currentPlayer.setDataSource(path);
            currentPlayer.prepare();
            currentPlayer.start();
            btnPlay.setText("⏹");

            currentPlayer.setOnCompletionListener(mp -> {
                btnPlay.setText("▶");
                mp.release();
                currentPlayer = null;
            });
        } catch (Exception e) {
            btnPlay.setText("▶");
        }
    }
}