package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shivani.buddyroute.databinding.ActivityCompareBinding;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompareActivity extends AppCompatActivity {

    private ActivityCompareBinding binding;
    private TripViewModel viewModel;
    private List<Trip> allTrips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        binding.backButton.setOnClickListener(v -> finish());

        // Load all trips into spinners
        viewModel.getAllTrips().observe(this, trips -> {
            if (trips == null || trips.size() < 2) {
                Toast.makeText(this,
                        "You need at least 2 trips to compare!",
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            allTrips = trips;

            List<String> names = new ArrayList<>();
            for (Trip t : trips) names.add(t.name);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    names);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);

            binding.spinnerTrip1.setAdapter(adapter);
            binding.spinnerTrip2.setAdapter(adapter);

            // Default second spinner to second trip
            if (trips.size() > 1) {
                binding.spinnerTrip2.setSelection(1);
            }
        });

        binding.btnCompare.setOnClickListener(v -> compareTrips());
    }

    private void compareTrips() {
        int pos1 = binding.spinnerTrip1.getSelectedItemPosition();
        int pos2 = binding.spinnerTrip2.getSelectedItemPosition();

        if (pos1 == pos2) {
            Toast.makeText(this,
                    "Please select two different trips!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Trip t1 = allTrips.get(pos1);
        Trip t2 = allTrips.get(pos2);

        binding.resultsLayout.setVisibility(View.VISIBLE);

        // Trip names
        binding.tvTrip1Name.setText(t1.name);
        binding.tvTrip2Name.setText(t2.name);

        // Distance
        String d1 = formatDist(t1.totalDistance);
        String d2 = formatDist(t2.totalDistance);
        binding.tvDist1.setText(d1);
        binding.tvDist2.setText(d2);

        // Highlight winner in green
        if (t1.totalDistance > t2.totalDistance) {
            binding.tvDist1.setTextColor(Color.parseColor("#1D9E75"));
            binding.tvDist2.setTextColor(Color.parseColor("#333333"));
        } else if (t2.totalDistance > t1.totalDistance) {
            binding.tvDist2.setTextColor(Color.parseColor("#1D9E75"));
            binding.tvDist1.setTextColor(Color.parseColor("#333333"));
        }

        // Notes
        binding.tvNotes1.setText(String.valueOf(t1.notesCount));
        binding.tvNotes2.setText(String.valueOf(t2.notesCount));
        if (t1.notesCount > t2.notesCount) {
            binding.tvNotes1.setTextColor(Color.parseColor("#1D9E75"));
            binding.tvNotes2.setTextColor(Color.parseColor("#333333"));
        } else if (t2.notesCount > t1.notesCount) {
            binding.tvNotes2.setTextColor(Color.parseColor("#1D9E75"));
            binding.tvNotes1.setTextColor(Color.parseColor("#333333"));
        }

        // Type
        binding.tvType1.setText(t1.tripType);
        binding.tvType2.setText(t2.tripType);

        // Overall winner
        int score1 = 0, score2 = 0;
        if (t1.totalDistance > t2.totalDistance) score1++;
        else if (t2.totalDistance > t1.totalDistance) score2++;
        if (t1.notesCount > t2.notesCount) score1++;
        else if (t2.notesCount > t1.notesCount) score2++;

        if (score1 > score2) {
            binding.tvWinner.setText("🏆 " + t1.name + " wins overall!");
            binding.tvWinner.setBackgroundColor(
                    Color.parseColor("#1D9E75"));
        } else if (score2 > score1) {
            binding.tvWinner.setText("🏆 " + t2.name + " wins overall!");
            binding.tvWinner.setBackgroundColor(
                    Color.parseColor("#534AB7"));
        } else {
            binding.tvWinner.setText("🤝 It's a tie!");
            binding.tvWinner.setBackgroundColor(
                    Color.parseColor("#BA7517"));
        }
    }

    private String formatDist(float km) {
        if (km < 1) return (int)(km * 1000) + " m";
        return String.format(Locale.getDefault(), "%.1f km", km);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}