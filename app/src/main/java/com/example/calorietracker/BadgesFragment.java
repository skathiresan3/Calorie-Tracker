package com.example.calorietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BadgesFragment extends Fragment {

    private RecyclerView recyclerView;
    private BadgesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_badges, container, false);

        recyclerView = view.findViewById(R.id.badgesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BadgesAdapter();
        recyclerView.setAdapter(adapter);


        loadBadges();

        return view;
    }

    private void loadBadges() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference statsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(user.getUid()).child("stats");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HomeFragment.UserStats stats = snapshot.getValue(HomeFragment.UserStats.class);
                if (stats == null) stats = new HomeFragment.UserStats();

                List<Badge> badgeList = new ArrayList<>();

                badgeList.add(new Badge("First Step", "Log your very first meal", stats.totalDaysLogged >= 1));
                badgeList.add(new Badge("Getting Serious", "Log meals for 3 total days", stats.totalDaysLogged >= 3));
                badgeList.add(new Badge("Week Warrior", "Log meals for 7 total days", stats.totalDaysLogged >= 7));
                badgeList.add(new Badge("Streak Starter", "Reach a 3-day streak", stats.longestStreak >= 3));
                badgeList.add(new Badge("On Fire!", "Reach a 7-day streak", stats.longestStreak >= 7));
                badgeList.add(new Badge("Unstoppable", "Reach a 30-day streak", stats.longestStreak >= 30));

                adapter.setBadges(badgeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load badges", Toast.LENGTH_SHORT).show();
            }
        });
    }
}