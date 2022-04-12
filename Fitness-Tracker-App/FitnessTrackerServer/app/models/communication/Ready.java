package models.communication;

public class Ready implements Sendable{
    private String firebaseID;

    public Ready() {
    }

    public Ready(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }
}
