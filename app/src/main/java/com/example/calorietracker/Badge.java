package com.example.calorietracker;

public class Badge {
    public String title;
    public String description;
    public boolean isUnlocked;

    public Badge(String title, String description, boolean isUnlocked) {
        this.title = title;
        this.description = description;
        this.isUnlocked = isUnlocked;
    }
}