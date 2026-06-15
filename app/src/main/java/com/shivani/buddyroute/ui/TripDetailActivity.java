package com.shivani.buddyroute.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.shivani.buddyroute.PdfExportHelper;
import com.shivani.buddyroute.databinding.ActivityTripDetailBinding;
import com.shivani.buddyroute.SafetyScoreHelper;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripDetailActivity extends AppCompatActivity {

    private ActivityTripDetailBinding binding;
    private TripViewModel viewModel;
    private NoteAdapter noteAdapter;
    private PhotoGridAdapter photoAdapter;
    private int tripId = -1;
    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tripId = getIntent().getIntExtra("TRIP_ID", -1);
        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        // Set up journal RecyclerView
        noteAdapter = new NoteAdapter();
        binding.notesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        binding.notesRecyclerView.setAdapter(noteAdapter);

        // Set up photo grid RecyclerView — 3 columns
        photoAdapter = new PhotoGridAdapter();
        binding.photosRecyclerView.setLayoutManager(
                new GridLayoutManager(this, 3));
        binding.photosRecyclerView.setAdapter(photoAdapter);

        binding.backButton.setOnClickListener(v -> finish());

        // Timeline button — we'll add this to the layout
        binding.btnTimeline.setOnClickListener(v -> {
            Intent intent = new Intent(this,
                    TimelineActivity.class);
            intent.putExtra("TRIP_ID", tripId);
            startActivity(intent);
        });

        // Tab switching
        binding.tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            // Journal tab
                            binding.notesRecyclerView.setVisibility(View.VISIBLE);
                            binding.photosRecyclerView.setVisibility(View.GONE);
                            binding.tvNoPhotos.setVisibility(View.GONE);
                        } else {
                            // Photos tab
                            binding.notesRecyclerView.setVisibility(View.GONE);
                            if (photoAdapter.getPhotoCount() == 0) {
                                binding.tvNoPhotos.setVisibility(View.VISIBLE);
                                binding.photosRecyclerView.setVisibility(View.GONE);
                            } else {
                                binding.tvNoPhotos.setVisibility(View.GONE);
                                binding.photosRecyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override public void onTabReselected(TabLayout.Tab tab) {}
                });

        // Load trip info
        viewModel.getTripById(tripId).observe(this, trip -> {
            if (trip == null) return;
            currentTrip = trip;

            binding.tvTripName.setText(trip.name);
            binding.tvDestination.setText("📍 " + trip.destination);

            if (trip.totalDistance < 1) {
                binding.tvStatDistance.setText(
                        (int)(trip.totalDistance * 1000) + " m");
            } else {
                binding.tvStatDistance.setText(String.format(
                        Locale.getDefault(), "%.1f km", trip.totalDistance));
            }

            binding.tvStatNotes.setText(String.valueOf(trip.notesCount));

            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd MMM", Locale.getDefault());
            binding.tvStatDate.setText(
                    sdf.format(new Date(trip.startTime)));

            try {
                binding.headerLayout.setBackgroundColor(
                        Color.parseColor(trip.colorTheme));
            } catch (Exception ignored) {}
            // Show safety score
            showSafetyScore(trip);
        });

        // Load notes — feeds both journal and photo adapters
        viewModel.getNotesForTrip(tripId).observe(this, notes -> {
            if (notes != null) {
                noteAdapter.setNotes(notes);
                photoAdapter.setNotes(notes);
            }
        });

        // Export button
        binding.btnExportPdf.setOnClickListener(v -> exportTripAsPdf());
    }

    private void exportTripAsPdf() {
        if (currentTrip == null) {
            Toast.makeText(this,
                    "Trip data not loaded yet, try again",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnExportPdf.setEnabled(false);
        binding.btnExportPdf.setText("Generating PDF...");

        new Thread(() -> {
            try {
                List<TripNote> notes =
                        viewModel.getNotesForTripSync(tripId);
                File pdfFile = PdfExportHelper.exportTrip(
                        this, currentTrip, notes);

                runOnUiThread(() -> {
                    binding.btnExportPdf.setEnabled(true);
                    binding.btnExportPdf.setText(
                            "📄 Export Trip Story (PDF)");

                    Uri pdfUri = androidx.core.content.FileProvider
                            .getUriForFile(
                                    this,
                                    getPackageName() + ".provider",
                                    pdfFile);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                    shareIntent.addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(
                            shareIntent, "Share your trip story"));
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    binding.btnExportPdf.setEnabled(true);
                    binding.btnExportPdf.setText(
                            "📄 Export Trip Story (PDF)");
                    Toast.makeText(this,
                            "Export failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    private void showSafetyScore(Trip trip) {
        SafetyScoreHelper.SafetyResult result =
                SafetyScoreHelper.calculate(this, trip);

        // Find or create safety card
        // We'll show it as a toast for now — full UI in next update
        // You can also add a dedicated view in the layout
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}