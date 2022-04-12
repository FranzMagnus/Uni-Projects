package models.communication;

public class UserData implements Sendable{
    private String username;
    private int teamId;

    public UserData() {
    }

    public UserData(String username, int teamId) {
        this.username = username;
        this.teamId = teamId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
}
