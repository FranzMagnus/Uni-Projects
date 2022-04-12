package com.example.fitnessteamtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import com.example.fitnessteamtracker.models.Location;

public class createMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    int MY_RESULT_FINE_LOCATION;
    private List<LatLng> locations = new ArrayList<LatLng>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(createMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(createMapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_RESULT_FINE_LOCATION);
        } else {

            mMap.setMyLocationEnabled(true);
            final Button done = findViewById(R.id.doneButton);
            final Button remove = findViewById(R.id.removeButton);
            done.setVisibility(View.VISIBLE);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    remove.setVisibility(View.GONE);
                    mMap.addMarker(new MarkerOptions().position(latLng));
                    locations.add(latLng);
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {
                    remove.setVisibility(View.VISIBLE);
                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            locations.remove(marker.getPosition());
                            marker.remove();
                            remove.setVisibility(View.GONE);
                        }
                    });
                    return false;
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mapCreated();
                }
            });
        }

    }

    private void mapCreated() {
        // Location[] mapLocations = new Location[locations.size()];
        double[] lats = new double[locations.size()];
        double[] lons = new double[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            //mapLocations[i] = new Location(locations.get(i).latitude, locations.get(i).longitude);
            lats[i] = locations.get(i).latitude;
            lons[i] = locations.get(i).longitude;
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("resultText", "Map created!");
        //resultIntent.putExtra("locations", mapLocations);
        resultIntent.putExtra("lats", lats);
        resultIntent.putExtra("lons", lons);
        resultIntent.putExtra("created", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
