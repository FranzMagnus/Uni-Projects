package com.example.fitnessteamtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitnessteamtracker.Util.HttpPoster;
import com.example.fitnessteamtracker.models.Location;
import com.example.fitnessteamtracker.models.Match;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CreateActivity extends AppCompatActivity {

    private Location[] loc;
    private boolean mapset = false;
    private double difficulty = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        final CheckBox joker = findViewById(R.id.btn_Joker);
        final Button add = findViewById(R.id.btn_Add);
        final Button mapgen = findViewById(R.id.btn_Mapgen);
        final Button strt = findViewById(R.id.btn_Startgame);
        final TextView players = findViewById(R.id.txtv_players);
        final EditText number = findViewById(R.id.txt_number);
        final EditText id = findViewById(R.id.txt_ID);
        final ArrayList<Integer> teams = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();
        final ArrayList<Boolean> jokers = new ArrayList<>();
        final Spinner dropdown = findViewById(R.id.spinner);

        final String firebaseID = FirebaseAuth.getInstance().getUid();

        String[] items = new String[]{"Very Easy", "Easy", "Normal", "Hard", "Very Hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch((String) parent.getItemAtPosition(position)){
                    case "Very Easy":
                        difficulty = 0.5;
                        break;
                    case "Easy":
                        difficulty = 0.75;
                        break;
                    case "Normal":
                        difficulty = 1;
                        break;
                    case "Hard":
                        difficulty = 1.5;
                        break;
                    case "Very Hard":
                        difficulty = 2;
                        break;
                }
                Log.v("difficulty", String.valueOf(difficulty));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        // Button actions for navigating the menu
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teams.add(Integer.parseInt(number.getText().toString()));
                ids.add(id.getText().toString());
                jokers.add(joker.isChecked());
                players.append("ID: " + id.getText().toString() + " Team: " + number.getText() + " Joker: ");
                if(joker.isChecked()){
                    players.append("yes\n");
                }
                else{
                    players.append("no\n");
                }
                number.setText("");
                id.setText("");
                joker.setChecked(false);

            }
        });
        mapgen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateActivity.this, createMapActivity.class);
                startActivityForResult(intent, 0);

            }
        });

        strt.setOnClickListener(new View.OnClickListener() {
            //TODO: Create real activity, replace create map activity
            @Override
            public void onClick(View view) {
                if (mapset == true) {
                    Set<Integer> teamcount = new HashSet<>();
                    teamcount.addAll(teams);
                    int a = teamcount.size();
                    int[] c = new int[teams.size()];
                    String[] players = new String[teams.size()];
                    boolean[] jokerArray = new boolean[jokers.size()];
                    for (int i = 0; i < teams.size(); i++) {
                        c[i] = teams.get(i);
                        players[i] = ids.get(i);
                        jokerArray[i] = jokers.get(i);
                    }

                    Match dieter = new Match(firebaseID, players, c, loc, a, jokerArray, difficulty);
                    HttpPoster createPoster = new HttpPoster(dieter);
                    createPoster.execute("create", "match");
                    String result = "fail";
                    try {
                        result = createPoster.get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (createPoster.getResponseCode() != 200) {
                        Log.d("Poster", createPoster.getResponseCode() + " " + result);
                        Toast.makeText(CreateActivity.this, result, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(CreateActivity.this, GameActivity.class);
                        startActivity(intent);
                    }

                    /*Bundle b = new Bundle();
                    b.putSerializable("teams", teams.toArray());
                    b.putSerializable("ids", ids.toArray());
                    b.putSerializable("locations", loc);
                    intent.putExtras(b);

                     */


                }


            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                mapset = data.getBooleanExtra("created", false);
                if (mapset == true) {
                    double[] lats, lons;

                    lats = data.getDoubleArrayExtra("lats");
                    lons = data.getDoubleArrayExtra("lons");

                    loc = new Location[lats.length];
                    for (int i = 0; i < lats.length; i++) {
                        loc[i] = new Location(lats[i], lons[i]);
                    }

                }
            }

        }
    }
}