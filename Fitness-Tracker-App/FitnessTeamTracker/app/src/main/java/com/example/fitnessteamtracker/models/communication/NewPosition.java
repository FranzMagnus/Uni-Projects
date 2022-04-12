package com.example.fitnessteamtracker.models.communication;

public class NewPosition implements Sendable {
    private int newPosId;
    private int teamId;

    public NewPosition(int newPosId, int teamId) {
        this.newPosId = newPosId;
        this.teamId = teamId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getNewPosId() {
        return newPosId;
    }

    public void setNewPosId(int newPosId) {
        this.newPosId = newPosId;
    }

    public NewPosition() {
    }
}
