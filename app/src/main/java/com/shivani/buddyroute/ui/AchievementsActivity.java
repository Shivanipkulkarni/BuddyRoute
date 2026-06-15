package com.shivani.buddyroute.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shivani.buddyroute.AchievementHelper;
import com.shivani.buddyroute.R;
import com.shivani.buddyroute.model.Achievement;
import com.shivani.buddyroute.viewmodel.TripViewModel;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build layout programmatically
        androidx.constraintlayout.widget.ConstraintLayout
                root = new androidx.constraintlayout.widget
                .ConstraintLayout(this);
        root.setBackgroundColor(
                Color.parseColor("#F0F4F8"));
        setContentView(root);

        // Header
        TextView header = new TextView(this);
        header.setId(View.generateViewId());
        header.setText("🏆 My Achievements");
        header.setTextSize(22);
        header.setTextColor(Color.WHITE);
        header.setTypeface(null,
                android.graphics.Typeface.BOLD);
        header.setPadding(40, 60, 40, 20);
        header.setBackgroundColor(
                Color.parseColor("#1D9E75"));

        androidx.constraintlayout.widget.ConstraintLayout
                .LayoutParams headerParams =
                new androidx.constraintlayout.widget
                        .ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        root.addView(header, headerParams);

        // RecyclerView
        RecyclerView rv = new RecyclerView(this);
        rv.setId(View.generateViewId());
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setPadding(16, 16, 16, 16);

        androidx.constraintlayout.widget.ConstraintLayout
                .LayoutParams rvParams =
                new androidx.constraintlayout.widget
                        .ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0);
        rvParams.topToBottom = header.getId();
        rvParams.bottomToBottom =
                androidx.constraintlayout.widget
                        .ConstraintLayout.LayoutParams.PARENT_ID;
        root.addView(rv, rvParams);

        // Load achievements
        TripViewModel viewModel =
                new ViewModelProvider(this)
                        .get(TripViewModel.class);

        viewModel.getAllTrips().observe(this, trips -> {
            List<Achievement> achievements =
                    AchievementHelper.getWithStatus(this);
            rv.setAdapter(
                    new AchievementAdapter(achievements));
        });

        // Back on header tap
        header.setOnClickListener(v -> finish());
    }

    // Inner adapter
    static class AchievementAdapter extends
            RecyclerView.Adapter<AchievementAdapter.VH> {

        private final List<Achievement> items;

        AchievementAdapter(List<Achievement> items) {
            this.items = items;
        }

        @Override
        public VH onCreateViewHolder(
                ViewGroup parent, int viewType) {
            // Card view
            com.google.android.material.card.MaterialCardView
                    card = new com.google.android.material
                    .card.MaterialCardView(parent.getContext());

            ViewGroup.MarginLayoutParams lp =
                    new ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8, 8, 8, 8);
            card.setLayoutParams(lp);
            card.setRadius(16);
            card.setCardElevation(4);

            android.widget.LinearLayout inner =
                    new android.widget.LinearLayout(
                            parent.getContext());
            inner.setOrientation(
                    android.widget.LinearLayout.VERTICAL);
            inner.setGravity(android.view.Gravity.CENTER);
            inner.setPadding(16, 24, 16, 24);
            card.addView(inner);

            TextView tvEmoji = new TextView(
                    parent.getContext());
            tvEmoji.setTextSize(36);
            tvEmoji.setGravity(android.view.Gravity.CENTER);
            tvEmoji.setTag("emoji");
            inner.addView(tvEmoji);

            TextView tvTitle = new TextView(
                    parent.getContext());
            tvTitle.setTextSize(13);
            tvTitle.setGravity(android.view.Gravity.CENTER);
            tvTitle.setTypeface(null,
                    android.graphics.Typeface.BOLD);
            tvTitle.setTag("title");
            tvTitle.setPadding(0, 8, 0, 4);
            inner.addView(tvTitle);

            TextView tvDesc = new TextView(
                    parent.getContext());
            tvDesc.setTextSize(11);
            tvDesc.setGravity(android.view.Gravity.CENTER);
            tvDesc.setTag("desc");
            inner.addView(tvDesc);

            return new VH(card);
        }

        @Override
        public void onBindViewHolder(VH holder, int pos) {
            Achievement a = items.get(pos);
            com.google.android.material.card.MaterialCardView
                    card = (com.google.android.material.card
                    .MaterialCardView) holder.itemView;

            android.widget.LinearLayout inner =
                    (android.widget.LinearLayout)
                            card.getChildAt(0);

            TextView tvEmoji =
                    (TextView) inner.findViewWithTag("emoji");
            TextView tvTitle =
                    (TextView) inner.findViewWithTag("title");
            TextView tvDesc =
                    (TextView) inner.findViewWithTag("desc");

            tvEmoji.setText(a.emoji);
            tvTitle.setText(a.title);
            tvDesc.setText(a.description);

            if (a.unlocked) {
                card.setCardBackgroundColor(
                        Color.parseColor("#E1F5EE"));
                tvTitle.setTextColor(
                        Color.parseColor("#0F6E56"));
                tvDesc.setTextColor(
                        Color.parseColor("#1D9E75"));
                tvEmoji.setAlpha(1f);
            } else {
                card.setCardBackgroundColor(
                        Color.parseColor("#F5F5F5"));
                tvTitle.setTextColor(
                        Color.parseColor("#AAAAAA"));
                tvDesc.setTextColor(
                        Color.parseColor("#CCCCCC"));
                tvEmoji.setAlpha(0.3f);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            VH(View v) { super(v); }
        }
    }
}