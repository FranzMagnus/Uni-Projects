package com.example.fitnessteamtracker.models;

public class Player {
    //playerdata
    String hostID;
    String firebaseID;
    int current_loc_index;
    int turn_number;
    int teamID;
    boolean ready;
    boolean finished;
    boolean joker;

    public void setCurrent_loc_index(int current_loc_index) {
        this.current_loc_index = current_loc_index;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    //Constructors
    public Player(String firebaseID, String hostID, int teamID, boolean joker) {
        this.firebaseID = firebaseID;
        this.hostID = hostID;
        this.current_loc_index = 0;
        this.turn_number = 0;
        this.teamID = teamID;
        this.ready = false;
        this.finished = false;
        this.joker = joker;
    }
    //Dummy
    public Player(){

    }

    //Getters and Setters
    public boolean isJoker() { return joker; }

    public void setJoker(boolean joker) { this.joker = joker; }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public int getCurrent_loc_index() {
        return current_loc_index;
    }

    public void setCurrent_loc(int current_loc_index) {
        this.current_loc_index = current_loc_index;
    }

    public int getTurn_number() {
        return turn_number;
    }

    public void setTurn_number(int turn_number) {
        this.turn_number = turn_number;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public boolean getReady(){
        return ready;
    }

    public void setReady(boolean ready){
        this.ready = ready;
    }
}