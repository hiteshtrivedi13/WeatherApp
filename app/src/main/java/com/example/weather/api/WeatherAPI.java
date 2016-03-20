package com.example.weather.api;

import com.example.weather.model.WeatherData;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by hitesh on 3/20/16.
 */
public interface WeatherAPI
{
    @GET("/weather")
    void getWeatherDetailWithCityName(@Query("q") String city,@Query("appid") String appId, Callback<WeatherData> response);

    @GET("/weather")
    void getWeatherDetailWithLatLong(@Query("lon") double longitude, @Query("lat") double latitude, @Query("appid") String appId ,Callback<WeatherData> response);
}
