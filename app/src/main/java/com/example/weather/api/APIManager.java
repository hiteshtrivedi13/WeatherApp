package com.example.weather.api;

import retrofit.RestAdapter;

/**
 * Created by hitesh on 3/20/16.
 */
public class APIManager
{
    private static final String API_URL_WEATHER = "http://api.openweathermap.org/data/2.5";
    private static RestAdapter restAdapter;

    private static  RestAdapter getRestAdapter(){
        if(restAdapter==null){
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API_URL_WEATHER)
                    .build();
        }
        return restAdapter;
    }

    public static WeatherAPI getWeatherApiInterface(){

        // Create an instance of our  API interface.
        WeatherAPI weatherAPI =null;
        try {
            if(restAdapter==null){
                restAdapter=getRestAdapter();
            }
            weatherAPI = restAdapter.create(WeatherAPI.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherAPI;
    }
}
