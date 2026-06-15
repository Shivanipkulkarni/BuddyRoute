package com.shivani.buddyroute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shivani.buddyroute.databinding.ActivityMainBinding;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.PersonalityHelper;
import com.shivani.buddyroute.ui.CompareActivity;
import com.shivani.buddyroute.ui.HelpActivity;
import com.shivani.buddyroute.ui.NewTripActivity;
import com.shivani.buddyroute.ui.TripAdapter;
import com.shivani.buddyroute.ui.TripDetailActivity;
import com.shivani.buddyroute.ui.AchievementsActivity;
import com.shivani.buddyroute.ui.WhereWasIActivity;
import com.shivani.buddyroute.ui.FriendshipActivity;
import com.shivani.buddyroute.ui.ScratchMapActivity;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TripViewModel viewModel;
    private TripAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupViewModel();
        setupButtons();
        setGreeting();
    }

    // ── SETUP ──────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new TripAdapter(this::onTripClicked);
        binding.tripsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        binding.tripsRecyclerView.setAdapter(adapter);
        binding.tripsRecyclerView.setNestedScrollingEnabled(false);
        setupSwipeToDelete();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this)
                .get(TripViewModel.class);

        viewModel.getAllTrips().observe(this, trips -> {
            if (trips == null || trips.isEmpty()) {
                showEmptyState();
            } else {
                showTripsList(trips);
            }

            // Streak — background thread
            new Thread(() -> {
                int streak = viewModel.getStreak();
                runOnUiThread(() -> updateStreak(streak));
            }).start();
        });
    }

    private void setupButtons() {
        // New trip FAB
        binding.fabNewTrip.setOnClickListener(v ->
                startActivity(new Intent(this,
                        NewTripActivity.class)));

        // Help
        binding.btnHelp.setOnClickListener(v ->
                startActivity(new Intent(this,
                        HelpActivity.class)));

        // SOS
        binding.btnSOS.setOnClickListener(v ->
                showEmergencyContactSetup());

        // Compare
        binding.btnCompareText.setOnClickListener(v ->
                startActivity(new Intent(this,
                        CompareActivity.class)));
        // Long press the trips recycler area to open scratch map
        binding.tripsRecyclerView.setOnLongClickListener(v -> {
            startActivity(new Intent(this,
                    ScratchMapActivity.class));
            return true;
        });
        binding.btnScratchMap.setOnClickListener(v ->
                startActivity(new Intent(this,
                        ScratchMapActivity.class)));
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already home — scroll to top
                return true;
            } else if (id == R.id.nav_trips) {
                // Scroll to trips section
                binding.tripsRecyclerView.smoothScrollToPosition(0);
                return true;
            } else if (id == R.id.nav_achievements) {
                new androidx.appcompat.app.AlertDialog
                        .Builder(this)
                        .setTitle("Choose")
                        .setItems(new String[]{
                                "🏆 My Achievements",
                                "👫 Friendship Score"
                        }, (d, which) -> {
                            if (which == 0) {
                                startActivity(new Intent(this,
                                        AchievementsActivity.class));
                            } else {
                                startActivity(new Intent(this,
                                        FriendshipActivity.class));
                            }
                        })
                        .show();
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this,
                        WhereWasIActivity.class));
                return true;
            }
            return false;
        });

    }

    private void showStatsDialog() {
        viewModel.getAllTrips().observe(this, trips -> {
            PersonalityHelper.PersonalityResult personality =
                    PersonalityHelper.calculate(trips);

            float totalKm = 0;
            int totalNotes = 0;
            if (trips != null) {
                for (Trip t : trips) {
                    totalKm += t.totalDistance;
                    totalNotes += t.notesCount;
                }
            }

            int tripCount = trips != null ?
                    trips.size() : 0;

            String msg =
                    personality.emoji + "  " +
                            personality.title + "\n\n" +
                            personality.description + "\n\n" +
                            "────────────────\n" +
                            "🗺️  Total Trips: " + tripCount + "\n" +
                            "📏  Total Distance: " +
                            String.format(
                                    java.util.Locale.getDefault(),
                                    "%.1f km", totalKm) + "\n" +
                            "📝  Total Notes: " + totalNotes;

            new AlertDialog.Builder(this)
                    .setTitle("Your Travel Personality")
                    .setMessage(msg)
                    .setPositiveButton("📅 Where Was I?",
                            (d, w) -> startActivity(
                                    new Intent(this,
                                            WhereWasIActivity.class)))
                    .setNegativeButton("Close", null)
                    .show();
        });
    }

    // ── GREETING ───────────────────────────────────────────

    private void setGreeting() {
        int hour = Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = "Good morning! ☀️";
        } else if (hour < 17) {
            greeting = "Good afternoon! 🌤️";
        } else if (hour < 21) {
            greeting = "Good evening! 🌆";
        } else {
            greeting = "Good night! 🌙";
        }
        binding.tvGreeting.setText(greeting);
    }

    // ── STATE DISPLAY ──────────────────────────────────────

    private void showEmptyState() {
        binding.emptyStateLayout.setVisibility(View.VISIBLE);
        binding.tripsRecyclerView.setVisibility(View.GONE);
        binding.statsStrip.setVisibility(View.GONE);
        binding.emptyStatsLayout.setVisibility(View.VISIBLE);
        binding.streakBadge.setVisibility(View.GONE);
    }

    private void showTripsList(List<Trip> trips) {
        binding.emptyStateLayout.setVisibility(View.GONE);
        binding.tripsRecyclerView.setVisibility(View.VISIBLE);
        binding.statsStrip.setVisibility(View.VISIBLE);
        binding.emptyStatsLayout.setVisibility(View.GONE);
        adapter.setTrips(trips);
        updateStats(trips);
    }

    private void updateStats(List<Trip> trips) {
        binding.tvTotalTrips.setText(
                String.valueOf(trips.size()));

        float totalKm = 0;
        int totalNotes = 0;
        for (Trip t : trips) {
            totalKm += t.totalDistance;
            totalNotes += t.notesCount;
        }

        if (totalKm < 1) {
            binding.tvTotalDistance.setText(
                    (int)(totalKm * 1000) + " m");
        } else {
            binding.tvTotalDistance.setText(
                    String.format(Locale.getDefault(),
                            "%.1f", totalKm));
        }

        binding.tvTotalNotes.setText(
                String.valueOf(totalNotes));
    }

    private void updateStreak(int streak) {
        if (streak > 0) {
            binding.streakBadge.setVisibility(View.VISIBLE);
            binding.tvStreakCount.setText(
                    "🔥 " + streak + " week" +
                            (streak > 1 ? "s" : "") + " travel streak!");

            // Progress toward next milestone
            int nextMilestone;
            if (streak < 4) nextMilestone = 4;
            else if (streak < 8) nextMilestone = 8;
            else if (streak < 12) nextMilestone = 12;
            else nextMilestone = streak + 4;

            int progress = (streak * 100) / nextMilestone;
            binding.streakProgress.setProgress(progress);
            binding.tvStreakNext.setText(
                    nextMilestone - streak + " more to next milestone");
        } else {
            binding.streakBadge.setVisibility(View.GONE);
        }
    }

    // ── TRIP CLICK ─────────────────────────────────────────

    private void onTripClicked(Trip trip) {
        Intent intent = new Intent(this,
                TripDetailActivity.class);
        intent.putExtra("TRIP_ID", trip.id);
        startActivity(intent);
    }

    // ── SWIPE TO DELETE ────────────────────────────────────

    private void setupSwipeToDelete() {
        new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        0, ItemTouchHelper.LEFT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                          @NonNull RecyclerView.ViewHolder vh,
                                          @NonNull RecyclerView.ViewHolder t) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            @NonNull RecyclerView.ViewHolder vh,
                            int direction) {
                        int pos = vh.getAdapterPosition();
                        Trip trip = adapter.getTripAt(pos);

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Delete Trip?")
                                .setMessage("\"" + trip.name +
                                        "\" will be permanently deleted.")
                                .setPositiveButton("Delete",
                                        (d, w) -> {
                                            viewModel.deleteTrip(trip);
                                            Toast.makeText(MainActivity.this,
                                                    "Trip deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        })
                                .setNegativeButton("Cancel",
                                        (d, w) ->
                                                adapter.notifyItemChanged(pos))
                                .show();
                    }
                }).attachToRecyclerView(binding.tripsRecyclerView);
    }

    // ── EMERGENCY SOS ──────────────────────────────────────

    private void showEmergencyContactSetup() {
        if (androidx.core.app.ActivityCompat
                .checkSelfPermission(this,
                        android.Manifest.permission.SEND_SMS)
                != android.content.pm.PackageManager
                .PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat
                    .requestPermissions(this,
                            new String[]{
                                    android.Manifest.permission.SEND_SMS},
                            200);
            return;
        }

        android.widget.EditText etName =
                new android.widget.EditText(this);
        etName.setHint("Contact name e.g. Mom");
        etName.setInputType(android.text.InputType
                .TYPE_CLASS_TEXT);

        android.widget.EditText etPhone =
                new android.widget.EditText(this);
        etPhone.setHint("10-digit number e.g. 9876543210");
        etPhone.setInputType(android.text.InputType
                .TYPE_CLASS_PHONE);

        String existingName =
                EmergencyHelper.getEmergencyName(this);
        String existingPhone =
                EmergencyHelper.getEmergencyPhone(this);
        if (existingName != null && !existingName.isEmpty())
            etName.setText(existingName);
        if (existingPhone != null && !existingPhone.isEmpty())
            etPhone.setText(existingPhone);

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);
        layout.setOrientation(
                android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(etName);
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("🆘 Emergency Contact")
                .setMessage("Shake your phone hard during a trip to send your GPS location to this contact instantly.")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText()
                            .toString().trim();
                    String phone = etPhone.getText()
                            .toString().trim();
                    if (!phone.isEmpty()) {
                        EmergencyHelper.saveEmergencyContact(
                                this, name, phone);
                        Toast.makeText(this,
                                "✅ Emergency contact saved! Shake phone during trip to activate SOS.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Please enter a phone number",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
        if (requestCode == 200 &&
                grantResults.length > 0 &&
                grantResults[0] ==
                        android.content.pm.PackageManager
                                .PERMISSION_GRANTED) {
            showEmergencyContactSetup();
        } else if (requestCode == 200) {
            Toast.makeText(this,
                    "SMS permission needed for SOS",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}