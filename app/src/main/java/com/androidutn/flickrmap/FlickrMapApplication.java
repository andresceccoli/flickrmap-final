package com.androidutn.flickrmap;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by andres on 12/5/17.
 */

public class FlickrMapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
    }
}
