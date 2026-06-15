package com.shivani.buddyroute.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shivani.buddyroute.R;
import com.shivani.buddyroute.model.TripNote;

import java.util.ArrayList;
import java.util.List;

public class PhotoGridAdapter extends
        RecyclerView.Adapter<PhotoGridAdapter.PhotoViewHolder> {

    private List<TripNote> photosOnly = new ArrayList<>();

    public void setNotes(List<TripNote> allNotes) {
        // Filter — only notes that have a photo
        photosOnly.clear();
        if (allNotes != null) {
            for (TripNote note : allNotes) {
                if (note.photoPath != null) {
                    photosOnly.add(note);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getPhotoCount() {
        return photosOnly.size();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_grid, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PhotoViewHolder holder, int position) {
        TripNote note = photosOnly.get(position);

        // Load photo with Glide — handles caching and smooth loading
        Glide.with(holder.ivPhoto.getContext())
                .load(new java.io.File(note.photoPath))
                .centerCrop()
                .placeholder(R.color.brand_green_light)
                .into(holder.ivPhoto);

        // Mood emoji on top of photo
        holder.tvMood.setText(getMoodEmoji(note.mood));
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
        return photosOnly.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvMood;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivGridPhoto);
            tvMood = itemView.findViewById(R.id.tvGridMood);
        }
    }
}