package models;

public class Match {
    String hostID;
    String[] players;

    int[] playerTeam;
    Location[] locations;
    int status;
    String winners[];
    int teamCount;
    boolean[] jokers;
    double difficulty;

    //Constructors
    public Match(String hostID, String[] players, int[] playerTeam, Location[] locations, int teamCount, boolean[] jokers, double difficulty){
        this.hostID = hostID;
        this.players = players;
        this.playerTeam = playerTeam;
        this.locations = locations;
        this.status = 0;
        this.winners = new String[0];
        this.teamCount = teamCount;
        this.jokers = jokers;
        this.difficulty = difficulty;
    }
    //Dummy
    public Match(){

    }

    //Getters and Setters
    public boolean[] getJokers() { return jokers; }

    public void setJokers(boolean[] jokers) { this.jokers = jokers; }

    public String getHostID() { return hostID; }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public String[] getPlayers() {
        return players;
    }

    public void setPlayers(String[] players) {
        this.players = players;
    }

    public int[] getPlayerTeam() {
        return playerTeam;
    }

    public void setPlayerTeam(int[] playerTeam) {
        this.playerTeam = playerTeam;
    }

    public Location[] getLocations() {
        return locations;
    }

    public void setLocations(Location[] locations) {
        this.locations = locations;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String[] getWinners() {
        return winners;
    }

    public void setWinners(String[] winners) {
        this.winners = winners;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    public double getDifficulty() { return difficulty; }

    public void setDifficulty(double difficulty) { this.difficulty = difficulty; }
}
