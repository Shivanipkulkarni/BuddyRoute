package com.shivani.buddyroute.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shivani.buddyroute.model.Trip;

import java.util.List;

@Dao
public interface TripDao {

    // Save a new trip
    @Insert
    long insertTrip(Trip trip);

    // Update an existing trip (used when trip ends)
    @Update
    void updateTrip(Trip trip);

    // Delete a trip
    @Delete
    void deleteTrip(Trip trip);

    // Get all trips, newest first — LiveData means UI auto-updates when data changes
    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    LiveData<List<Trip>> getAllTrips();

    // Get one specific trip by its ID
    @Query("SELECT * FROM trips WHERE id = :tripId")
    LiveData<Trip> getTripById(int tripId);

    // Get the currently active trip (endTime is 0 means still running)
    @Query("SELECT * FROM trips WHERE endTime = 0 LIMIT 1")
    LiveData<Trip> getActiveTrip();

    // Sync version — used internally, not for UI
    @Query("SELECT * FROM trips WHERE id = :tripId")
    Trip getTripByIdSync(int tripId);

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    List<Trip> getAllTripsSync();
}