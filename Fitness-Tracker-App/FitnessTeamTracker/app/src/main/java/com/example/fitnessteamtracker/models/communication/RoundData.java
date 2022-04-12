package com.example.fitnessteamtracker.models.communication;

public class RoundData implements Sendable {
    private ExerciseData[] exercises;
    private int time;

    public RoundData() {
    }

    public RoundData(ExerciseData[] exercises, int time) {
        this.exercises = exercises;
        this.time = time;
    }

    public ExerciseData[] getExercises() {
        return exercises;
    }

    public void setExercises(ExerciseData[] exercises) {
        this.exercises = exercises;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
