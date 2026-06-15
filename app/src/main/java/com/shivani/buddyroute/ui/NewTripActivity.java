package com.shivani.buddyroute.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shivani.buddyroute.databinding.ActivityNewTripBinding;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.viewmodel.TripViewModel;

public class NewTripActivity extends AppCompatActivity {

    private ActivityNewTripBinding binding;
    private TripViewModel viewModel;

    // Selected values
    private String selectedType = "Road";
    private String selectedColor = "#1D9E75";

    // Available colors — one per theme button
    private final String[] colors = {
            "#1D9E75", // green
            "#378ADD", // blue
            "#D4537E", // pink
            "#BA7517", // amber
            "#534AB7", // purple
            "#993C1D"  // coral
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setupTripTypeButtons();
        setupColorButtons();
        setupStartButton();

        // Back arrow
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupTripTypeButtons() {
        // Set Road as default selected
        binding.btnTypeRoad.setSelected(true);
        updateTypeButtonStyles();

        binding.btnTypeRoad.setOnClickListener(v -> {
            selectedType = "Road";
            updateTypeButtonStyles();
        });
        binding.btnTypeTrek.setOnClickListener(v -> {
            selectedType = "Trek";
            updateTypeButtonStyles();
        });
        binding.btnTypeCity.setOnClickListener(v -> {
            selectedType = "City";
            updateTypeButtonStyles();
        });
        binding.btnTypeBeach.setOnClickListener(v -> {
            selectedType = "Beach";
            updateTypeButtonStyles();
        });
    }

    private void updateTypeButtonStyles() {
        int activeColor = android.graphics.Color.parseColor(selectedColor);
        int inactiveColor = android.graphics.Color.parseColor("#EEEEEE");
        int activeText = android.graphics.Color.WHITE;
        int inactiveText = android.graphics.Color.parseColor("#555555");

        binding.btnTypeRoad.setBackgroundColor(selectedType.equals("Road") ? activeColor : inactiveColor);
        binding.btnTypeRoad.setTextColor(selectedType.equals("Road") ? activeText : inactiveText);
        binding.btnTypeTrek.setBackgroundColor(selectedType.equals("Trek") ? activeColor : inactiveColor);
        binding.btnTypeTrek.setTextColor(selectedType.equals("Trek") ? activeText : inactiveText);
        binding.btnTypeCity.setBackgroundColor(selectedType.equals("City") ? activeColor : inactiveColor);
        binding.btnTypeCity.setTextColor(selectedType.equals("City") ? activeText : inactiveText);
        binding.btnTypeBeach.setBackgroundColor(selectedType.equals("Beach") ? activeColor : inactiveColor);
        binding.btnTypeBeach.setTextColor(selectedType.equals("Beach") ? activeText : inactiveText);
    }

    private void setupColorButtons() {
        // Array of color buttons in the same order as colors[]
        android.widget.ImageButton[] colorButtons = {
                binding.color1, binding.color2, binding.color3,
                binding.color4, binding.color5, binding.color6
        };

        // Set background colors
        for (int i = 0; i < colorButtons.length; i++) {
            android.graphics.drawable.GradientDrawable circle =
                    new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circle.setColor(android.graphics.Color.parseColor(colors[i]));
            circle.setSize(80, 80);
            colorButtons[i].setBackground(circle);

            final int index = i;
            colorButtons[i].setOnClickListener(v -> {
                selectedColor = colors[index];
                updateTypeButtonStyles(); // refresh type buttons with new color
            });
        }
    }

    private void setupStartButton() {
        binding.btnStartTrip.setOnClickListener(v -> {

            String name = binding.etTripName.getText().toString().trim();
            String destination = binding.etDestination.getText().toString().trim();

            // Validation
            if (name.isEmpty()) {
                binding.etTripName.setError("Please enter a trip name");
                return;
            }
            if (destination.isEmpty()) {
                binding.etDestination.setError("Please enter a destination");
                return;
            }

            // Create and save the trip
            Trip newTrip = new Trip(name, destination, selectedType, selectedColor);

            viewModel.insertTrip(newTrip, tripId -> {
                // This runs after trip is saved — open tracking screen
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, TrackingActivity.class);
                    intent.putExtra("TRIP_ID", tripId);
                    intent.putExtra("TRIP_NAME", name);
                    startActivity(intent);
                    finish(); // close this screen
                });
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}