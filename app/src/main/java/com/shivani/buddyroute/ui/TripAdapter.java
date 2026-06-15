package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.shivani.buddyroute.R;
import com.shivani.buddyroute.model.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips = new ArrayList<>();
    private final OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    // Called when new trip data comes in — refreshes the list
    public void setTrips(List<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.tripName.setText(trip.name);
        holder.tripDestination.setText("📍 " + trip.destination);
        holder.tripType.setText(trip.tripType);

        // Distance
        if (trip.totalDistance < 1) {
            holder.tripDistance.setText("📏 " + (int)(trip.totalDistance * 1000) + " m");
        } else {
            holder.tripDistance.setText(String.format(Locale.getDefault(),
                    "📏 %.1f km", trip.totalDistance));
        }

        // Notes count
        holder.tripNotes.setText("📝 " + trip.notesCount + " notes");

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.tripDate.setText(sdf.format(new Date(trip.startTime)));

        // Color dot — uses the trip's theme color
        try {
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(Color.parseColor(trip.colorTheme));
            dot.setSize(28, 28);
            holder.colorDot.setBackground(dot);

            // Badge color matches too
            GradientDrawable badge = new GradientDrawable();
            badge.setShape(GradientDrawable.RECTANGLE);
            badge.setColor(Color.parseColor(trip.colorTheme));
            badge.setCornerRadius(40);
            holder.tripType.setBackground(badge);
        } catch (Exception e) {
            // fallback color if hex is invalid
        }

        // Active trip — show green border
        if (trip.endTime == 0) {
            holder.cardView.setStrokeColor(Color.parseColor("#1D9E75"));
            holder.cardView.setStrokeWidth(3);
            holder.tripName.setText("🟢 " + trip.name);
        } else {
            holder.cardView.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        View colorDot;
        TextView tripName, tripDestination, tripType;
        TextView tripDistance, tripNotes, tripDate;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            colorDot = itemView.findViewById(R.id.colorDot);
            tripName = itemView.findViewById(R.id.tripName);
            tripDestination = itemView.findViewById(R.id.tripDestination);
            tripType = itemView.findViewById(R.id.tripType);
            tripDistance = itemView.findViewById(R.id.tripDistance);
            tripNotes = itemView.findViewById(R.id.tripNotes);
            tripDate = itemView.findViewById(R.id.tripDate);
        }
    }
    public Trip getTripAt(int position) {
        return trips.get(position);
    }
}