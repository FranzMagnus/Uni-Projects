package com.example.fitnessteamtracker.models.communication;

public class ExerciseData implements Sendable {
    private int id;
    private int count;

    public ExerciseData() {
    }

    public ExerciseData(int id, int count) {
        this.id = id;
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
