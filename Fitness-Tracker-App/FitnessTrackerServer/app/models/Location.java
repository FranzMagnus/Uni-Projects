package models;

public class Location {
    public double lat;
    public double lon;

    // Constructors
    public Location(double Lat, double Lon){
        this.lat = Lat;
        this.lon = Lon;
    }
    //Dummy
    public Location(){

    }

    //Getters and Setters
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
