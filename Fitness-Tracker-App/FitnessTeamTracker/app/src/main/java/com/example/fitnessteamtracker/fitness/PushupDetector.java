package com.example.fitnessteamtracker.fitness;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class PushupDetector extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorMan;
    private Sensor gravity;

    private boolean running;

    private boolean topReached, bottomReached, pushupStarted;
    private long lastTop;
    protected int pushups;

    protected float topLimit = 20f;
    protected float botLimit = 85f;
    protected long timeLimit = 1000;

    private class Vector3 {
        public float x,y,z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return (float) Math.sqrt(x*x + y*y + z*z);
        }

        public float scalar(Vector3 other) {
            return x*other.x + y*other.y + z*other.z;
        }

        public float angleTo(Vector3 other) {
            float za = scalar(other);
            float nen = length() * other.length();
            float co = za / nen;
            return (float) Math.acos(co);
        }

        public String toString() {
            return "x: " + x + " y: " + y + " z: " + z;
        }
    }

    public enum FailedPushup {
        TooFast, NotDeepEnough;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
        gravity  = sensorMan.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    protected void start() {
        sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);

        running = true;
    }

    protected void stop() {
        running = false;
        sensorMan.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(running) {
            sensorMan.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                    pushups++;
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

    protected void pushupDetected() {

    }

    protected void failedPushup(FailedPushup reason) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }
}
