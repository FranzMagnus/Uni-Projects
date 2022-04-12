package models.communication;

public class Done implements Sendable {
    private String firebaseID;
    private int countDone;

    public Done() {
    }

    public Done(String firebaseID, int countDone) {
        this.firebaseID = firebaseID;
        this.countDone = countDone;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public int getCountDone() {
        return countDone;
    }

    public void setCountDone(int countDone) {
        this.countDone = countDone;
    }
}
