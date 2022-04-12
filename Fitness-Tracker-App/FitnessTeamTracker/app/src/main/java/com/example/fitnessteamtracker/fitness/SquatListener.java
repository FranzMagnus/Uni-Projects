package com.example.fitnessteamtracker.fitness;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.example.fitnessteamtracker.Util.Consumer;
import com.example.fitnessteamtracker.Util.Vector3;

import java.util.Calendar;

import static android.content.Context.SENSOR_SERVICE;

public class SquatListener implements SensorEventListener, FitnessListener {
    private SensorManager sensorMan;
    private Sensor gravity, linearAcceleration;

    private boolean running;

    public int squatCount;

    private Runnable onSquat;
    private Consumer<FailedSquat> onFailedSquat;

    public enum FailedSquat {
        TooFast;
    }

    // Call in onCreate of the Activity
    public SquatListener(Activity activity, Bundle savedInstanceState, Runnable onSquat, Consumer<FailedSquat> onFailedSquat) {
        sensorMan = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        linearAcceleration = sensorMan.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravity = sensorMan.getDefaultSensor(Sensor.TYPE_GRAVITY);

        this.onSquat = onSquat;
        this.onFailedSquat = onFailedSquat;
    }

    // Start listening
    public void start() {
        register();

        running = true;
    }

    private void register() {
        sensorMan.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_UI);
        sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
    }

    // Stop listening
    public void stop() {
        running = false;
        sensorMan.unregisterListener(this);
    }

    @Override
    public int getCount() {
        return squatCount;
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
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float[] grav = event.values.clone();
            float x = grav[0];
            float y = grav[1];
            float z = grav[2];
            lastGravity = new Vector3(x, y, z);
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float[] acc = event.values.clone();
            float x = acc[0];
            float y = acc[1];
            float z = acc[2];
            Vector3 cur = new Vector3(x, y, z);

            checkSquat(cur);
        }

    }

    private boolean inDownMovement, inUpMovement;
    private long downMovementStarted, upMovementStarted;

    private int moveTime = 500;

    private void checkSquat(Vector3 linAcc) {
        if(lastGravity == null) {
            return;
        }

        // don't care about small movements
        if (linAcc.length() < 1) {
            long curTime = Calendar.getInstance().getTimeInMillis();
            if(inUpMovement && curTime > upMovementStarted + moveTime) {
                squatDetected();
                inUpMovement = false;
            }
            return;
        }

        boolean movingDown = false;
        float angleDeg = linAcc.angleToDeg(lastGravity);
        if (angleDeg < 20) {
            movingDown = true;
        }
        boolean movingUp = false;
        if(angleDeg > 160) {
            movingUp = true;
        }

        if(movingDown && !inDownMovement && !inUpMovement) {
            inDownMovement = true;
            downMovementStarted = Calendar.getInstance().getTimeInMillis();
        }
        if(movingUp && inDownMovement) {
            long time = Calendar.getInstance().getTimeInMillis();
            if(time > downMovementStarted + moveTime) {
                inDownMovement = false;
                inUpMovement = true;
                upMovementStarted = time;
            }else {
                failedSquat(FailedSquat.TooFast);
                inUpMovement = false;
                inDownMovement = false;
            }
        }

    }

    private void squatDetected() {
        squatCount++;
        onSquat.run();
    }

    private void failedSquat(FailedSquat reason) {
        onFailedSquat.accept(reason);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }
}
