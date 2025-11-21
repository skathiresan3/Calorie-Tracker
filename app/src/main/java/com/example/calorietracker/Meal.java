package com.example.calorietracker;

public class Meal {
    private String name;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private long timestamp;

    public Meal() {} // required empty constructor for Firestore

    public Meal(String name, double calories, double protein, double carbs, double fat, long timestamp) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.timestamp = timestamp;
    }

    public String getName() { return name; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFat() { return fat; }
    public long getTimestamp() { return timestamp; }
}
