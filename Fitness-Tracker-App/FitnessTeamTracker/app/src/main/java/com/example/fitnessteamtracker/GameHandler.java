package com.example.fitnessteamtracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.example.fitnessteamtracker.Util.Configuration;
import com.example.fitnessteamtracker.fitness.FitnessActivity;
import com.example.fitnessteamtracker.models.communication.ClientConnect;
import com.example.fitnessteamtracker.models.communication.Done;
import com.example.fitnessteamtracker.models.communication.ExerciseData;
import com.example.fitnessteamtracker.models.communication.GameData;
import com.example.fitnessteamtracker.models.communication.NewPosition;
import com.example.fitnessteamtracker.models.communication.Packet;
import com.example.fitnessteamtracker.models.communication.Ranking;
import com.example.fitnessteamtracker.models.communication.Ready;
import com.example.fitnessteamtracker.models.communication.RoundData;
import com.example.fitnessteamtracker.models.communication.UserData;
import com.example.fitnessteamtracker.models.communication.Winner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import tech.gusavila92.websocketclient.WebSocketClient;

public class GameHandler extends FragmentActivity {
    private WebSocketClient webSocketClient;

    protected GameData currentGame;
    protected RoundData currentRound;
    protected int currentExercise;
    protected int timeRemaining;
    protected int exercisesDone;
    protected int[] currentPositions;
    protected boolean joker;

    // private final String id = "myID1";
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        id = FirebaseAuth.getInstance().getUid();

        createWebSocketClient();
    }

    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://" + Configuration.ServerIP + "/ws");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                webSocketOpened();
            }

            @Override
            public void onTextReceived(String s) {
                webSocketMessageReceived(s);
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
                Log.d("WebSocket", e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                webSocketClosed();
            }
        };
        webSocketClient.setConnectTimeout(Integer.MAX_VALUE);
        // webSocketClient.setReadTimeout(Integer.MAX_VALUE);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    private void send(String message) {
        Log.d("WebSocket", "Sent: " + message);
        webSocketClient.send(message);
    }

    private void webSocketOpened() {
        Log.i("WebSocket", "WebSocket Created");
        ClientConnect cC = new ClientConnect(id);
        try {
            send(Packet.createPacket(cC));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void webSocketMessageReceived(String message) {
        Log.i("WebSocket", "Message received: " + message);

        receiveMessage(message);
        handleMessage(message);
    }

    protected void receiveMessage(String message) {

    }

    private void webSocketClosed() {
        Log.i("WebSocket", "Closed ");
        System.out.println("onCloseReceived");
    }

    public void handleMessage(String message) {
        try {
            Packet p = new Packet(message);

            Log.d("GameHandler", "Packet type " + p.type);
            if (p.type == 1) {
                handleGameData((GameData) p.value);
            }else if (p.type == 3) {
                handleRoundData((RoundData) p.value);
            }else if (p.type == 5) {
                handleWinner((Winner) p.value);
            }else if (p.type == 6) {
                handleNewPosition((NewPosition) p.value);
            }else if (p.type == 7) {
                handleRanking((Ranking) p.value);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGameData(GameData gameData) {
        // save GameData
        if(currentGame == null) {
            currentGame = gameData;
            currentPositions = new int[gameData.getTeamCount()];
            joker = gameData.isJoker();

            onGameData(gameData);

            initialize();
        }
        // TODO: display this stuff
        updateUI();
    }

    protected void onGameData(GameData gameData) {

    }

    protected void initialize() {

    }

    private void handleRoundData(RoundData roundData) {
        // save the exercises
        currentRound = roundData;
        currentExercise = 0;
        exercisesDone = 0;
        timeRemaining = roundData.getTime();

        // start the first exercise
        startNextExercise();
    }

    private void startNextExercise() {
        if(currentExercise < currentRound.getExercises().length) {
            startExercise(currentRound.getExercises()[currentExercise++]);
        }else {
            Log.d("GameHandler", "not enough exercises");
            roundOver();
        }
    }

    private void startExercise(ExerciseData exerciseData) {
        Intent intent = new Intent(GameHandler.this, FitnessActivity.class);
        Bundle b = new Bundle();
        b.putInt("exerciseID", exerciseData.getId());
        b.putInt("neededCount", exerciseData.getCount());
        b.putInt("time", timeRemaining);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            boolean done = false;
            if(data != null) {
                timeRemaining = data.getIntExtra("time", 0);
                done = data.getBooleanExtra("done", false);
            }else {
                timeRemaining = 0;
            }
            if(done) {
                exercisesDone++;
            }

            // start next exercise
            if(timeRemaining > 0) {
                startNextExercise();
            }else {
                roundOver();
            }
        }
    }

    protected void backFromExercises() {

    }

    private void roundOver() {
        try {
            send(Packet.createPacket(new Done(id,exercisesDone)));
            backFromExercises();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void handleWinner(Winner winner) {
        onWinner(winner.getTeamId());
    }

    protected void onWinner(int teamID) {

    }

    private void handleNewPosition(NewPosition newPosition) {
        currentPositions[newPosition.getTeamId()] = newPosition.getNewPosId();

        updateUI();
    }

    private void handleRanking(Ranking ranking) {
        // disconnect
        webSocketClient.close();

        Intent intent = new Intent(GameHandler.this, EndGameActivity.class);
        Bundle b = new Bundle();
        UserData[] users = currentGame.getUserdatas();
        String[] players = new String[users.length];
        int[] playerTeams = new int[users.length];
        for(int i = 0;i<players.length;i++) {
            players[i] = users[i].getUsername();
            playerTeams[i] = users[i].getTeamId();
        }

        b.putStringArray("players", players);
        b.putIntArray("playerTeams", playerTeams);
        b.putIntArray("turnsPerTeam", ranking.getTurnsPerTeam());
        intent.putExtras(b);
        startActivity(intent);
    }

    protected void ready() {
        try {
            send(Packet.createPacket(new Ready(id)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    protected void updateUI() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketClient.close();
    }
}
