package com.shivani.buddyroute;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.shivani.buddyroute.database.TripRepository;
import com.shivani.buddyroute.model.Waypoint;

public class TrackingService extends Service {

    public static final String CHANNEL_ID = "tracking_channel";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String EXTRA_TRIP_ID = "TRIP_ID";

    // This broadcasts location updates to the TrackingActivity
    public static final String ACTION_LOCATION_UPDATE = "com.shivani.buddyroute.LOCATION_UPDATE";
    public static final String EXTRA_LATITUDE = "LATITUDE";
    public static final String EXTRA_LONGITUDE = "LONGITUDE";
    public static final String EXTRA_DISTANCE = "DISTANCE";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TripRepository repository;

    private int currentTripId = -1;
    private Location lastLocation = null;
    private float totalDistance = 0f;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        repository = new TripRepository(getApplication());
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();

        if (ACTION_START.equals(action)) {
            currentTripId = intent.getIntExtra(EXTRA_TRIP_ID, -1);
            totalDistance = 0f;
            lastLocation = null;
            startForeground(1, buildNotification());
            startLocationUpdates();
        } else if (ACTION_STOP.equals(action)) {
            stopLocationUpdates();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 45000) // every 45 seconds
                .setMinUpdateIntervalMillis(15000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null || currentTripId == -1) return;

                Location location = result.getLastLocation();
                if (location == null) return;

                // Calculate distance from last point
                if (lastLocation != null) {
                    float dist = lastLocation.distanceTo(location);
                    totalDistance += dist / 1000f; // convert to km
                }
                lastLocation = location;

                // Save waypoint to database
                Waypoint waypoint = new Waypoint(
                        currentTripId,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getSpeed()
                );
                repository.insertWaypoint(waypoint);

                // Broadcast to TrackingActivity so it can update the map
                Intent broadcast = new Intent(ACTION_LOCATION_UPDATE);
                broadcast.putExtra(EXTRA_LATITUDE, location.getLatitude());
                broadcast.putExtra(EXTRA_LONGITUDE, location.getLongitude());
                broadcast.putExtra(EXTRA_DISTANCE, totalDistance);
                sendBroadcast(broadcast);
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("BuddyRoute is recording your trip");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BuddyRoute Recording")
                .setContentText("Your trip is being recorded")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}