package com.shivani.buddyroute;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.SmsManager;
import android.location.Location;

public class EmergencyHelper implements SensorEventListener {

    public interface OnShakeListener {
        void onShake();
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private OnShakeListener listener;
    private long lastShakeTime = 0;

    // Shake detection values
    private static final float SHAKE_THRESHOLD = 2.7f;
    private static final int SHAKE_WAIT_MS = 3000;

    public EmergencyHelper(Context context) {
        sensorManager = (SensorManager)
                context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calculate acceleration force
        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;
        float gForce = (float) Math.sqrt(gX*gX + gY*gY + gZ*gZ);

        if (gForce > SHAKE_THRESHOLD) {
            long now = System.currentTimeMillis();
            // Prevent multiple triggers
            if ((now - lastShakeTime) > SHAKE_WAIT_MS) {
                lastShakeTime = now;
                if (listener != null) listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Send SMS to emergency contact
    public static void sendEmergencySMS(Context context,
                                        String phoneNumber, double lat, double lng) {
        try {
            // Add +91 if not already there
            String formattedNumber = phoneNumber;
            if (!phoneNumber.startsWith("+")) {
                formattedNumber = "+91" + phoneNumber;
            }

            String locationUrl =
                    "https://maps.google.com/?q=" + lat + "," + lng;
            String message =
                    "🆘 EMERGENCY ALERT from BuddyRoute!\n" +
                            "I may need help. My last location:\n" +
                            locationUrl;

            SmsManager smsManager = SmsManager.getDefault();
            // Split into multiple parts in case message is long
            java.util.ArrayList<String> parts =
                    smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(
                    formattedNumber, null, parts, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Save emergency contact
    public static void saveEmergencyContact(Context context,
                                            String name, String phone) {
        SharedPreferences prefs = context.getSharedPreferences(
                "buddyroute_prefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("emergency_name", name)
                .putString("emergency_phone", phone)
                .apply();
    }

    // Get emergency contact
    public static String getEmergencyPhone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "buddyroute_prefs", Context.MODE_PRIVATE);
        return prefs.getString("emergency_phone", null);
    }

    public static String getEmergencyName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "buddyroute_prefs", Context.MODE_PRIVATE);
        return prefs.getString("emergency_name", "");
    }
}