package com.shivani.buddyroute.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shivani.buddyroute.R;
import com.shivani.buddyroute.databinding.ActivityHelpBinding;

public class HelpActivity extends AppCompatActivity {

    private ActivityHelpBinding binding;

    // Step content
    private final String[] stepNumbers = {"1", "2", "3", "4", "5"};
    private final String[] stepEmojis  = {"➕", "🚀", "📝", "⏹️", "📄"};
    private final String[] stepTitles  = {
            "Create a new trip",
            "Start recording",
            "Add moments",
            "End your trip",
            "Export your story"
    };
    private final String[] stepDescs = {
            "Tap the green ➕ button on the home screen. Give your trip a name, destination, type and color theme.",
            "Tap 'Start Recording Trip'. Grant location permission once. Your route starts drawing on the map automatically.",
            "Tap '📝 Add Note' anytime during the trip. Write what you're feeling, pick a mood emoji, and attach a photo or voice note.",
            "When you're done, tap '⏹ End Trip'. Your highlights will be shown automatically — your best moments of the trip!",
            "Open the trip from home screen. Tap '📄 Export Trip Story' to generate a beautiful PDF. Share it on WhatsApp or Gmail!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> finish());

        // Set up each step
        int[] stepIds = {
                R.id.step1, R.id.step2,
                R.id.step3, R.id.step4, R.id.step5
        };

        for (int i = 0; i < stepIds.length; i++) {
            android.view.View stepView = binding.getRoot()
                    .findViewById(stepIds[i]);
            if (stepView == null) continue;

            TextView tvNum =
                    stepView.findViewById(R.id.tvStepNumber);
            TextView tvEmoji =
                    stepView.findViewById(R.id.tvStepEmoji);
            TextView tvTitle =
                    stepView.findViewById(R.id.tvStepTitle);
            TextView tvDesc =
                    stepView.findViewById(R.id.tvStepDesc);

            tvNum.setText(stepNumbers[i]);
            tvEmoji.setText(stepEmojis[i]);
            tvTitle.setText(stepTitles[i]);
            tvDesc.setText(stepDescs[i]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}