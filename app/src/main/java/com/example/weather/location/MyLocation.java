package com.example.weather.location;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by hitesh on 3/18/16.
 */
public class MyLocation  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener
{

        private Location mLastLocation;
        // Google client to interact with Google API
        private GoogleApiClient mGoogleApiClient;
        private LocationRequest mLocationRequest;

        // Location updates intervals in sec
        private static int UPDATE_INTERVAL = 5000; // 10 sec
        private static int FATEST_INTERVAL = 5000; // 5 sec
        private static int DISPLACEMENT = 0; // 10 meters
        private Intent i;
        private Object lockObject = new Object();
        private Thread locationUpdateThread;

        private static final long ONE_MIN = 1000 * 60;
        private static final long TWO_MIN = ONE_MIN * 2;
        private static final long FIVE_MIN = ONE_MIN * 5;
        private static final float MIN_ACCURACY = 25.0f;
        private static final float MIN_LAST_READ_ACCURACY = 200.0f;
        private boolean isSendLocation = false;
        private Context context;
        private MyLocationListener myLocationListener;

        public MyLocation()
        {

        }

        public void init(Context context, MyLocationListener myLocationListener)
        {
                this.context = context;
                this.myLocationListener = myLocationListener;
                buildGoogleApiClient();
                createLocationRequest();
                if (mGoogleApiClient != null && !mGoogleApiClient.isConnected())
                {
                        mGoogleApiClient.connect();
                }

        }

        /**
         * Creating google api client object
         * */
        protected synchronized void buildGoogleApiClient() {
                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(com.google.android.gms.location.LocationServices.API).build();
        }

        /**
         * Creating location request object
         * */
        protected void createLocationRequest() {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FATEST_INTERVAL);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        }

        @Override
        public void onConnected(Bundle bundle)
        {
                Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mCurrentLocation != null)
                {
                        if(myLocationListener != null)
                                myLocationListener.gotLocation(mCurrentLocation);
                        stopMyLocation();
                }
                else
                {
                        try
                        {
                                LocationServices.FusedLocationApi.requestLocationUpdates(
                                        mGoogleApiClient, mLocationRequest, this);
                        }catch (Exception e)
                        {
                                e.printStackTrace();
                        }
                }
        }

        @Override
        public void onConnectionSuspended(int i)
        {
                mGoogleApiClient.connect();
        }

        @Override
        public void onLocationChanged(Location location)
        {
                if (location != null)
                {
                        if(myLocationListener != null)
                                myLocationListener.gotLocation(location);
                        stopMyLocation();
                }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult)
        {
                if(myLocationListener != null)
                        myLocationListener.unableToFindLocation();
                stopMyLocation();
        }

        private void stopMyLocation()
        {
                try
                {
                        myLocationListener = null;
                        if (mGoogleApiClient.isConnected())
                                mGoogleApiClient.disconnect();
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

        public interface MyLocationListener
        {
                void gotLocation(Location location);
                void unableToFindLocation();
        }
}
