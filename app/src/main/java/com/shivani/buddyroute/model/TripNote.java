package com.shivani.buddyroute.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trip_notes")
public class TripNote {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int tripId;          // which trip this belongs to
    public String noteText;     // what the user wrote
    public String mood;         // "happy", "tired", "amazed", "hungry", "peaceful"
    public String photoPath;    // local file path, null if no photo
    public double latitude;     // where the note was written
    public double longitude;
    public long timestamp;
    public String voiceNotePath;  // local path to .3gp audio file

    // Constructor
    public TripNote(int tripId, String noteText, String mood,
                    String photoPath, String voiceNotePath,
                    double latitude, double longitude) {
        this.tripId = tripId;
        this.noteText = noteText;
        this.mood = mood;
        this.photoPath = photoPath;
        this.voiceNotePath = voiceNotePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }
}