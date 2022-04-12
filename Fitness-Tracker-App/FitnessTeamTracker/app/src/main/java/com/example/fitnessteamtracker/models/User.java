package com.example.fitnessteamtracker.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class User{

    //userdata
    String firebaseID;
    String username;
    int matches;
    int wins;
    String[] friends;
    String[] rivals;
    Map<String,Integer> gamesPlayedWith = new HashMap<>();  //Maps the number of games played to the corresponding FirebaseID (of your partner)
    Map<String,Integer> jokersWith = new HashMap<>();       //Maps the number of available Jokers to the corresponding FirebaseID (of your partner)

    //constructors
    public User(String firebaseID, String name){
        this.firebaseID = firebaseID;
        this.username = name;
        this.matches = 0;
        this.wins = 0;
        this.friends = new String[0];
        this.rivals = new String[0];
    }

    //Dummy
    public User(){

    }

    //Getters and Setters
    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = matches;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public String[] getFriends() {
        return friends;
    }

    public void setFriends(String[] friends) {
        this.friends = friends;
    }

    public String[] getRivals() {
        return rivals;
    }

    public void setRivals(String[] rivals) {
        this.rivals = rivals;
    }

    public Map<String, Integer> getGamesPlayedWith() {
        return gamesPlayedWith;
    }

    public void setGamesPlayedWith(Map<String, Integer> gamesPlayedWith) { this.gamesPlayedWith = gamesPlayedWith; }

    public Map<String, Integer> getJokersWith() {
        return jokersWith;
    }

    public void setJokersWith(Map<String, Integer> jokersWith) {
        this.jokersWith = jokersWith;
    }
}