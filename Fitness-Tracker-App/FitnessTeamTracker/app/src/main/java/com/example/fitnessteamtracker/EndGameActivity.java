package com.example.fitnessteamtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class EndGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        final Button btnMainMenu = findViewById(R.id.btn_end_menu);
        btnMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EndGameActivity.this, MenuActivity2.class);
                startActivity(intent);
            }
        });

        Bundle b = getIntent().getExtras();
        String[] players;
        int[] playerTeams;
        int[] turnsPerTeam;
        if (b != null) {
            players = b.getStringArray("players");
            playerTeams = b.getIntArray("playerTeams");
            turnsPerTeam = b.getIntArray("turnsPerTeam");
        }else {
            return;
        }
        int[] ranking = findRanking(turnsPerTeam);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rec_end_score);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter mAdapter = new MyAdapterScoreboard(ranking, turnsPerTeam);
        recyclerView.setAdapter(mAdapter);


        RecyclerView recyclerView2 = (RecyclerView) findViewById(R.id.rec_end_players);
        recyclerView2.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        recyclerView2.setLayoutManager(layoutManager2);

        RecyclerView.Adapter mAdapter2 = new MyAdapterPlayers(players, playerTeams);
        recyclerView2.setAdapter(mAdapter2);
    }

    private int[] findRanking(int[] turnsPerTeam) {
        int[] tPT = turnsPerTeam.clone();
        int[] teams = new int[tPT.length];
        for(int i = 0;i<tPT.length;i++) {
            teams[i] = findLowest(tPT);
        }
        return teams;
    }

    private int findLowest(int[] tPT) {
        int lowest = tPT[0];
        int lowestAt = 0;
        for(int i = 1;i<tPT.length;i++) {
            if(tPT[i] < lowest) {
                lowest = tPT[i];
                lowestAt = i;
            }
        }

        tPT[lowestAt] = Integer.MAX_VALUE;
        return lowestAt;
    }


}
