package com.example.raka.mapsdirection.network;

import com.example.raka.mapsdirection.BuildConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigRetrofit {
    public static Retrofit setInit(){
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.URL_MAP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public static MapService getInstance(){
        return setInit().create(MapService.class);
    }
}
