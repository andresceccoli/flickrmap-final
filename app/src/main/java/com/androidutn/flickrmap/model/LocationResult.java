package com.androidutn.flickrmap.model;

/**
 * Created by andres on 12/4/17.
 */

public class LocationResult {

    private PhotoLocation photo;
    private String stat;

    public PhotoLocation getPhoto() {
        return photo;
    }

    public void setPhoto(PhotoLocation photo) {
        this.photo = photo;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
}
