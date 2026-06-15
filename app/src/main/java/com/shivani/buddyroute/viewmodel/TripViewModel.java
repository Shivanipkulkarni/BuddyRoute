package com.shivani.buddyroute.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.shivani.buddyroute.database.TripRepository;
import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.model.Waypoint;

import java.util.List;

public class TripViewModel extends AndroidViewModel {

    private final TripRepository repository;

    public TripViewModel(@NonNull Application application) {
        super(application);
        repository = new TripRepository(application);
    }

    // ─── TRIP ACTIONS ───────────────────────────────────────

    public void insertTrip(Trip trip, TripRepository.OnTripInsertedCallback callback) {
        repository.insertTrip(trip, callback);
    }

    public void updateTrip(Trip trip) {
        repository.updateTrip(trip);
    }

    public void deleteTrip(Trip trip) {
        repository.deleteTrip(trip);
    }

    public LiveData<List<Trip>> getAllTrips() {
        return repository.getAllTrips();
    }

    public LiveData<Trip> getTripById(int tripId) {
        return repository.getTripById(tripId);
    }

    public LiveData<Trip> getActiveTrip() {
        return repository.getActiveTrip();
    }

    // ─── WAYPOINT ACTIONS ───────────────────────────────────

    public void insertWaypoint(Waypoint waypoint) {
        repository.insertWaypoint(waypoint);
    }

    public LiveData<List<Waypoint>> getWaypointsForTrip(int tripId) {
        return repository.getWaypointsForTrip(tripId);
    }

    public List<Waypoint> getWaypointsForTripSync(int tripId) {
        return repository.getWaypointsForTripSync(tripId);
    }

    // ─── NOTE ACTIONS ───────────────────────────────────────

    public void insertNote(TripNote note) {
        repository.insertNote(note);
    }

    public void deleteNote(TripNote note) {
        repository.deleteNote(note);
    }

    public LiveData<List<TripNote>> getNotesForTrip(int tripId) {
        return repository.getNotesForTrip(tripId);
    }

    public List<TripNote> getNotesForTripSync(int tripId) {
        return repository.getNotesForTripSync(tripId);
    }

    // ─── HELPER METHOD ──────────────────────────────────────

    // Call this when user taps "End Trip"
    public void endTrip(Trip trip, float totalDistance) {
        trip.endTime = System.currentTimeMillis();
        trip.totalDistance = totalDistance;
        repository.updateTrip(trip);
    }
    public int getStreak() {
        return repository.calculateStreak();
    }
}