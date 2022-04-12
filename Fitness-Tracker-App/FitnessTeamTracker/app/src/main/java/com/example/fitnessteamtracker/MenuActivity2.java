package com.example.fitnessteamtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.fitnessteamtracker.Util.HttpGetter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutionException;

public class MenuActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);

        final Button creation = findViewById(R.id.btn_Create);
        final Button stats = findViewById(R.id.btn_Stats);
        final Button join = findViewById(R.id.btn_Join);
        final Button training = findViewById(R.id.btn_Training);


        // Button actions for navigating the menu
        creation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MenuActivity2.this, CreateActivity.class);
                startActivity(intent);
            }
        });
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MenuActivity2.this, StatsActivity.class);
                startActivity(intent);
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpGetter getter = new HttpGetter();
                getter.execute("ingame", FirebaseAuth.getInstance().getUid());
                int status = -1;
                try {
                    String s = getter.get();
                    status = getter.getResponseCode();

                    if(status == 200) {
                        Intent intent = new Intent(MenuActivity2.this, GameActivity.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(MenuActivity2.this, "You weren't added to a game", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MenuActivity2.this, "There was an error", Toast.LENGTH_SHORT).show();
                }

            }
        });
        training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity2.this, ChallengesActivity.class);
                startActivity(intent);


            }
        });

        int MY_RESULT_FINE_LOCATION = 0;
        int MY_RESULT_ACTIVITY = 0;
        if (ContextCompat.checkSelfPermission(MenuActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MenuActivity2.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_RESULT_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MenuActivity2.this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MenuActivity2.this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    MY_RESULT_ACTIVITY);
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(MenuActivity2.this).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Doing this will log you out");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MenuActivity2.this,LoginActivity.class);
                startActivity(intent);
                dialogInterface.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }
}