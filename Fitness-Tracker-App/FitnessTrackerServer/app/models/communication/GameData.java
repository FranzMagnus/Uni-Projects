package models.communication;


import models.Location;

public class GameData implements Sendable {
    private Location[] locations;
    private UserData[] userdatas;
    private int clientTeam;
    private boolean isJoker;
    private double difficulty;
    private int nextTarget;

    public int getNextTarget() {
        return nextTarget;
    }

    public void setNextTarget(int nextTarget) {
        this.nextTarget = nextTarget;
    }

    public boolean isJoker() {
        return isJoker;
    }

    public void setJoker(boolean joker) {
        isJoker = joker;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    private int teamCount;

    public int getClientTeam() {
        return clientTeam;
    }

    public void setClientTeam(int clientTeam) {
        this.clientTeam = clientTeam;
    }

    public GameData() {
    }

    public GameData(Location[] locations, UserData[] userdatas, int clientTeam, int teamCount, boolean isJoker, double difficulty, int nextTarget) {
        this.locations = locations;
        this.userdatas = userdatas;
        this.clientTeam = clientTeam;
        this.teamCount = teamCount;
        this.isJoker = isJoker;
        this.difficulty = difficulty;
        this.nextTarget = nextTarget;
    }

    public Location[] getLocations() {
        return locations;
    }

    public void setLocations(Location[] locations) {
        this.locations = locations;
    }

    public UserData[] getUserdatas() {
        return userdatas;
    }

    public void setUserdatas(UserData[] userdatas) {
        this.userdatas = userdatas;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getDifficulty() {
        return difficulty;
    }
}
