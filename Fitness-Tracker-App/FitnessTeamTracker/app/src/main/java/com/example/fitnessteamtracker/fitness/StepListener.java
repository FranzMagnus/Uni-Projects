package com.example.fitnessteamtracker.fitness;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.fitnessteamtracker.Util.Vector3;

import static android.content.Context.SENSOR_SERVICE;

public class StepListener implements SensorEventListener, FitnessListener {
    private SensorManager sensorMan;
    private Sensor step;

    private boolean running;

    public int stepCount;

    private Runnable onStep;

    // Call in onCreate of the Activity
    public StepListener(Activity activity, Bundle savedInstanceState, Runnable onStep) {
        sensorMan = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        step = sensorMan.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        this.onStep = onStep;
    }

    // Start listening
    public void start() {
        register();

        running = true;
    }

    private void register() {
        sensorMan.registerListener(this, step, SensorManager.SENSOR_DELAY_UI);
    }

    // Stop listening
    public void stop() {
        running = false;
        sensorMan.unregisterListener(this);
    }

    @Override
    public int getCount() {
        return stepCount;
    }

    // Call in onResume of the Activity
    public void resume() {
        if (running) {
            register();
        }
    }

    // Call in onPause of the Activity
    public void pause() {
        if (running) {
            sensorMan.unregisterListener(this);
        }
    }

    private Vector3 lastGravity;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepDetected();
        }
    }

    private void stepDetected() {
        stepCount++;
        onStep.run();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }
}
