package models.communication;

public class Winner implements Sendable {
    private int teamId;

    public Winner() {
    }

    public Winner(int teamId) {
        this.teamId = teamId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
