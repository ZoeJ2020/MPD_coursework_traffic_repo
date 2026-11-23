package org.me.gcu.johnston_zoe_s2336647.model;

import java.util.ArrayList;

public class Location {
    //    what end this is - either "from" or "to"
    private final String role;

    //    coordinates (used for the map and the distance search)
    private float latitude;
    private float longitude;
    private String placeName;

    //    road names at this location that will be used in road searching
    private final ArrayList<String> roadNames = new ArrayList<>();

    //    constructor
    public Location(String role) {
        this.role = role;
    }

    //    get methods
    public String getRole() {
        return role;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public ArrayList<String> getRoadNames() {
        return roadNames;
    }

//    set methods
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void addRoadName(String roadName) {
        roadNames.add(roadName);
    }

    //    toString for debug logging
    @Override
    public String toString() {
        return "Location{role=" + role + ", latitude=" + latitude + ", longitude=" + longitude
                + ", place=" + placeName + ", roads=" + roadNames + "}";
    }

}