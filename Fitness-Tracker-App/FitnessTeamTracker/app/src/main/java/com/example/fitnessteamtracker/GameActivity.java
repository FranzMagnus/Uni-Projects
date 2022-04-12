package com.example.fitnessteamtracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fitnessteamtracker.models.Location;
import com.example.fitnessteamtracker.models.communication.GameData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class GameActivity extends GameHandler implements OnMapReadyCallback {
    final int[] teamColors = new int[]{Color.CYAN, Color.YELLOW, Color.RED, Color.MAGENTA};

    private GoogleMap mMap;

    private Marker[] markers;
    private Marker[] teams;

    private int myNextPos;
    private boolean mapReady;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private double distanceFromNextTarget;

    private TextView txtDistance;
    private Button btnReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_game);
        mapFragment.getMapAsync(this);

        btnReady = findViewById(R.id.btn_game_ready);
        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (distanceFromNextTarget < 20) {
                    ready();
                }
            }
        });

        txtDistance = findViewById(R.id.txt_game_distance);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (android.location.Location location : locationResult.getLocations()) {
                    if (mapReady && currentGame != null) {
                        Location loc = new Location(location.getLatitude(), location.getLongitude());
                        int myTargetLocation = currentPositions[currentGame.getClientTeam()];
                        distanceFromNextTarget = distanceBetweenCoordinates(loc, currentGame.getLocations()[myTargetLocation]);
                        double distanceFromNextPos = distanceBetweenCoordinates(loc, currentGame.getLocations()[myNextPos]);
                        String dist = Integer.toString((int) distanceFromNextPos);
                        txtDistance.setText(dist + " meters");
                        if (distanceFromNextTarget < 20) {
                            if (myNextPos >= myTargetLocation) {
                                btnReady.setVisibility(View.VISIBLE);
                            }
                            // stopLocationUpdates();
                        } else {
                            btnReady.setVisibility(View.GONE);
                        }
                        if (distanceFromNextPos < 20) {
                            if (myNextPos < currentGame.getLocations().length - 1) {
                                myNextPos++;
                                updateUI();
                            }
                        }
                    }
                }
            }
        };


        createLocationRequest();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Log.d("GameActivity", "initializing");

        if (!joker) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startLocationUpdates();
                }
            });

        } else {
            distanceFromNextTarget = 0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtDistance.setText("Joker");
                    btnReady.setVisibility(View.VISIBLE);

                }
            });

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        mapReady = true;
        updateUI();
    }

    private void addMarkers() {
        Location[] locations = currentGame.getLocations();
        if (markers == null) {
            markers = new Marker[locations.length];
        }

        for (int i = 0; i < locations.length; i++) {
            LatLng p = new LatLng(locations[i].getLat(), locations[i].getLon());
            String title = "Wegpunkt " + i;
            if (i == 0) {
                title = "Start";
            } else if (i == locations.length - 1) {
                title = "Ziel";
            }
            MarkerOptions marker = new MarkerOptions().position(p).title(title);
            markers[i] = mMap.addMarker(marker);
        }
    }

    private void addTeamCircles() {
        if (teams == null) {
            teams = new Marker[currentGame.getTeamCount()];
        }

        for (int i = 0; i < currentGame.getTeamCount(); i++) {
            if (i != currentGame.getClientTeam()) {
                LatLng pos = getTeamMarkerPos(i);
                MarkerOptions marker = new MarkerOptions().position(pos).title("Team " + i).icon(getBitmapFromVector(this, R.drawable.ic_team, teamColors[i]));
                teams[i] = mMap.addMarker(marker);
            }
        }
    }

    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context,
                                                       @DrawableRes int vectorResourceId,
                                                       @ColorInt int tintColor) {

        Drawable vectorDrawable = ResourcesCompat.getDrawable(
                context.getResources(), vectorResourceId, null);
        if (vectorDrawable == null) {
            Log.e("Bitmap", "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, tintColor);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private LatLng getTeamMarkerPos(int teamNumber) {
        float factor = 0.00005f;
        float angleBetween = 0;
        if (currentGame.getTeamCount() > 2) {
            angleBetween = (float) Math.PI / (currentGame.getTeamCount() - 2);
        }
        double curAngle = Math.PI;
        int teamPos = teamNumber;
        if (teamNumber > currentGame.getClientTeam()) {
            teamPos--;
        }
        curAngle += teamPos * angleBetween;

        int teamLoc = currentPositions[teamNumber];
        Location[] locations = currentGame.getLocations();
        double lat = locations[teamLoc].getLat() + Math.sin(curAngle) * factor;
        double lon = locations[teamLoc].getLon() + Math.cos(curAngle) * factor;
        return new LatLng(lat, lon);
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("UI", "update");

                if (currentGame == null || !mapReady) {
                    return;
                }

                if (markers == null) {
                    addMarkers();
                    addTeamCircles();
                }

                int pos = currentPositions[currentGame.getClientTeam()];
                if (!joker) {
                    for (int i = 0; i < myNextPos; i++) {
                        markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                    if (myNextPos < pos) {
                        markers[myNextPos].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    }
                } else {
                    for (int i = 0; i < pos; i++) {
                        markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                }
                markers[pos].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


                for (int i = 0; i < currentGame.getTeamCount(); i++) {
                    if (i != currentGame.getClientTeam()) {
                        teams[i].setPosition(getTeamMarkerPos(i));
                    }
                }
            }
        });

    }

    @Override
    protected void onWinner(int teamID) {
        super.onWinner(teamID);
        if (teamID == currentGame.getClientTeam()) {

        } else {
            teams[teamID].setIcon(getBitmapFromVector(this, R.drawable.ic_team_finished, teamColors[teamID]));
        }
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private double distanceBetweenCoordinates(Location loc1, Location loc2) {
        int earthRadiusKm = 6371;

        double dLat = Math.toRadians(loc2.getLat() - loc1.getLat());
        double dLon = Math.toRadians(loc2.getLon() - loc1.getLon());

        double lat1 = Math.toRadians(loc1.getLat());
        double lat2 = Math.toRadians(loc2.getLat());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c * 1000;
    }

    @Override
    protected void backFromExercises() {
        super.backFromExercises();
        // startLocationUpdates();
    }

    @Override
    protected void onGameData(GameData gameData) {
        super.onGameData(gameData);
        myNextPos = gameData.getNextTarget();
    }
}
