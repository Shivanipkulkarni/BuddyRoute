package com.shivani.buddyroute.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.shivani.buddyroute.model.Waypoint;

import java.util.List;

@Dao
public interface WaypointDao {

    // Save one GPS point
    @Insert
    void insertWaypoint(Waypoint waypoint);

    // Get all waypoints for one trip — used to draw the route line on map
    @Query("SELECT * FROM waypoints WHERE tripId = :tripId ORDER BY timestamp ASC")
    LiveData<List<Waypoint>> getWaypointsForTrip(int tripId);

    // Same as above but returns plain List (not LiveData) — used for PDF export
    @Query("SELECT * FROM waypoints WHERE tripId = :tripId ORDER BY timestamp ASC")
    List<Waypoint> getWaypointsForTripSync(int tripId);

    // Delete all waypoints when a trip is deleted
    @Query("DELETE FROM waypoints WHERE tripId = :tripId")
    void deleteWaypointsForTrip(int tripId);
}