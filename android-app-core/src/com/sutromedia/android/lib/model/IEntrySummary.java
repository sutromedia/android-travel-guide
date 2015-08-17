package com.sutromedia.android.lib.model;

import android.location.Location;

public interface IEntrySummary {

    String getId();
    String getName();
    String getGroup();
    CurrencyAmount getPrice();
    String getIconPhoto();
    Distance getDistance(Location location);
    //String getDistance(Location location);
    Double getDistanceInMeters(Location location);    
    Double getDistanceInMiles(Location location);
    Location getLocation();
}