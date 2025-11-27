package com.example.calorietracker;

public class UserStats {
    public int currentStreak;
    public int longestStreak;
    public int totalDaysLogged;
    public String lastLogDate; 

    public UserStats() {
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.totalDaysLogged = 0;
        this.lastLogDate = "";
    }
}