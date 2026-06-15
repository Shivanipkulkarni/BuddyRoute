package com.shivani.buddyroute.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shivani.buddyroute.EmergencyHelper;
import com.shivani.buddyroute.OfflineMapHelper;
import com.shivani.buddyroute.R;
import com.shivani.buddyroute.TrackingService;
import com.shivani.buddyroute.databinding.ActivityTrackingBinding;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.model.Waypoint;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackingActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private ActivityTrackingBinding binding;
    private TripViewModel viewModel;
    private GoogleMap googleMap;

    private int tripId = -1;
    private String tripName = "";
    private Trip currentTrip;
    private float totalDistance = 0f;
    private double lastLat = 0, lastLng = 0;

    private final List<LatLng> routePoints = new ArrayList<>();

    private final Handler timerHandler =
            new Handler(Looper.getMainLooper());
    private long startTimeMillis;

    private EmergencyHelper emergencyHelper;

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    // ── BROADCAST RECEIVER ─────────────────────────────────

    private final BroadcastReceiver locationReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double lat = intent.getDoubleExtra(
                            TrackingService.EXTRA_LATITUDE, 0);
                    double lng = intent.getDoubleExtra(
                            TrackingService.EXTRA_LONGITUDE, 0);
                    totalDistance = intent.getFloatExtra(
                            TrackingService.EXTRA_DISTANCE, 0);

                    lastLat = lat;
                    lastLng = lng;

                    routePoints.add(new LatLng(lat, lng));
                    updateMapRoute();
                    updateDistanceUI();
                }
            };

    // ── TIMER ──────────────────────────────────────────────

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed =
                    System.currentTimeMillis() - startTimeMillis;
            long minutes = (elapsed / 1000) / 60;
            long seconds = (elapsed / 1000) % 60;
            binding.tvDuration.setText(
                    String.format(Locale.getDefault(),
                            "%02d:%02d", minutes, seconds));
            binding.tvPoints.setText(
                    String.valueOf(routePoints.size()));
            timerHandler.postDelayed(this, 1000);
        }
    };

    // ── LIFECYCLE ──────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackingBinding.inflate(
                getLayoutInflater());
        setContentView(binding.getRoot());

        tripId = getIntent().getIntExtra("TRIP_ID", -1);
        tripName = getIntent().getStringExtra("TRIP_NAME");
        binding.tvTripName.setText(
                tripName != null ? tripName : "Recording...");

        viewModel = new ViewModelProvider(this)
                .get(TripViewModel.class);

        // Load trip for color theme
        viewModel.getTripById(tripId).observe(this, trip -> {
            if (trip != null) currentTrip = trip;
        });

        // Load existing waypoints
        viewModel.getWaypointsForTrip(tripId)
                .observe(this, waypoints -> {
                    if (waypoints != null && !waypoints.isEmpty()) {
                        routePoints.clear();
                        for (Waypoint wp : waypoints) {
                            routePoints.add(
                                    new LatLng(wp.latitude, wp.longitude));
                        }
                        updateMapRoute();
                    }
                });

        // Map setup
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Buttons
        binding.backButton.setOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        binding.btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(this,
                    AddNoteActivity.class);
            intent.putExtra("TRIP_ID", tripId);
            if (!routePoints.isEmpty()) {
                LatLng last =
                        routePoints.get(routePoints.size() - 1);
                intent.putExtra("LATITUDE", last.latitude);
                intent.putExtra("LONGITUDE", last.longitude);
            }
            startActivity(intent);
        });

        binding.btnEndTrip.setOnClickListener(v ->
                showEndTripDialog());

        // Emergency shake setup
        setupEmergency();

        // Check permissions and start tracking
        checkPermissionAndStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (emergencyHelper != null) emergencyHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (emergencyHelper != null) emergencyHelper.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        try {
            unregisterReceiver(locationReceiver);
        } catch (Exception ignored) {}
        binding = null;
    }

    // ── PERMISSIONS ────────────────────────────────────────

    private void checkPermissionAndStart() {
        List<String> needed = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
            needed.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO);
        }

        if (needed.isEmpty()) {
            startTracking();
        } else {
            ActivityCompat.requestPermissions(this,
                    needed.toArray(new String[0]),
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode != LOCATION_PERMISSION_REQUEST) return;

        boolean locationGranted = false;
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[i]
                    == PackageManager.PERMISSION_GRANTED) {
                locationGranted = true;
            }
        }

        if (locationGranted) {
            startTracking();
            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("Location permission is needed to track your trip. Please enable it in App Settings.")
                    .setPositiveButton("Open Settings", (d, w) -> {
                        Intent i = new Intent(
                                android.provider.Settings
                                        .ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.setData(android.net.Uri.fromParts(
                                "package", getPackageName(), null));
                        startActivity(i);
                        finish();
                    })
                    .setNegativeButton("Cancel", (d, w) -> finish())
                    .show();
        }
    }

    // ── TRACKING ───────────────────────────────────────────

    private void startTracking() {
        Intent serviceIntent =
                new Intent(this, TrackingService.class);
        serviceIntent.setAction(TrackingService.ACTION_START);
        serviceIntent.putExtra(
                TrackingService.EXTRA_TRIP_ID, tripId);
        startForegroundService(serviceIntent);

        startTimeMillis = System.currentTimeMillis();
        timerHandler.post(timerRunnable);

        IntentFilter filter = new IntentFilter(
                TrackingService.ACTION_LOCATION_UPDATE);
        ContextCompat.registerReceiver(this,
                locationReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    // ── MAP ────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings()
                .setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        if (!OfflineMapHelper.isOnline(this)) {
            binding.tvRecording.setText("● OFFLINE");
            binding.tvRecording.setTextColor(
                    Color.parseColor("#FF9800"));
        }

        // Move to last known location
        com.google.android.gms.location
                .FusedLocationProviderClient client =
                com.google.android.gms.location.LocationServices
                        .getFusedLocationProviderClient(this);
        try {
            client.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null && googleMap != null) {
                            googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(
                                                    location.getLatitude(),
                                                    location.getLongitude()),
                                            15f));
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateMapRoute() {
        if (googleMap == null || routePoints.isEmpty()) return;
        googleMap.clear();

        String color = currentTrip != null ?
                currentTrip.colorTheme : "#1D9E75";

        googleMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(8f)
                .color(Color.parseColor(color))
                .geodesic(true));

        googleMap.addMarker(new MarkerOptions()
                .position(routePoints.get(0))
                .title("Start"));

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        routePoints.get(routePoints.size() - 1), 16f));
    }

    private void updateDistanceUI() {
        if (totalDistance < 1) {
            binding.tvDistance.setText(
                    (int)(totalDistance * 1000) + " m");
        } else {
            binding.tvDistance.setText(
                    String.format(Locale.getDefault(),
                            "%.1f km", totalDistance));
        }
    }

    // ── END TRIP ───────────────────────────────────────────

    private void showEndTripDialog() {
        new AlertDialog.Builder(this)
                .setTitle("End Trip?")
                .setMessage("Are you sure you want to end this trip?")
                .setPositiveButton("Yes, End Trip",
                        (dialog, which) -> endTrip())
                .setNegativeButton("Keep Going", null)
                .show();
    }

    private void endTrip() {
        Intent serviceIntent =
                new Intent(this, TrackingService.class);
        serviceIntent.setAction(TrackingService.ACTION_STOP);
        startService(serviceIntent);

        if (currentTrip != null) {
            viewModel.endTrip(currentTrip, totalDistance);
        }

        new Thread(() -> {
            List<TripNote> notes =
                    viewModel.getNotesForTripSync(tripId);
            runOnUiThread(() -> {
                if (notes != null && !notes.isEmpty()) {
                    HighlightsDialog.show(
                            TrackingActivity.this,
                            currentTrip,
                            notes,
                            () -> {
                                Intent intent = new Intent(
                                        TrackingActivity.this,
                                        TripDetailActivity.class);
                                intent.putExtra("TRIP_ID", tripId);
                                startActivity(intent);
                                finish();
                            }
                    );
                } else {
                    Toast.makeText(this,
                            "Trip saved! 🎉",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    // ── EMERGENCY SOS ──────────────────────────────────────

    private void setupEmergency() {
        emergencyHelper = new EmergencyHelper(this);
        emergencyHelper.setOnShakeListener(() -> {
            String phone =
                    EmergencyHelper.getEmergencyPhone(this);
            if (phone == null) {
                runOnUiThread(() ->
                        new AlertDialog.Builder(this)
                                .setTitle("No Emergency Contact!")
                                .setMessage("Set an emergency contact from the home screen SOS button first.")
                                .setPositiveButton("OK", null)
                                .show());
                return;
            }
            runOnUiThread(() -> showSOSCountdown(phone));
        });
    }

    private void showSOSCountdown(String phone) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("🆘 Sending SOS in 5 seconds...");
        builder.setMessage("Shake detected! Sending location to emergency contact.\n\nTap CANCEL if this was a mistake.");
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        new android.os.CountDownTimer(5000, 1000) {
            public void onTick(long millisLeft) {
                runOnUiThread(() ->
                        dialog.setTitle("🆘 Sending SOS in " +
                                (millisLeft / 1000) + " seconds..."));
            }
            public void onFinish() {
                dialog.dismiss();
                EmergencyHelper.sendEmergencySMS(
                        TrackingActivity.this,
                        phone, lastLat, lastLng);
                runOnUiThread(() ->
                        Toast.makeText(TrackingActivity.this,
                                        "🆘 SOS sent!", Toast.LENGTH_LONG)
                                .show());
            }
        }.start();

        dialog.setButton(
                AlertDialog.BUTTON_NEGATIVE, "CANCEL ✋",
                (d, w) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "SOS cancelled",
                            Toast.LENGTH_SHORT).show();
                });
    }
}