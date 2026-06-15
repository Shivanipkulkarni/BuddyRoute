package com.shivani.buddyroute.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;
import com.shivani.buddyroute.model.Waypoint;

@Database(entities = {Trip.class, Waypoint.class, TripNote.class},
        version = 2, exportSchema = false)
public abstract class BuddyRouteDatabase extends RoomDatabase {

    public abstract TripDao tripDao();
    public abstract WaypointDao waypointDao();
    public abstract TripNoteDao tripNoteDao();

    // Migration from version 1 to 2
    // Adds voiceNotePath column to trip_notes table
    public static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL(
                            "ALTER TABLE trip_notes " +
                                    "ADD COLUMN voiceNotePath TEXT");
                }
            };

    private static volatile BuddyRouteDatabase INSTANCE;

    public static BuddyRouteDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BuddyRouteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            BuddyRouteDatabase.class,
                            "buddyroute_database"
                            ).addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}