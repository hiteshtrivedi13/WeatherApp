package com.example.weather.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.weather.R;
import com.example.weather.common.Constant;
import com.example.weather.common.Utils;
import com.example.weather.detail.DetailActivity;
import com.example.weather.location.MyLocation;
import com.example.weather.model.SearchedData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MyLocation.MyLocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,SearchListAdapter.OnItemClickListener
{
    private ProgressDialog progressDialog;
    private EditText cityName;
    private GoogleApiClient mGoogleApiClient;
    private ImageView gpsIcon;
    private SearchListAdapter  searchListAdapter;
    private RecyclerView previousSearchList;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        gpsIcon = (ImageView) findViewById(R.id.gpsIcon);
        gpsIcon.setOnClickListener(this);
        cityName = (EditText) findViewById(R.id.cityName);
        findViewById(R.id.btnSearch).setOnClickListener(this);
        previousSearchList = (RecyclerView) findViewById(R.id.previousSearchList);
        previousSearchList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.gpsIcon :
            {
                requestLocationPermission();
                break;
            }
            case R.id.btnSearch:
            {
                if(!cityName.getText().toString().equals(""))
                {
                    openDetailActivity(cityName.getText().toString());
                }
                else
                    Toast.makeText(MainActivity.this, "Please enter valid city name.", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateHistory();
    }

    @Override
    public void gotLocation(Location location)
    {
        if(progressDialog != null)
            progressDialog.dismiss();
        openDetailActivity(location);
    }

    @Override
    public void unableToFindLocation()
    {
        if(progressDialog != null)
            progressDialog.dismiss();
        Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
    }

    private void openDetailActivity(String cityName)
    {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constant.KEY_SEARCH_TYPE, Constant.KEY_SEARCH_TYPE_NAME);
        intent.putExtra(Constant.KEY_LOCATION_NAME, cityName);
        startActivity(intent);
    }

    private void openDetailActivity(Location location)
    {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Constant.KEY_SEARCH_TYPE, Constant.KEY_SEARCH_TYPE_LOCATION);
        intent.putExtra(Constant.KEY_LOCATION_LATLONG, location);
        startActivity(intent);
    }

    private void requestLocationPermission()
    {
        if (Utils.isVersionBelowM()) {
            showLocationDialog();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    new AlertDialog.Builder(this)
                            .setMessage("We need Location permission to auto-detect your current location.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            Constant.REQUEST_CODE_LOCATION_PERMISSION);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create()
                            .show();
                } else
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.REQUEST_CODE_LOCATION_PERMISSION);
            } else {
                showLocationDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode ==  Constant.REQUEST_CODE_LOCATION_PERMISSION) {

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showLocationDialog();
            }
        }
    }

    private void showLocationDialog()
    {

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).
                build();


        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        LocationRequest mLocationRequestHighAccuracy = LocationRequest.create();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy);
        //.addLocationRequest(mLocationRequestBalancedPowerAccuracy);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(LocationSettingsResult result)
            {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //Location is enable find location
                        mGoogleApiClient.disconnect();
                        findLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try
                        {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, Constant.REQUEST_CODE_LOCATION_SETTING);

                        }
                        catch (IntentSender.SendIntentException e)
                        {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Constant.REQUEST_CODE_LOCATION_SETTING)
        {
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
            if(resultCode == RESULT_OK)
                findLocation();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnected(Bundle bundle)
    {
    }
    @Override
    public void onConnectionSuspended(int i)
    {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
    }

    private void findLocation()
    {
        if(progressDialog != null)
            progressDialog.dismiss();
        progressDialog = ProgressDialog.show(MainActivity.this, "Location",
                "Fetching location...", true);
        MyLocation myLocation = new MyLocation();
        myLocation.init(MainActivity.this, MainActivity.this);
    }

    @Override
    public void onItemClick(Location location)
    {
        openDetailActivity(location);
    }

    private void updateHistory()
    {
        SharedPreferences  mPrefs = getSharedPreferences(Constant.KEY_PREFERENCE, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(Constant.KEY_SEARCH_HISTORY, "");
        if(!json.equals(""))
        {
            List<SearchedData> searchedDataList = Arrays.asList(gson.fromJson(json, SearchedData[].class));
            if(searchListAdapter == null)
            {
                searchListAdapter = new SearchListAdapter(this);
                previousSearchList.setAdapter(searchListAdapter);
            }
            searchListAdapter.updateData(searchedDataList);
        }
    }
}
