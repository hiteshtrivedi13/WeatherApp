package com.example.weather.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hitesh on 3/20/16.
 */
public class SearchedData
{
    @SerializedName("name")
    private String name;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getLat()
    {
        return lat;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public double getLng()
    {
        return lng;
    }

    public void setLng(double lng)
    {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SearchedData that = (SearchedData) o;

        if (Double.compare(that.lat, lat) != 0)
            return false;
        if (Double.compare(that.lng, lng) != 0)
            return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
