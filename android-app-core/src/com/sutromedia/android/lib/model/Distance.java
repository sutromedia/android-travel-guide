package com.sutromedia.android.lib.model;

import android.location.Location;

public class Distance {

    private final static double MILES_PER_METER = 0.000621371192;

    public enum Unit {
        Meter,
        Kilometer,
        Mile
    }

    double mDistance;
    
    public Distance(Location currentLocation, Location otherLocation) {
        mDistance = -1.0;
        if (currentLocation != null && otherLocation!=null) {
            mDistance = currentLocation.distanceTo(otherLocation);
        }
    }
    
    public boolean hasDistance() {
        return mDistance >= 0;
    }
    
    public double getDistance() {
        return hasDistance() ? mDistance : 0.0;
    }
    
    public double getDistance(Unit unit) {
        double distance = getDistance();
        switch (unit) {
            case Meter:
                break;
            case Kilometer:
                distance = distance /1000.0;
                break;
            case Mile:
                distance = MILES_PER_METER * distance;
                break;
        }
        return distance;
    }
    
    public String getFormatted(
        String formatStringClose,
        String formatStringFar,
        String unitString,
        double cutoff,
        Unit unit) {
        
        if (hasDistance()) {
            String formatString = getDistance(unit) < cutoff ? formatStringClose : formatStringFar;
            return String.format(formatString, getDistance(unit), unitString);
        }
        return null;
    }
}