package com.androidutn.flickrmap.model;

import java.util.Locale;

/**
 * Created by andres on 12/4/17.
 */

public class Photo {

    private String id;
    private String owner;
    private String secret;
    private String server;
    private int farm;
    private String title;

    public String buildPhotoUrl() {
        return String.format(Locale.getDefault(),
                "https://farm%d.staticflickr.com/%s/%s_%s_z.jpg",
                farm, server, id, secret);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getFarm() {
        return farm;
    }

    public void setFarm(int farm) {
        this.farm = farm;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
