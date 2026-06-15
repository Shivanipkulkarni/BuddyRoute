package com.shivani.buddyroute.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.shivani.buddyroute.R;

import java.io.File;
import java.io.IOException;

public class VoiceNoteDialog {

    public interface OnVoiceSaved {
        void onSaved(String filePath);
    }

    public static void show(Context context,
                            double latitude, double longitude,
                            OnVoiceSaved callback) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_voice_note, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int)(context.getResources()
                            .getDisplayMetrics().widthPixels * 0.88f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvStatus = view.findViewById(R.id.tvVoiceStatus);
        TextView tvTimer = view.findViewById(R.id.tvRecordTimer);
        TextView btnMic = view.findViewById(R.id.btnMic);
        View pulseRing = view.findViewById(R.id.pulseRing);
        android.widget.Button btnSave =
                view.findViewById(R.id.btnSaveVoice);
        android.widget.Button btnCancel =
                view.findViewById(R.id.btnCancelVoice);

        // State
        final boolean[] isRecording = {false};
        final boolean[] hasSavedRecording = {false};
        final MediaRecorder[] recorder = {null};
        final String[] outputPath = {null};
        final Handler timerHandler = new Handler(Looper.getMainLooper());
        final long[] startTime = {0};

        // Timer runnable
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed =
                        System.currentTimeMillis() - startTime[0];
                long seconds = elapsed / 1000;
                tvTimer.setText(String.format(
                        java.util.Locale.getDefault(),
                        "%02d:%02d", seconds / 60, seconds % 60));

                // Auto stop at 60 seconds
                if (seconds >= 60) {
                    stopRecording(recorder, isRecording,
                            tvStatus, tvTimer, btnMic,
                            pulseRing, btnSave, timerHandler, this);
                    hasSavedRecording[0] = true;
                    return;
                }
                timerHandler.postDelayed(this, 1000);
            }
        };

        // Pulse animation for recording indicator
        AlphaAnimation pulse = new AlphaAnimation(0.3f, 0.7f);
        pulse.setDuration(600);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);

        // Mic tap — start/stop recording
        btnMic.setOnClickListener(v -> {
            if (!isRecording[0]) {
                // Check permission
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context,
                            "Microphone permission needed",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create output file
                File audioDir = context.getFilesDir();
                outputPath[0] = audioDir.getAbsolutePath()
                        + "/voice_" + System.currentTimeMillis() + ".3gp";

                // Start recording
                try {
                    recorder[0] = new MediaRecorder();
                    recorder[0].setAudioSource(
                            MediaRecorder.AudioSource.MIC);
                    recorder[0].setOutputFormat(
                            MediaRecorder.OutputFormat.THREE_GPP);
                    recorder[0].setAudioEncoder(
                            MediaRecorder.AudioEncoder.AMR_NB);
                    recorder[0].setOutputFile(outputPath[0]);
                    recorder[0].setMaxDuration(60000);
                    recorder[0].prepare();
                    recorder[0].start();

                    isRecording[0] = true;
                    startTime[0] = System.currentTimeMillis();

                    // Update UI
                    tvStatus.setText("Recording... tap mic to stop");
                    tvTimer.setVisibility(View.VISIBLE);
                    btnMic.setText("⏹");
                    pulseRing.startAnimation(pulse);
                    timerHandler.post(timerRunnable);

                } catch (IOException e) {
                    Toast.makeText(context,
                            "Could not start recording: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                // Stop recording
                stopRecording(recorder, isRecording,
                        tvStatus, tvTimer, btnMic,
                        pulseRing, btnSave, timerHandler, timerRunnable);
                hasSavedRecording[0] = true;
            }
        });

        btnSave.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null && outputPath[0] != null) {
                callback.onSaved(outputPath[0]);
            }
        });

        btnCancel.setOnClickListener(v -> {
            // Clean up recording if exists
            if (isRecording[0] && recorder[0] != null) {
                try {
                    recorder[0].stop();
                    recorder[0].release();
                } catch (Exception ignored) {}
            }
            timerHandler.removeCallbacks(timerRunnable);
            // Delete file
            if (outputPath[0] != null) {
                new File(outputPath[0]).delete();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void stopRecording(
            MediaRecorder[] recorder,
            boolean[] isRecording,
            TextView tvStatus,
            TextView tvTimer,
            TextView btnMic,
            View pulseRing,
            android.widget.Button btnSave,
            Handler handler,
            Runnable timerRunnable) {
        try {
            recorder[0].stop();
            recorder[0].release();
            recorder[0] = null;
        } catch (Exception ignored) {}

        isRecording[0] = false;
        handler.removeCallbacks(timerRunnable);
        pulseRing.clearAnimation();
        tvStatus.setText("Recording saved! Tap Save to attach.");
        btnMic.setText("✅");
        btnSave.setEnabled(true);
    }
}