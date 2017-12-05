package com.androidutn.flickrmap.api;

import com.androidutn.flickrmap.model.LocationResult;
import com.androidutn.flickrmap.model.SearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by andres on 12/4/17.
 */

public interface FlickrService {

    @GET("rest/?method=flickr.photos.search&radius=1&per_page=30")
    Call<SearchResult> buscarFotos(@Query("lat") String lat, @Query("lon") String lon);

    @GET("rest/?method=flickr.photos.search&radius=1&per_page=30")
    Call<SearchResult> buscarFotosArea(@Query("bbox") String bbox);

    @GET("rest/?method=flickr.photos.geo.getLocation")
    Call<LocationResult> getUbicacion(@Query("photo_id") String id);

}
