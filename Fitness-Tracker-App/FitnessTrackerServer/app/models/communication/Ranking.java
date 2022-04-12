package models.communication;

public class Ranking implements Sendable {
    private int[] turnsPerTeam;

    public int[] getTurnsPerTeam() {
        return turnsPerTeam;
    }

    public void setTurnsPerTeam(int[] turnsPerTeam) {
        this.turnsPerTeam = turnsPerTeam;
    }

    public Ranking() {
    }

    public Ranking(int[] turnsPerTeam) {
        this.turnsPerTeam = turnsPerTeam;
    }
}
