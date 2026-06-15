package com.shivani.buddyroute.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.shivani.buddyroute.model.TripNote;

import java.util.List;

@Dao
public interface TripNoteDao {

    // Save a new note
    @Insert
    void insertNote(TripNote note);

    // Delete a note
    @Delete
    void deleteNote(TripNote note);

    // Get all notes for one trip — shown in the timeline
    @Query("SELECT * FROM trip_notes WHERE tripId = :tripId ORDER BY timestamp ASC")
    LiveData<List<TripNote>> getNotesForTrip(int tripId);

    // Plain list version — used for PDF export
    @Query("SELECT * FROM trip_notes WHERE tripId = :tripId ORDER BY timestamp ASC")
    List<TripNote> getNotesForTripSync(int tripId);

    // Delete all notes when a trip is deleted
    @Query("DELETE FROM trip_notes WHERE tripId = :tripId")
    void deleteNotesForTrip(int tripId);
}