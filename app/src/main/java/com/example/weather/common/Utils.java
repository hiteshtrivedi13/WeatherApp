package com.example.weather.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Created by hitesh on 3/20/16.
 */
public class Utils
{
    public static boolean isNetworkAvailable(Context context) {
        boolean isNetworkAvailable = false;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            isNetworkAvailable = true;
        }
        return isNetworkAvailable;
    }

    public static boolean isVersionBelowM() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }
}
