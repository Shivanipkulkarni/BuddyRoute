package com.shivani.buddyroute.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.model.Waypoint;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripRepository {

    private final TripDao tripDao;
    private final WaypointDao waypointDao;
    private final TripNoteDao tripNoteDao;

    // This runs database work in background — never on main thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TripRepository(Application application) {
        BuddyRouteDatabase db = BuddyRouteDatabase.getInstance(application);
        tripDao = db.tripDao();
        waypointDao = db.waypointDao();
        tripNoteDao = db.tripNoteDao();
    }

    // ─── TRIP METHODS ───────────────────────────────────────

    public void insertTrip(Trip trip, OnTripInsertedCallback callback) {
        executor.execute(() -> {
            long newId = tripDao.insertTrip(trip);
            if (callback != null) callback.onInserted((int) newId);
        });
    }

    public void updateTrip(Trip trip) {
        executor.execute(() -> tripDao.updateTrip(trip));
    }

    public void deleteTrip(Trip trip) {
        executor.execute(() -> {
            tripDao.deleteTrip(trip);
            waypointDao.deleteWaypointsForTrip(trip.id);
            tripNoteDao.deleteNotesForTrip(trip.id);
        });
    }

    public LiveData<List<Trip>> getAllTrips() {
        return tripDao.getAllTrips();
    }

    public LiveData<Trip> getTripById(int tripId) {
        return tripDao.getTripById(tripId);
    }

    public LiveData<Trip> getActiveTrip() {
        return tripDao.getActiveTrip();
    }

    // ─── WAYPOINT METHODS ───────────────────────────────────

    public void insertWaypoint(Waypoint waypoint) {
        executor.execute(() -> waypointDao.insertWaypoint(waypoint));
    }

    public LiveData<List<Waypoint>> getWaypointsForTrip(int tripId) {
        return waypointDao.getWaypointsForTrip(tripId);
    }

    public List<Waypoint> getWaypointsForTripSync(int tripId) {
        return waypointDao.getWaypointsForTripSync(tripId);
    }

    public void deleteWaypointsForTrip(int tripId) {
        executor.execute(() -> waypointDao.deleteWaypointsForTrip(tripId));
    }

    // ─── NOTE METHODS ───────────────────────────────────────

    public void insertNote(TripNote note) {
        executor.execute(() -> {
            // Save the note
            tripNoteDao.insertNote(note);

            // Get the trip and increment note count
            Trip trip = tripDao.getTripByIdSync(note.tripId);
            if (trip != null) {
                trip.notesCount = trip.notesCount + 1;
                tripDao.updateTrip(trip);
            }
        });
    }

    public void deleteNote(TripNote note) {
        executor.execute(() -> tripNoteDao.deleteNote(note));
    }

    public LiveData<List<TripNote>> getNotesForTrip(int tripId) {
        return tripNoteDao.getNotesForTrip(tripId);
    }

    public List<TripNote> getNotesForTripSync(int tripId) {
        return tripNoteDao.getNotesForTripSync(tripId);
    }

    // ─── CALLBACK INTERFACE ─────────────────────────────────

    // This is used to get the new trip's ID back after inserting
    // We need the ID immediately so we can start tracking that trip
    public interface OnTripInsertedCallback {
        void onInserted(int tripId);
    }
    // Returns how many consecutive weeks the user has taken a trip
    public int calculateStreak() {
        // Get all trips synchronously sorted by date
        List<Trip> trips = tripDao.getAllTripsSync();
        if (trips == null || trips.isEmpty()) return 0;

        int streak = 0;
        long oneWeekMs = 7 * 24 * 60 * 60 * 1000L;
        long now = System.currentTimeMillis();

        // Check each week going backwards from today
        long weekStart = now - oneWeekMs;

        for (int i = 0; i < 52; i++) { // check up to 52 weeks back
            long weekEnd = weekStart + oneWeekMs;
            boolean hadTripThisWeek = false;

            for (Trip trip : trips) {
                if (trip.startTime >= weekStart && trip.startTime < weekEnd) {
                    hadTripThisWeek = true;
                    break;
                }
            }

            if (hadTripThisWeek) {
                streak++;
                weekStart -= oneWeekMs;
            } else {
                break; // streak broken
            }
        }
        return streak;
    }
}