package com.example.fitnessteamtracker.fitness;

import android.app.Activity;
import android.os.Bundle;

import com.example.fitnessteamtracker.Util.Consumer;

public interface FitnessListener {
    void start();
    void stop();

    int getCount();
}
