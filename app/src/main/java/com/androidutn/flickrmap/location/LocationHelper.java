package com.androidutn.flickrmap.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by andres on 12/4/17.
 */

public class LocationHelper extends LocationCallback implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface LocationListener {
        void onLocation(Location location);
    }

    public static final int REQUEST_RESOLVE_LOCATION_SETTINGS = 2000;
    private static final int REQUEST_LOCATION_PERM = 2001;
    private Activity activity;
    private LocationListener listener;
    private GoogleApiClient mGoogleApiClient;

    private boolean requestingLocationUpdates;
    private boolean locationSettingsOk;
    private boolean locationPermissionsOk;
    private LocationRequest locationRequest;

    public static LocationHelper with(Activity activity, LocationListener listener) {
        LocationHelper locationHelper = new LocationHelper(activity, listener);
        return locationHelper;
    }

    private LocationHelper(Activity activity, LocationListener listener) {
        this.activity = activity;
        this.listener = listener;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERM);
        } else {
            locationPermissionsOk = true;
        }
    }

    public void start() {
        mGoogleApiClient.connect();
    }

    public void stop() {
        if (requestingLocationUpdates) {
            LocationServices.getFusedLocationProviderClient(activity)
                    .removeLocationUpdates(this);
            requestingLocationUpdates = false;
        }
        mGoogleApiClient.disconnect();
    }

    // google api client callbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(activity)
                .checkLocationSettings(new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .build());
        task.addOnCompleteListener(activity, new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);

                    locationSettingsOk = true;
                    startLocationUpdates();
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ((ResolvableApiException) e).startResolutionForResult(activity, REQUEST_RESOLVE_LOCATION_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // location methods/callbacks
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (locationSettingsOk && locationPermissionsOk) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.getFusedLocationProviderClient(activity)
                    .requestLocationUpdates(locationRequest, this, null);
            requestingLocationUpdates = true;
        }
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (listener != null)
            listener.onLocation(locationResult.getLastLocation());
    }

    // activity callbacks
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_RESOLVE_LOCATION_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (states.isLocationUsable()) {
                            locationSettingsOk = true;
                            startLocationUpdates();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERM) {
            boolean granted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                locationPermissionsOk = true;
                startLocationUpdates();
            }
        }
    }
}
