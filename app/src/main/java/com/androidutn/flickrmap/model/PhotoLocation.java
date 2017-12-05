package com.androidutn.flickrmap.model;

/**
 * Created by andres on 12/4/17.
 */

public class PhotoLocation {

    private String id;
    private Location location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
