package com.example.calorietracker;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.BadgeViewHolder> {

    private List<Badge> badges = new ArrayList<>();

    public void setBadges(List<Badge> badges) {
        this.badges = badges;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        holder.bind(badges.get(position));
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc;
        ImageView icon, lock;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.badgeTitle);
            desc = itemView.findViewById(R.id.badgeDesc);
            icon = itemView.findViewById(R.id.badgeIcon);
            lock = itemView.findViewById(R.id.lockIcon);
        }

        public void bind(Badge badge) {
            title.setText(badge.title);
            desc.setText(badge.description);

            if (badge.isUnlocked) {
                icon.setColorFilter(Color.parseColor("#FFA726"), PorterDuff.Mode.SRC_IN);
                icon.setAlpha(1.0f);
                title.setTextColor(Color.BLACK);
                lock.setVisibility(View.GONE);
            } else {
                icon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                icon.setAlpha(0.5f);
                title.setTextColor(Color.GRAY);
                lock.setVisibility(View.VISIBLE);
            }
        }
    }
}