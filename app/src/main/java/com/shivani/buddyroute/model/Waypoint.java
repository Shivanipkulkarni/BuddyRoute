package com.shivani
        .buddyroute.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "waypoints")
public class Waypoint {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int tripId;       // which trip this belongs to
    public double latitude;
    public double longitude;
    public long timestamp;   // when this point was recorded
    public float speed;      // metres per second

    // Constructor
    public Waypoint(int tripId, double latitude, double longitude, float speed) {
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
        this.speed = speed;
    }
}