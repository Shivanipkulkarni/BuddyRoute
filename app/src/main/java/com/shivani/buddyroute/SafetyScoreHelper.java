package com.shivani.buddyroute;

import android.content.Context;
import com.shivani.buddyroute.model.Trip;

public class SafetyScoreHelper {

    public static class SafetyResult {
        public int score;
        public String[] factors;
        public String[] values;
        public String level; // Good, Fair, Needs Attention
        public String levelColor;
    }

    public static SafetyResult calculate(
            Context context, Trip trip) {
        SafetyResult result = new SafetyResult();
        int score = 0;

        String[] factors = new String[5];
        String[] values = new String[5];

        // Factor 1 — Day travel (6am-8pm)
        java.util.Calendar cal =
                java.util.Calendar.getInstance();
        cal.setTimeInMillis(trip.startTime);
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        boolean isDayTravel = hour >= 6 && hour <= 20;
        factors[0] = "🌞 Travel Time";
        if (isDayTravel) {
            score += 25;
            values[0] = "Day travel ✅";
        } else {
            values[0] = "Night travel ⚠️";
        }

        // Factor 2 — Emergency contact set
        String emergencyPhone =
                EmergencyHelper.getEmergencyPhone(context);
        boolean hasContact = emergencyPhone != null
                && !emergencyPhone.isEmpty();
        factors[1] = "🆘 Emergency Contact";
        if (hasContact) {
            score += 25;
            values[1] = "Contact set ✅";
        } else {
            values[1] = "Not set ❌";
        }

        // Factor 3 — Has notes (engaged travel)
        boolean hasNotes = trip.notesCount > 0;
        factors[2] = "📝 Active Journaling";
        if (hasNotes) {
            score += 20;
            values[2] = trip.notesCount + " notes ✅";
        } else {
            values[2] = "No notes yet";
        }

        // Factor 4 — Trip has ended properly
        boolean tripEnded = trip.endTime > 0;
        factors[3] = "⏱️ Trip Completed";
        if (tripEnded) {
            score += 15;
            values[3] = "Ended properly ✅";
        } else {
            values[3] = "Still active";
        }

        // Factor 5 — Named destination
        boolean hasDestination = trip.destination != null
                && !trip.destination.isEmpty();
        factors[4] = "📍 Destination Set";
        if (hasDestination) {
            score += 15;
            values[4] = trip.destination + " ✅";
        } else {
            values[4] = "Not specified";
        }

        result.score = score;
        result.factors = factors;
        result.values = values;

        if (score >= 80) {
            result.level = "Excellent";
            result.levelColor = "#1D9E75";
        } else if (score >= 60) {
            result.level = "Good";
            result.levelColor = "#4CAF50";
        } else if (score >= 40) {
            result.level = "Fair";
            result.levelColor = "#FF9800";
        } else {
            result.level = "Needs Attention";
            result.levelColor = "#E53935";
        }

        return result;
    }
}