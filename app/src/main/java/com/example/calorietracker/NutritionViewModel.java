package com.example.calorietracker;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NutritionViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> calorieGoal = new MutableLiveData<>(2000);
    private final MutableLiveData<Integer> proteinGoal = new MutableLiveData<>(120);
    private final MutableLiveData<Integer> carbsGoal = new MutableLiveData<>(200);
    private final MutableLiveData<Integer> fatGoal = new MutableLiveData<>(60);

    private final MutableLiveData<Integer> consumedCalories = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> consumedProtein = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> consumedCarbs = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> consumedFat = new MutableLiveData<>(0);

    private ValueEventListener mealListener;
    private DatabaseReference userMealsRef;

    public LiveData<Integer> getCalorieGoal() { return calorieGoal; }
    public LiveData<Integer> getProteinGoal() { return proteinGoal; }
    public LiveData<Integer> getCarbsGoal() { return carbsGoal; }
    public LiveData<Integer> getFatGoal() { return fatGoal; }

    public LiveData<Integer> getConsumedCalories() { return consumedCalories; }
    public LiveData<Integer> getConsumedProtein() { return consumedProtein; }
    public LiveData<Integer> getConsumedCarbs() { return consumedCarbs; }
    public LiveData<Integer> getConsumedFat() { return consumedFat; }

    public NutritionViewModel(@NonNull Application application) {
        super(application);
        listenToUserMeals();
    }

    public void listenToUserMeals() {
        if (userMealsRef != null && mealListener != null) {
            userMealsRef.removeEventListener(mealListener);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            resetDailyValues();
            return;
        }

        String userId = user.getUid();
        userMealsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("meals");

        mealListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalCals = 0;
                int totalPro = 0;
                int totalCarb = 0;
                int totalFat = 0;


                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    Meal meal = mealSnapshot.getValue(Meal.class);
                    if (meal != null) {

                        String mealDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(meal.getTimestamp()));

                        if (todayStr.equals(mealDateStr)) {
                            totalCals += (int) meal.getCalories();
                            totalPro += (int) meal.getProtein();
                            totalCarb += (int) meal.getCarbs();
                            totalFat += (int) meal.getFat();
                        }
                    }
                }

                consumedCalories.setValue(totalCals);
                consumedProtein.setValue(totalPro);
                consumedCarbs.setValue(totalCarb);
                consumedFat.setValue(totalFat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        userMealsRef.addValueEventListener(mealListener);
    }

    public void resetDailyValues() {
        consumedCalories.setValue(0);
        consumedProtein.setValue(0);
        consumedCarbs.setValue(0);
        consumedFat.setValue(0);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (userMealsRef != null && mealListener != null) {
            userMealsRef.removeEventListener(mealListener);
        }
    }
}