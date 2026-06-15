package com.shivani.buddyroute;

import android.content.Context;
import android.content.SharedPreferences;

import com.shivani.buddyroute.model.Achievement;
import com.shivani.buddyroute.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class AchievementHelper {

    private static final String PREFS =
            "buddyroute_achievements";

    public static List<Achievement> getAllAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("first_trip", "🏆",
                "First Adventure",
                "Complete your very first trip"));
        list.add(new Achievement("note_taker", "📝",
                "Note Taker",
                "Write 10 journal notes"));
        list.add(new Achievement("photographer", "📷",
                "Trip Photographer",
                "Attach 5 photos to notes"));
        list.add(new Achievement("km_10", "🚶",
                "10 km Explorer",
                "Travel a total of 10 km"));
        list.add(new Achievement("km_50", "🛣️",
                "50 km Traveller",
                "Travel a total of 50 km"));
        list.add(new Achievement("km_100", "🚗",
                "100 km Adventurer",
                "Travel a total of 100 km"));
        list.add(new Achievement("beach_trip", "🏖️",
                "Beach Bum",
                "Complete a Beach type trip"));
        list.add(new Achievement("trek_trip", "🥾",
                "Mountain Climber",
                "Complete a Trek type trip"));
        list.add(new Achievement("city_trip", "🏙️",
                "City Slicker",
                "Complete a City type trip"));
        list.add(new Achievement("streak_3", "🔥",
                "On Fire",
                "Maintain a 3 week travel streak"));
        list.add(new Achievement("five_trips", "🌍",
                "Globetrotter",
                "Complete 5 trips"));
        list.add(new Achievement("night_owl", "🌙",
                "Night Owl",
                "Start a trip after 8 PM"));
        return list;
    }

    public static List<Achievement> checkAndUnlock(
            Context context, List<Trip> trips,
            int totalNotes, int totalPhotos,
            int streak) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<Achievement> newlyUnlocked = new ArrayList<>();
        List<Achievement> all = getAllAchievements();

        float totalKm = 0;
        boolean hasBeach = false, hasTrek = false,
                hasCity = false, hasNightTrip = false;

        if (trips != null) {
            for (Trip t : trips) {
                totalKm += t.totalDistance;
                if ("Beach".equals(t.tripType))
                    hasBeach = true;
                if ("Trek".equals(t.tripType))
                    hasTrek = true;
                if ("City".equals(t.tripType))
                    hasCity = true;
                java.util.Calendar cal =
                        java.util.Calendar.getInstance();
                cal.setTimeInMillis(t.startTime);
                if (cal.get(java.util.Calendar.HOUR_OF_DAY)
                        >= 20)
                    hasNightTrip = true;
            }
        }

        int tripCount = trips != null ? trips.size() : 0;

        for (Achievement a : all) {
            boolean alreadyUnlocked =
                    prefs.getBoolean(a.id, false);
            if (alreadyUnlocked) {
                a.unlocked = true;
                continue;
            }

            boolean shouldUnlock = false;

            switch (a.id) {
                case "first_trip":
                    shouldUnlock = tripCount >= 1; break;
                case "note_taker":
                    shouldUnlock = totalNotes >= 10; break;
                case "photographer":
                    shouldUnlock = totalPhotos >= 5; break;
                case "km_10":
                    shouldUnlock = totalKm >= 10; break;
                case "km_50":
                    shouldUnlock = totalKm >= 50; break;
                case "km_100":
                    shouldUnlock = totalKm >= 100; break;
                case "beach_trip":
                    shouldUnlock = hasBeach; break;
                case "trek_trip":
                    shouldUnlock = hasTrek; break;
                case "city_trip":
                    shouldUnlock = hasCity; break;
                case "streak_3":
                    shouldUnlock = streak >= 3; break;
                case "five_trips":
                    shouldUnlock = tripCount >= 5; break;
                case "night_owl":
                    shouldUnlock = hasNightTrip; break;
            }

            if (shouldUnlock) {
                a.unlocked = true;
                a.unlockedAt = System.currentTimeMillis();
                editor.putBoolean(a.id, true);
                newlyUnlocked.add(a);
            }
        }

        editor.apply();
        return newlyUnlocked;
    }

    public static List<Achievement> getWithStatus(
            Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS,
                        Context.MODE_PRIVATE);
        List<Achievement> all = getAllAchievements();
        for (Achievement a : all) {
            a.unlocked = prefs.getBoolean(a.id, false);
        }
        return all;
    }
}