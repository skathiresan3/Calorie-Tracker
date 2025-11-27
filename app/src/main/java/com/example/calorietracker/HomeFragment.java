package com.example.calorietracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private NutritionViewModel nutritionViewModel;
    private ProgressBar progressBar;
    private TextView caloriesRemainingValue, proteinValue, carbsValue, fatValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nutritionViewModel = new ViewModelProvider(requireActivity()).get(NutritionViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        caloriesRemainingValue = view.findViewById(R.id.caloriesRemainingValue);
        proteinValue = view.findViewById(R.id.proteinValue);
        carbsValue = view.findViewById(R.id.carbsValue);
        fatValue = view.findViewById(R.id.fatValue);

        // Analyze Meal Button
        MaterialButton analyzeMealButton = view.findViewById(R.id.button_first);
        analyzeMealButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_photoSelectFragment);
        });

        Button historyButton = view.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
             NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_MealHistoryFragment);
        });


        // Logout Button
        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            nutritionViewModel.resetDailyValues();
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_WelcomeFragment);
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nutritionViewModel.getCalorieGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedCalories().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getCalorieGoal().getValue()));
        nutritionViewModel.getProteinGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedProtein().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getProteinGoal().getValue()));
        nutritionViewModel.getCarbsGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedCarbs().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getCarbsGoal().getValue()));
        nutritionViewModel.getFatGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedFat().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getFatGoal().getValue()));

        updateUI(0);

        View historyButton = view.findViewById(R.id.historyButton);
        if (historyButton != null) {
            historyButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_HomeFragment_to_MealHistoryFragment)
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nutritionViewModel.listenToUserMeals();
        checkDateAndReset();
    }

    private void checkDateAndReset() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("DailyTrackerPrefs", Context.MODE_PRIVATE);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastSavedDate = prefs.getString("last_login_date", "");

        if (!currentDate.equals(lastSavedDate)) {
            nutritionViewModel.resetDailyValues();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_login_date", currentDate);
            editor.apply();
        }
    }

    private void updateUI(Integer goal) {
        Integer calorieGoal = nutritionViewModel.getCalorieGoal().getValue();
        Integer consumedCalories = nutritionViewModel.getConsumedCalories().getValue();
        Integer proteinGoal = nutritionViewModel.getProteinGoal().getValue();
        Integer consumedProtein = nutritionViewModel.getConsumedProtein().getValue();
        Integer carbsGoal = nutritionViewModel.getCarbsGoal().getValue();
        Integer consumedCarbs = nutritionViewModel.getConsumedCarbs().getValue();
        Integer fatGoal = nutritionViewModel.getFatGoal().getValue();
        Integer consumedFat = nutritionViewModel.getConsumedFat().getValue();

        if (calorieGoal == null || consumedCalories == null) return;

        int remainingCalories = calorieGoal - consumedCalories;
        int remainingProtein = proteinGoal - consumedProtein;
        int remainingCarbs = carbsGoal - consumedCarbs;
        int remainingFat = fatGoal - consumedFat;

        if (remainingCalories < 0) remainingCalories = 0;
        if (remainingProtein < 0) remainingProtein = 0;
        if (remainingCarbs < 0) remainingCarbs = 0;
        if (remainingFat < 0) remainingFat = 0;

        progressBar.setMax(calorieGoal);
        progressBar.setProgress(remainingCalories);

        caloriesRemainingValue.setText(String.format(Locale.getDefault(), "%,d", remainingCalories));
        proteinValue.setText(String.format(Locale.getDefault(), "%dg", remainingProtein));
        carbsValue.setText(String.format(Locale.getDefault(), "%dg", remainingCarbs));
        fatValue.setText(String.format(Locale.getDefault(), "%dg", remainingFat));
    }
}