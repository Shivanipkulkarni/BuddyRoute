package com.shivani.buddyroute;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shivani.buddyroute.ui.OnboardingActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvAppName = findViewById(R.id.tvSplashName);
        TextView tvTagline = findViewById(R.id.tvSplashTagline);

        // Animate app name
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        AlphaAnimation fadeAnim = new AlphaAnimation(0f, 1f);

        AnimationSet nameAnim = new AnimationSet(true);
        nameAnim.addAnimation(scaleAnim);
        nameAnim.addAnimation(fadeAnim);
        nameAnim.setDuration(700);
        nameAnim.setFillAfter(true);

        if (tvAppName != null) tvAppName.startAnimation(nameAnim);

        AlphaAnimation taglineAnim = new AlphaAnimation(0f, 1f);
        taglineAnim.setDuration(600);
        taglineAnim.setStartOffset(500);
        taglineAnim.setFillAfter(true);

        if (tvTagline != null) tvTagline.startAnimation(taglineAnim);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(
                    "buddyroute_prefs", MODE_PRIVATE);
            boolean onboardingDone = prefs.getBoolean(
                    "onboarding_done", false);

            Intent intent;
            if (onboardingDone) {
                intent = new Intent(
                        SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(
                        SplashActivity.this, OnboardingActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}