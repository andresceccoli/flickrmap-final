package com.androidutn.flickrmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.androidutn.flickrmap.api.Api;
import com.androidutn.flickrmap.location.LocationHelper;
import com.androidutn.flickrmap.model.Location;
import com.androidutn.flickrmap.model.LocationResult;
import com.androidutn.flickrmap.model.Photo;
import com.androidutn.flickrmap.model.SearchResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusquedaActivity extends FragmentActivity implements OnMapReadyCallback, LocationHelper.LocationListener {

    private GoogleMap mMap;
    private List<Photo> photos;
    private List<Marker> markers = new ArrayList<>();
    private Map<String, String> photosByMarkerId;
    private Map<String, Photo> photosById;
    private LocationHelper locationHelper;
    private boolean buscandoResultados;
    private Button buscar;
    private android.location.Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busqueda);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buscar = findViewById(R.id.buscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buscar();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationHelper != null) {
            locationHelper.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationHelper != null) {
            locationHelper.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String photoId = photosByMarkerId.get(marker.getId());
                Photo photo = photosById.get(photoId);
                if (photo != null) {
                    new ImageViewer.Builder<String>(BusquedaActivity.this, Arrays.asList(photo.buildPhotoUrl()))
                            .setStartPosition(0)
                            .show();
                }
                return true;
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                buscar.setVisibility(View.VISIBLE);
            }
        });

        locationHelper = LocationHelper.with(this, this);
        locationHelper.start();

        mostrarDatos();
    }

    private void mostrarDatos() {
        if (mMap != null && photos != null) {
            Callback<LocationResult> callback = new Callback<LocationResult>() {
                @Override
                public void onResponse(Call<LocationResult> call, Response<LocationResult> response) {
                    LocationResult result = response.body();
                    Location location = result.getPhoto().getLocation();
                    agregarMarker(result.getPhoto().getId(), location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onFailure(Call<LocationResult> call, Throwable t) {

                }
            };

            markers.clear();
            photosByMarkerId = new HashMap<>();

            for (Photo photo : photos) {
                Api.getFlickrService().getUbicacion(photo.getId()).enqueue(callback);
            }
        }
    }

    private void agregarMarker(String id, double latitude, double longitude) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
        photosByMarkerId.put(marker.getId(), id);
        markers.add(marker);
    }

    @Override
    public void onLocation(android.location.Location location) {
        if (photos == null && !buscandoResultados) {
            this.location = location;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            buscar();
        }
    }

    private void buscar() {
        for (Marker marker : markers) {
            marker.remove();
        }

        buscar.setVisibility(View.GONE);

        buscandoResultados = true;
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        Api.getFlickrService().buscarFotosArea(
                "" + bounds.southwest.longitude + "," +
                    bounds.southwest.latitude + "," +
                    bounds.northeast.longitude + "," +
                    bounds.northeast.latitude)
                .enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                        if (response.isSuccessful()) {
                            SearchResult result = response.body();
                            if (result.getPhotos() != null) {
                                photos = result.getPhotos().getPhoto();
                                photosById = new HashMap<>();
                                for (Photo photo : photos) {
                                    photosById.put(photo.getId(), photo);
                                }
                                mostrarDatos();
                            }
                        }

                        buscandoResultados = false;
                    }

                    @Override
                    public void onFailure(Call<SearchResult> call, Throwable t) {

                    }
                });
    }
}
