package com.example.calorietracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NutritionViewModel extends ViewModel {

    // --- Daily Goals (Hardcoded for simplicity) ---
    private final MutableLiveData<Integer> calorieGoal = new MutableLiveData<>(2000);
    private final MutableLiveData<Integer> proteinGoal = new MutableLiveData<>(120);
    private final MutableLiveData<Integer> carbsGoal = new MutableLiveData<>(200);
    private final MutableLiveData<Integer> fatGoal = new MutableLiveData<>(60);

    // --- Consumed Amounts ---
    private final MutableLiveData<Integer> consumedCalories = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> consumedProtein = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> consumedCarbs = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> consumedFat = new MutableLiveData<>(0);

    // --- LiveData Getters ---
    public LiveData<Integer> getCalorieGoal() { return calorieGoal; }
    public LiveData<Integer> getProteinGoal() { return proteinGoal; }
    public LiveData<Integer> getCarbsGoal() { return carbsGoal; }
    public LiveData<Integer> getFatGoal() { return fatGoal; }

    public LiveData<Integer> getConsumedCalories() { return consumedCalories; }
    public LiveData<Integer> getConsumedProtein() { return consumedProtein; }
    public LiveData<Integer> getConsumedCarbs() { return consumedCarbs; }
    public LiveData<Integer> getConsumedFat() { return consumedFat; }

    /**
     * Called from ResultFragment to add the nutrients from a meal.
     */
    public void addMeal(int calories, int protein, int carbs, int fat) {
        consumedCalories.setValue(consumedCalories.getValue() + calories);
        consumedProtein.setValue(consumedProtein.getValue() + protein);
        consumedCarbs.setValue(consumedCarbs.getValue() + carbs);
        consumedFat.setValue(consumedFat.getValue() + fat);
    }
}
