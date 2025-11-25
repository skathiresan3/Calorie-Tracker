package com.example.calorietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.List;

public class MealHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MealAdapter adapter;
    private TextView emptyStateText;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_history, container, false);

        recyclerView = view.findViewById(R.id.mealsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MealAdapter();
        recyclerView.setAdapter(adapter);

        // Handle click to re-open meal details
        adapter.setOnMealClickListener(meal -> {
            Bundle bundle = new Bundle();
            // Pass data to ResultFragment manually since we aren't analyzing an image
            bundle.putString("mealName", meal.getName());
            bundle.putInt("calories", (int) meal.getCalories());
            bundle.putInt("protein", (int) meal.getProtein());
            bundle.putInt("carbs", (int) meal.getCarbs());
            bundle.putInt("fat", (int) meal.getFat());
            bundle.putBoolean("isHistoryView", true); // Flag to tell ResultFragment not to run GPT

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_MealHistoryFragment_to_ResultFragment, bundle);
        });

        loadMeals();

        return view;
    }

    private void loadMeals() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        DatabaseReference mealsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(user.getUid()).child("meals");

        mealsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Meal> mealList = new ArrayList<>();
                for (DataSnapshot shot : snapshot.getChildren()) {
                    Meal meal = shot.getValue(Meal.class);
                    if (meal != null) {
                        mealList.add(meal);
                    }
                }

                // Reverse to show newest first
                Collections.reverse(mealList);

                if (mealList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setMeals(mealList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }
}