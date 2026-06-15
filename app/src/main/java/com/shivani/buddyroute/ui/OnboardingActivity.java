package com.shivani.buddyroute.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.shivani.buddyroute.MainActivity;
import com.shivani.buddyroute.R;
import com.shivani.buddyroute.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    private final String[] emojis = {"🗺️", "📍", "📄"};
    private final String[] titles = {
            "Track Your Journey",
            "Journal Every Moment",
            "Share Your Story"
    };
    private final String[] descs = {
            "BuddyRoute records your route on a map automatically as you travel. Works fully offline!",
            "Add notes, photos and moods at any point during your trip. Build your travel diary.",
            "Export your entire trip as a beautiful PDF story and share it with friends and family."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up ViewPager with slides
        binding.viewPager.setAdapter(new SlideAdapter());

        // Create dots
        setupDots(0);

        // Update dots when page changes
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == emojis.length - 1) {
                    binding.btnNext.setText("Get Started 🚀");
                } else {
                    binding.btnNext.setText("Next →");
                }
            }
        });

        // Next button
        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < emojis.length - 1) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                goToMain();
            }
        });

        // Skip button
        binding.tvSkip.setOnClickListener(v -> goToMain());
    }

    private void setupDots(int activeIndex) {
        binding.dotsLayout.removeAllViews();
        for (int i = 0; i < emojis.length; i++) {
            View dot = new View(this);
            int size = 10;
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dpToPx(size), dpToPx(size));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);

            // Active dot is bigger and green, others are grey
            if (i == activeIndex) {
                dot.setBackgroundColor(Color.parseColor("#1D9E75"));
                params.width = dpToPx(24);
            } else {
                dot.setBackgroundColor(Color.parseColor("#DDDDDD"));
            }
            dot.setLayoutParams(params);

            // Make dots rounded
            dot.post(() -> {
                dot.setPivotX(dot.getWidth() / 2f);
                dot.setPivotY(dot.getHeight() / 2f);
            });

            binding.dotsLayout.addView(dot);
        }
    }

    private void goToMain() {
        // Mark onboarding as done — never show again
        SharedPreferences prefs = getSharedPreferences("buddyroute_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_done", true).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Adapter for the 3 slides
    class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {

        @NonNull
        @Override
        public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_slide, parent, false);
            return new SlideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
            holder.tvEmoji.setText(emojis[position]);
            holder.tvTitle.setText(titles[position]);
            holder.tvDesc.setText(descs[position]);
        }

        @Override
        public int getItemCount() { return emojis.length; }

        class SlideViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvDesc;
            SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvSlideEmoji);
                tvTitle = itemView.findViewById(R.id.tvSlideTitle);
                tvDesc = itemView.findViewById(R.id.tvSlideDesc);
            }
        }
    }
}