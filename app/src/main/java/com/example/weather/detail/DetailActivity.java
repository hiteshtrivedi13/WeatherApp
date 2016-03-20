package com.example.weather.detail;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.R;
import com.example.weather.api.APIManager;
import com.example.weather.api.WeatherAPI;
import com.example.weather.common.Constant;
import com.example.weather.common.Utils;
import com.example.weather.model.SearchedData;
import com.example.weather.model.WeatherData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by hitesh on 3/18/16.
 */
public class DetailActivity extends AppCompatActivity
{
    private ProgressBar progressBar;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mainLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if(getIntent() != null && getIntent().getExtras() != null)
        {
            String type = "";
            if(getIntent().getExtras().containsKey(Constant.KEY_SEARCH_TYPE))
                type = getIntent().getExtras().getString(Constant.KEY_SEARCH_TYPE);

            WeatherAPI weatherAPI = APIManager.getWeatherApiInterface();

            if(!Utils.isNetworkAvailable(this))
                Toast.makeText(DetailActivity.this, "No Internet connection.", Toast.LENGTH_SHORT).show();
            else if(type != null)
            {
                progressBar.setVisibility(View.VISIBLE);
                switch (type)
                {
                    case Constant.KEY_SEARCH_TYPE_NAME:
                        weatherAPI.getWeatherDetailWithCityName(getIntent().getExtras().getString(Constant.KEY_LOCATION_NAME),Constant.KEY_APP_ID, weatherDataCallback);
                        break;
                    case Constant.KEY_SEARCH_TYPE_LOCATION:
                        Location location = getIntent().getExtras().getParcelable(Constant.KEY_LOCATION_LATLONG);
                        if (location != null) weatherAPI.getWeatherDetailWithLatLong(location.getLongitude(), location.getLatitude(),Constant.KEY_APP_ID,  weatherDataCallback);
                        break;
                }
            }
        }
    }

    private Callback<WeatherData> weatherDataCallback = new Callback<WeatherData>() {
        @Override
        public void success(WeatherData weatherData, Response response)
        {
            mainLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            initView(weatherData);
            updateHistory(weatherData);
        }

        @Override
        public void failure(RetrofitError error)
        {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(DetailActivity.this, "Some error occurs.", Toast.LENGTH_SHORT).show();
        }
    };

    private void initView(WeatherData weatherData)
    {
        if(weatherData != null)
        {
            TextView city = (TextView) findViewById(R.id.city);
            TextView description = (TextView) findViewById(R.id.description);
            TextView pressure = (TextView) findViewById(R.id.pressure);

            if(weatherData.getName() != null && weatherData.getSys() != null)
                city.setText(weatherData.getName() + ", " + weatherData.getSys().getCountry());


            StringBuilder stringBuilder = new StringBuilder("");

            if(weatherData.getWeather() != null && weatherData.getWeather().size() > 0)
                description.setText(weatherData.getWeather().get(0).getDescription().toUpperCase());

            if(weatherData.getMain() != null)
            {
                stringBuilder.append("\nHumidity : ").append(weatherData.getMain().getHumidity()).append("%");
                stringBuilder.append("\nPressure : ").append(weatherData.getMain().getPressure()).append(" hPa");
                stringBuilder.append("\nCurrent Temp : ").append(kelvinToCelsius(weatherData.getMain().getTemp())).append(" ℃");
                stringBuilder.append("\nMin Temp : ").append(kelvinToCelsius(weatherData.getMain().getTempMin())).append(" ℃");
                stringBuilder.append("\nMax Temp : ").append(kelvinToCelsius(weatherData.getMain().getTempMax())).append(" ℃");
            }

            pressure.setText(stringBuilder);
        }
    }

    private int kelvinToCelsius(double temp)
    {
        return (int) (temp - 273.0);
    }

    private void updateHistory(WeatherData weatherData)
    {
        SearchedData searchedData = new SearchedData();
        searchedData.setName(weatherData.getName());
        searchedData.setLat(weatherData.getCoord().getLat());
        searchedData.setLng(weatherData.getCoord().getLon());

        SharedPreferences mPrefs = getSharedPreferences(Constant.KEY_PREFERENCE, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(Constant.KEY_SEARCH_HISTORY, "");
        List<SearchedData> searchedDataList = null;
        if(!json.equals(""))
        {
             searchedDataList = new ArrayList<>(Arrays.asList(gson.fromJson(json, SearchedData[].class)));
        }

        if(searchedDataList == null)
            searchedDataList = new ArrayList<>();

        if(searchedDataList.contains(searchedData))
            searchedDataList.remove(searchedData);

        searchedDataList.add(0, searchedData);

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        json = gson.toJson(searchedDataList);
        prefsEditor.putString(Constant.KEY_SEARCH_HISTORY, json);
        prefsEditor.commit();
    }

}
