package com.example.calorietracker;

public class Feedback {
    public boolean wasAccurate;
    public String comment;
    public long timestamp;

    public Feedback() {}

    public Feedback(boolean wasAccurate, String comment, long timestamp) {
        this.wasAccurate = wasAccurate;
        this.comment = comment;
        this.timestamp = timestamp;
    }
}
