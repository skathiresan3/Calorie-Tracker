//package com.example.calorietracker;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;
//
//import com.example.calorietracker.databinding.HomeFragmentBinding;
//
//public class HomeFragment extends Fragment {
//
//    private HomeFragmentBinding binding;
//
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState
//    ) {
//
//        binding = HomeFragmentBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//
//    }
//
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        binding.buttonFirst.setOnClickListener(v ->
//                NavHostFragment.findNavController(HomeFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_PhotoSelectFragment)
//        );
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//
//}








package com.example.calorietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private NutritionViewModel nutritionViewModel;
    private ProgressBar progressBar;
    private TextView caloriesRemainingValue, proteinValue, carbsValue, fatValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the shared ViewModel
        nutritionViewModel = new ViewModelProvider(requireActivity()).get(NutritionViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // Initialize UI components
        progressBar = view.findViewById(R.id.progressBar);
        caloriesRemainingValue = view.findViewById(R.id.caloriesRemainingValue);
        proteinValue = view.findViewById(R.id.proteinValue);
        carbsValue = view.findViewById(R.id.carbsValue);
        fatValue = view.findViewById(R.id.fatValue);

        MaterialButton analyzeMealButton = view.findViewById(R.id.button_first);
        analyzeMealButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_HomeFragment_to_photoSelectFragment);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe the ViewModel LiveData for changes
        nutritionViewModel.getCalorieGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedCalories().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getCalorieGoal().getValue()));

        nutritionViewModel.getProteinGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedProtein().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getProteinGoal().getValue()));

        nutritionViewModel.getCarbsGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedCarbs().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getCarbsGoal().getValue()));

        nutritionViewModel.getFatGoal().observe(getViewLifecycleOwner(), this::updateUI);
        nutritionViewModel.getConsumedFat().observe(getViewLifecycleOwner(), consumed -> updateUI(nutritionViewModel.getFatGoal().getValue()));

        // Initial UI update
        updateUI(0); // Pass a dummy value to trigger the first update
    }

    private void updateUI(Integer goal) {
        // Ensure LiveData values are not null before using them
        Integer calorieGoal = nutritionViewModel.getCalorieGoal().getValue();
        Integer consumedCalories = nutritionViewModel.getConsumedCalories().getValue();
        Integer proteinGoal = nutritionViewModel.getProteinGoal().getValue();
        Integer consumedProtein = nutritionViewModel.getConsumedProtein().getValue();
        Integer carbsGoal = nutritionViewModel.getCarbsGoal().getValue();
        Integer consumedCarbs = nutritionViewModel.getConsumedCarbs().getValue();
        Integer fatGoal = nutritionViewModel.getFatGoal().getValue();
        Integer consumedFat = nutritionViewModel.getConsumedFat().getValue();

        if (calorieGoal == null || consumedCalories == null || proteinGoal == null || consumedProtein == null ||
                carbsGoal == null || consumedCarbs == null || fatGoal == null || consumedFat == null) {
            return; // Exit if data is not ready
        }

        // --- Calculate remaining values ---
        int remainingCalories = calorieGoal - consumedCalories;
        int remainingProtein = proteinGoal - consumedProtein;
        int remainingCarbs = carbsGoal - consumedCarbs;
        int remainingFat = fatGoal - consumedFat;

        // --- Update Progress Bar ---
        progressBar.setMax(calorieGoal);
        progressBar.setProgress(remainingCalories);

        // --- Update TextViews ---
        caloriesRemainingValue.setText(String.format(Locale.getDefault(), "%,d", remainingCalories));
        proteinValue.setText(String.format(Locale.getDefault(), "%dg", remainingProtein));
        carbsValue.setText(String.format(Locale.getDefault(), "%dg", remainingCarbs));
        fatValue.setText(String.format(Locale.getDefault(), "%dg", remainingFat));
    }
}
