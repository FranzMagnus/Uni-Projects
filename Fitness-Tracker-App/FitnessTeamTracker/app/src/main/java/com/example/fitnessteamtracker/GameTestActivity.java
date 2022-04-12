package com.example.fitnessteamtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fitnessteamtracker.Util.HttpPoster;
import com.example.fitnessteamtracker.fitness.FitnessActivity;
import com.example.fitnessteamtracker.models.Location;
import com.example.fitnessteamtracker.models.Match;
import com.example.fitnessteamtracker.models.Player;
import com.example.fitnessteamtracker.models.User;

public class GameTestActivity extends GameHandler {

    private TextView txtMessage, txtData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_test);

        txtMessage = findViewById(R.id.txt_test_message);
        txtData = findViewById(R.id.txt_test_data);

        Button btnReady = findViewById(R.id.btn_test_ready);
        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ready();
            }
        });

        Button btnDummy = findViewById(R.id.btn_test_dummy);
        btnDummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDummyGame();
            }
        });
    }

    private void createDummyGame() {
        String[] players = new String[]{"myID1","myID"};
        int[] playerTeam = new int[]{0,0};
        Location[] locations = new Location[] {new Location(0,0), new Location(1,1)};
        String matchID = "0";
        boolean[] jokers = {false, false};
        Match m = new Match(matchID, players, playerTeam, locations, 1,jokers, 1);

        new HttpPoster(m).execute("create","match");
        new HttpPoster(createUser(players[0])).execute("insert","user");
        new HttpPoster(createUser(players[1])).execute("insert","user");
    }

    private User createUser(String id) {
        User u = new User(id, id);
        return u;
    }


    @Override
    protected void receiveMessage(String message) {
        super.receiveMessage(message);
        final String m = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtMessage.setText(m);
            }
        });

    }

    @Override
    protected void updateUI() {
        super.updateUI();
        txtData.setText("Current position: " + currentPositions[currentGame.getClientTeam()]);
    }
}
