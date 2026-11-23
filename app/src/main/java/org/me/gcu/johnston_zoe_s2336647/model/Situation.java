package org.me.gcu.johnston_zoe_s2336647.model;

import java.util.ArrayList;

public class Situation {
    private final String id;
    private final String category;

    //    type (situationRecord by splitting on xsi:type - both raw and formatted for display)
    private String formattedType;
    private String rawType;

    private String description;

    private String createdTime;
    private String updatedTime;
    private String startTime;
    private String endTime;

    private String direction;

    private final Impact impact = new Impact();

    private final ArrayList<Location> locations = new ArrayList<>();
    private Float distanceKm = null;

    //    constructor
    public Situation(String id, String category) {
        this.id = id;
        this.category = category;

    }

    ;

    //    get methods
    public String getId() {
        return id;
    }
    public String getCategory() {
        return category;
    }

    public String getFormattedType() {
        return formattedType;
    }

    public String getRawType() {
        return rawType;
    }

    public String getDescription() {
        return description;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDirection() {
        return direction;
    }

    public Impact getImpact() {
        return impact;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public Float getDistanceKm() {
        return distanceKm;
    }

//    set methods

    public void setFormattedType(String formattedType) {
        this.formattedType = formattedType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setDistanceKm(Float distanceKm) {
        this.distanceKm = distanceKm;
    }

    public void addLocation(Location location) {
        locations.add(location);
    }

    //    toString for debug logging
    @Override
    public String toString() {
        return "Situation{id=" + id + ", category=" + category + ", type=" + formattedType
                + ", description=" + description + ", start=" + startTime + ", end=" + endTime
                + ", direction=" + direction + ", impact=" + impact
                + ", locations=" + locations + "}";
    }

}