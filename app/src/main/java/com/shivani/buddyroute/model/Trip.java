package com.shivani.buddyroute.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trips")
public class Trip {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;          // "Goa Trip 2024"
    public String destination;   // "Goa"
    public String tripType;      // "Beach", "Trek", "Road", "City"
    public String colorTheme;    // hex color like "#1D9E75"
    public long startTime;       // timestamp in milliseconds
    public long endTime;         // 0 if trip is still active
    public float totalDistance;  // in kilometres
    public int notesCount;
    public String coverPhotoPath; // local file path to cover photo

    // Constructor
    public Trip(String name, String destination, String tripType, String colorTheme) {
        this.name = name;
        this.destination = destination;
        this.tripType = tripType;
        this.colorTheme = colorTheme;
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
        this.totalDistance = 0;
        this.notesCount = 0;
        this.coverPhotoPath = null;
    }
}