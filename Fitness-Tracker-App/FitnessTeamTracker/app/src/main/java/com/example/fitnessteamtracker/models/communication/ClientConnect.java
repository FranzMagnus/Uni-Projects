package com.example.fitnessteamtracker.models.communication;

public class ClientConnect implements Sendable {
    private String firebaseID;

    public ClientConnect() {

    }

    public ClientConnect(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }
}
