package com.example.raka.mapsdirection.network;

import com.example.raka.mapsdirection.model.ResponseRoute;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapService {
    @GET("json")
    Call<ResponseRoute> route(
            @Query("origin") String origin,
            @Query("destination")String destination,
            @Query("api_key")String key);

}
