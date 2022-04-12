package com.example.fitnessteamtracker.fitness;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.example.fitnessteamtracker.Util.Consumer;
import com.example.fitnessteamtracker.Util.Vector3;

import java.util.Calendar;

import static android.content.Context.SENSOR_SERVICE;

public class JumpingJackListener implements SensorEventListener, FitnessListener {
    private SensorManager sensorMan;
    private Sensor gravity, linearAcceleration;

    private boolean running;

    public int jumpingJackCount;

    private Runnable onJumpingJack;
    private Consumer<FailedJumpingJack> onFailedJumpingJack;

    public enum FailedJumpingJack {
        TooFast, NotHighEnough, TooSlow;
    }

    // Call in onCreate of the Activity
    public JumpingJackListener(Activity activity, Bundle savedInstanceState, Runnable onJumpingJack, Consumer<FailedJumpingJack> onFailedJumpingJack) {
        sensorMan = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        gravity  = sensorMan.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcceleration  = sensorMan.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        this.onJumpingJack = onJumpingJack;
        this.onFailedJumpingJack = onFailedJumpingJack;
    }

    // Start listening
    public void start() {
        sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
        sensorMan.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_UI);

        running = true;
    }

    // Stop listening
    public void stop() {
        running = false;
        sensorMan.unregisterListener(this);
    }

    @Override
    public int getCount() {
        return jumpingJackCount;
    }

    // Call in onResume of the Activity
    public void resume() {
        if(running) {
            sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
            sensorMan.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_UI);
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

            Vector3 down = new Vector3(0,1,0);
            float angle = cur.angleTo(down);
            float angleDeg = (float) Math.toDegrees(angle);

            checkJumpingJackAngle(angleDeg);
        }

        if (!speedReached && event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float[] acc = event.values.clone();
            float x = acc[0];
            float y = acc[1];
            float z = acc[2];
            Vector3 cur = new Vector3(x,y,z);

            checkJumpingJackSpeed(cur.length());
        }

    }

    float botLimit = 20f;
    float topLimit = 150f;
    long lastBot;

    boolean botReached, topReached, jJStarted, speedReached;

    int timeLimit = 300;

    private void checkJumpingJackAngle(float angleInDegrees) {
        if(!botReached) {
            if(angleInDegrees < botLimit) {
                botReached = true;
                lastBot = Calendar.getInstance().getTimeInMillis();
            }
        }else if(!topReached) {
            if(angleInDegrees > topLimit) {
                topReached = true;
            }else if(!jJStarted) {
                if(angleInDegrees > botLimit) {
                    jJStarted = true;
                }
            }else if(jJStarted) {
                if(angleInDegrees < botLimit) {
                    failedJumpingJack(FailedJumpingJack.NotHighEnough);
                    jJStarted = false;
                }
            }
        }else {
            if(angleInDegrees < botLimit) {
                long curTime = Calendar.getInstance().getTimeInMillis();
                if(curTime - lastBot >= timeLimit) {
                    jumpingJackDetected();
                }else if(!speedReached) {
                    failedJumpingJack(FailedJumpingJack.TooSlow);
                }else {
                    failedJumpingJack(FailedJumpingJack.TooFast);
                }
                lastBot = curTime;
                topReached = false;
                jJStarted = false;
                speedReached = false;
            }
        }
    }

    private void checkJumpingJackSpeed(float speed) {
        if(speed > 3) {
            speedReached = true;
        }
    }

    private void jumpingJackDetected() {
        jumpingJackCount++;
        onJumpingJack.run();
    }

    private void failedJumpingJack(FailedJumpingJack reason) {
        onFailedJumpingJack.accept(reason);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }
}
