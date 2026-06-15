package com.shivani.buddyroute.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import android.Manifest;

import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.shivani.buddyroute.databinding.ActivityAddNoteBinding;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.viewmodel.TripViewModel;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private TripViewModel viewModel;

    private int tripId = -1;
    private double latitude = 0;
    private double longitude = 0;
    private String selectedMood = "happy";
    private String selectedPhotoPath = null;
    private String selectedVoicePath = null;

    // Photo picker launcher
    private final ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(), uri -> {
                        if (uri != null) {
                            try {
                                // Copy photo to app's private storage permanently
                                String permanentPath = copyPhotoToStorage(uri);
                                if (permanentPath != null) {
                                    selectedPhotoPath = permanentPath;
                                    com.bumptech.glide.Glide.with(this)
                                            .load(new java.io.File(permanentPath))
                                            .centerCrop()
                                            .into(binding.ivPhotoPreview);
                                    binding.ivPhotoPreview.setVisibility(
                                            android.view.View.VISIBLE);
                                    binding.btnPickPhoto.setText("Change Photo ✅");
                                }
                            } catch (Exception e) {
                                Toast.makeText(this,
                                        "Could not load photo: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private String copyPhotoToStorage(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream =
                    getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create permanent file in app storage
            java.io.File photosDir = new java.io.File(
                    getFilesDir(), "trip_photos");
            if (!photosDir.exists()) photosDir.mkdirs();

            String fileName = "photo_" +
                    System.currentTimeMillis() + ".jpg";
            java.io.File outputFile =
                    new java.io.File(photosDir, fileName);

            java.io.FileOutputStream outputStream =
                    new java.io.FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    101);
        }

        // Get data passed from TrackingActivity
        tripId = getIntent().getIntExtra("TRIP_ID", -1);
        latitude = getIntent().getDoubleExtra("LATITUDE", 0);
        longitude = getIntent().getDoubleExtra("LONGITUDE", 0);

        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setupMoodButtons();

        binding.backButton.setOnClickListener(v -> finish());

        binding.btnPickPhoto.setOnClickListener(v ->
                photoPickerLauncher.launch("image/*"));
        binding.btnSaveNote.setOnClickListener(v -> saveNote());
        binding.btnAddVoice.setOnClickListener(v -> VoiceNoteDialog.show(
                this,
                latitude,
                longitude,
                filePath -> {
                    selectedVoicePath = filePath;
                    binding.btnAddVoice.setText("🎤 Voice Note Added ✅");
                    binding.btnAddVoice.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#1D9E75")));
                }
        ));
    }

    private void setupMoodButtons() {
        // Set happy as default
        binding.moodHappy.setBackgroundResource(
                com.shivani.buddyroute.R.drawable.mood_bg_selected);

        TextView[] moodButtons = {
                binding.moodHappy,
                binding.moodAmazed,
                binding.moodTired,
                binding.moodHungry,
                binding.moodPeaceful
        };

        String[] moods = {"happy", "amazed", "tired", "hungry", "peaceful"};

        for (int i = 0; i < moodButtons.length; i++) {
            final String mood = moods[i];
            moodButtons[i].setOnClickListener(v -> {
                // Reset all to unselected
                for (TextView btn : moodButtons) {
                    btn.setBackgroundResource(
                            com.shivani.buddyroute.R.drawable.mood_bg_unselected);
                }
                // Highlight selected
                ((TextView) v).setBackgroundResource(
                        com.shivani.buddyroute.R.drawable.mood_bg_selected);
                selectedMood = mood;
            });
        }
    }

    private void saveNote() {
        String noteText = binding.etNoteText.getText().toString().trim();

        if (noteText.isEmpty() && selectedVoicePath == null) {
            binding.etNoteText.setError("Write something or add a voice note!");
            return;
        }

        // Use placeholder text if only voice note
        String text = noteText.isEmpty() ? "🎤 Voice note" : noteText;

        TripNote note = new TripNote(
                tripId,
                text,
                selectedMood,
                selectedPhotoPath,
                selectedVoicePath,   // ← new field
                latitude,
                longitude
        );

        viewModel.insertNote(note);
        Toast.makeText(this, "Note saved! 📝", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}