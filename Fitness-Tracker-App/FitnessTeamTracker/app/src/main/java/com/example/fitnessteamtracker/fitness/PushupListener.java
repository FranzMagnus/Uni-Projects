package com.example.fitnessteamtracker.fitness;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.fitnessteamtracker.Util.Consumer;
import com.example.fitnessteamtracker.Util.Vector3;
import com.google.firebase.installations.FirebaseInstallationsApi;

import java.util.Calendar;

import static android.content.Context.SENSOR_SERVICE;

public class PushupListener implements SensorEventListener, FitnessListener {
    private SensorManager sensorMan;
    private Sensor gravity;

    private boolean running;

    private boolean topReached, bottomReached, pushupStarted;
    private long lastTop;
    public int pushupCount;

    protected float topLimit = 20f;
    protected float botLimit = 85f;
    protected long timeLimit = 1000;

    private Runnable onPushup;
    private Consumer<FailedPushup> onFailedPushup;

    public enum FailedPushup {
        TooFast, NotDeepEnough;
    }

    // Call in onCreate of the Activity
    public PushupListener(Activity activity, Bundle savedInstanceState, Runnable onPushup, Consumer<FailedPushup> onFailedPushup) {
        sensorMan = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        gravity  = sensorMan.getDefaultSensor(Sensor.TYPE_GRAVITY);

        this.onPushup = onPushup;
        this.onFailedPushup = onFailedPushup;
    }

    // Start listening
    public void start() {
        sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);

        running = true;
    }

    // Stop listening
    public void stop() {
        running = false;
        sensorMan.unregisterListener(this);
    }

    @Override
    public int getCount() {
        return pushupCount;
    }

    // Call in onResume of the Activity
    public void resume() {
        if(running) {
            sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Call in onPause of the Activity
    public void pause() {
        if(running) {
            sensorMan.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float[] grav = event.values.clone();
            float x = grav[0];
            float y = grav[1];
            float z = grav[2];
            Vector3 cur = new Vector3(x,y,z);

            Vector3 up = new Vector3(0,1,0);
            float angle = cur.angleTo(up);
            float angleDeg = (float) Math.toDegrees(angle);

            checkPushup(angleDeg);
        }

    }

    private void checkPushup(float angleInDegrees) {
        if(!topReached) {
            if(angleInDegrees < topLimit) {
                topReached = true;
                lastTop = Calendar.getInstance().getTimeInMillis();
            }
        }else if(!bottomReached) {
            if(angleInDegrees > botLimit) {
                bottomReached = true;
            }else if(!pushupStarted) {
                if(angleInDegrees > topLimit) {
                    pushupStarted = true;
                }
            }else if(pushupStarted) {
                if(angleInDegrees < topLimit) {
                    failedPushup(FailedPushup.NotDeepEnough);
                    pushupStarted = false;
                }
            }
        }else {
            if(angleInDegrees < topLimit) {
                long curTime = Calendar.getInstance().getTimeInMillis();
                if(curTime - lastTop >= timeLimit) {
                    pushupDetected();
                }else{
                    failedPushup(FailedPushup.TooFast);
                }
                lastTop = curTime;
                bottomReached = false;
                pushupStarted = false;
            }
        }
    }

    private void pushupDetected() {
        pushupCount++;
        onPushup.run();
    }

    private void failedPushup(FailedPushup reason) {
        onFailedPushup.accept(reason);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }
}
