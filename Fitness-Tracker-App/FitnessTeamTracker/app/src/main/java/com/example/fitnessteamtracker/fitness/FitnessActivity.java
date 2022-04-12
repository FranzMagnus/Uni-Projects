package com.example.fitnessteamtracker.fitness;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fitnessteamtracker.R;
import com.example.fitnessteamtracker.Util.Consumer;

import java.util.Calendar;
import java.util.Date;

public class FitnessActivity extends AppCompatActivity {
    private int exerciseID, neededCount, time, timeRemaining;
    private String exerciseName;
    private FitnessListener fitnessListener;
    private ImageView countimage;
    private MediaPlayer mp;

    private TextView fitnessText, fitnessCount, timer;// timerText;

    CountDownTimer cTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            exerciseID = b.getInt("exerciseID");
            neededCount = b.getInt("neededCount");
            time = b.getInt("time");
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                exerciseDetected();
            }
        };
        Consumer c = new Consumer() {
            @Override
            public void accept(Object o) {

            }
        };
        if (exerciseID == 0) {
            fitnessListener = new PushupListener(this, savedInstanceState, r, c);
            exerciseName = "Pushups";
        } else if (exerciseID == 1) {
            fitnessListener = new JumpingJackListener(this, savedInstanceState, r, c);
            exerciseName = "Jumping Jacks";
        } else if (exerciseID == 2) {
            fitnessListener = new SquatListener(this, savedInstanceState, r, c);
            exerciseName = "Squats";
        } else if (exerciseID == 3) {
            fitnessListener = new StepListener(this, savedInstanceState, r);
            exerciseName = "Steps";
        }

        setContentView(R.layout.activity_fitness);
        countimage = findViewById(R.id.i_done);
        fitnessText = findViewById(R.id.txt_fitness_name);
        fitnessText.setText(exerciseName);
        fitnessCount = findViewById(R.id.txt_fitness_count);
        fitnessCount.setText("0/" + neededCount);
        mp = MediaPlayer.create(this,R.raw.ding);

        timer = findViewById(R.id.txt_fitness_timer);
        timer.setText(Integer.toString(time));
        // timerText = findViewById(R.id.txt_fitness_timer_text);

        cTimer = new CountDownTimer(time*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timeRemaining = seconds;
                timer.setText(Integer.toString(seconds));
                /*
                if(seconds == 1) {
                    timerText.setText("second left");
                }else if(seconds == 0) {
                    timerText.setText("seconds left");
                }

                 */
            }

            public void onFinish() {
                timerDone();
            }
        };
        cTimer.start();

        fitnessListener.start();
    }

    private void exerciseDetected() {
        fitnessCount.setText(fitnessListener.getCount() + "/" + neededCount);
        countimage.setVisibility(View.VISIBLE);
        //Date soon = new Date(Calendar.getInstance().getTimeInMillis()+500);
        if(mp!=null) {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(this, R.raw.ding);
            }
            mp.start();
        }
        Handler handler =new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countimage.setVisibility(View.INVISIBLE);
            }
        },500);
        if (fitnessListener.getCount() == neededCount) {
            exerciseDone();
        }
    }

    private void exerciseDone() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultText", "Exercise done!");
        resultIntent.putExtra("time", timeRemaining);
        resultIntent.putExtra("done", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void timerDone() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultText", "Timer ran out!");
        resultIntent.putExtra("time", timeRemaining);
        resultIntent.putExtra("done", false);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.release();
        mp=null;
        if(cTimer != null) {
            cTimer.cancel();
        }
    }
}
