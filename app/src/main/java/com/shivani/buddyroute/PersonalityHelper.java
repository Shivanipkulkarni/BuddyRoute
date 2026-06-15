package com.shivani.buddyroute;

import com.shivani.buddyroute.model.Trip;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PersonalityHelper {

    public static class PersonalityResult {
        public String emoji;
        public String title;
        public String description;
        public String color;
        public int percentage;
    }

    public static PersonalityResult calculate(
            List<Trip> trips) {
        if (trips == null || trips.isEmpty()) {
            PersonalityResult r = new PersonalityResult();
            r.emoji = "🌱";
            r.title = "New Explorer";
            r.description = "Your adventure has just begun! Complete more trips to discover your travel personality.";
            r.color = "#888888";
            r.percentage = 0;
            return r;
        }

        // Count trip types
        Map<String, Integer> typeCount = new HashMap<>();
        float totalKm = 0;
        int totalNotes = 0;
        boolean hasNightTrip = false;

        for (Trip t : trips) {
            String type = t.tripType != null ?
                    t.tripType : "Road";
            typeCount.put(type,
                    typeCount.getOrDefault(type, 0) + 1);
            totalKm += t.totalDistance;
            totalNotes += t.notesCount;

            java.util.Calendar cal =
                    java.util.Calendar.getInstance();
            cal.setTimeInMillis(t.startTime);
            if (cal.get(java.util.Calendar.HOUR_OF_DAY)
                    >= 20)
                hasNightTrip = true;
        }

        // Find dominant type
        String dominant = "Road";
        int maxCount = 0;
        for (Map.Entry<String, Integer> e :
                typeCount.entrySet()) {
            if (e.getValue() > maxCount) {
                maxCount = e.getValue();
                dominant = e.getKey();
            }
        }

        int percentage = (maxCount * 100) / trips.size();

        PersonalityResult r = new PersonalityResult();
        r.percentage = percentage;

        // Special cases first
        if (totalNotes > trips.size() * 3) {
            r.emoji = "✍️";
            r.title = "The Storyteller";
            r.description = "You document everything! With " + totalNotes + " notes across " + trips.size() + " trips, you're a true travel journalist.";
            r.color = "#7209B7";
        } else if (totalKm > 500) {
            r.emoji = "🚀";
            r.title = "The Distance Chaser";
            r.description = "You've covered " + (int)totalKm + " km! Distance doesn't scare you — the farther the better.";
            r.color = "#3A0CA3";
        } else if (hasNightTrip && trips.size() >= 2) {
            r.emoji = "🌙";
            r.title = "The Night Wanderer";
            r.description = "You love the magic of night travel. When others sleep, you explore.";
            r.color = "#1A1A2E";
        } else {
            switch (dominant) {
                case "Beach":
                    r.emoji = "🌊";
                    r.title = "Beach Explorer";
                    r.description = "Sand, waves and sunsets — you're most alive near the ocean. " + percentage + "% of your trips are beach adventures!";
                    r.color = "#0077B6";
                    break;
                case "Trek":
                    r.emoji = "⛰️";
                    r.title = "Mountain Hunter";
                    r.description = "Heights don't scare you — they thrill you! " + percentage + "% of your trips involve trekking and nature.";
                    r.color = "#2D6A4F";
                    break;
                case "City":
                    r.emoji = "🏛️";
                    r.title = "Urban Explorer";
                    r.description = "Streets, cafes and hidden alleys — you find magic in cities. " + percentage + "% of your trips are city adventures!";
                    r.color = "#3A0CA3";
                    break;
                default:
                    r.emoji = "🛣️";
                    r.title = "Road Warrior";
                    r.description = "The open road calls to you! " + percentage + "% of your trips are road adventures. You live for the journey.";
                    r.color = "#E85D04";
                    break;
            }
        }

        return r;
    }
}