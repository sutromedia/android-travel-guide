package com.sutromedia.android.lib.model;

import android.location.Location;
import java.text.NumberFormat;
import java.util.Locale;

public class EntrySummary extends ModelBase implements IEntrySummary {

    //private final static double MILES_PER_METER = 0.000621371192;

    private String          mId;
    private String          mName;
    private String          mGroup;
    private CurrencyAmount  mPrice;
    private String          mIconPhoto;
    private Location        mLocation;
    
    public EntrySummary(
        String id, 
        String name, 
        String group, 
        String price, 
        String icon) throws DataException {

        mId = ensureNotEmpty(id, "Entry.id is required");
        mName = ensureNotEmpty(name, "Entry.name is required");
        mGroup = ensureNullIfEmpty(group);
        mPrice = new CurrencyAmount(price);
        mIconPhoto = ensureNullIfEmpty(icon);
    }

    public String getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getGroup() {
        return mGroup;
    }
    
    public CurrencyAmount getPrice() {
        return mPrice;
    }
    
    public String getIconPhoto() {
        return mIconPhoto;
    }

    public Location getLocation() {
        return (mLocation != null) ? new Location(mLocation) : null;
    }

    public Double getDistanceInMeters(Location location) {
        Distance distance = new Distance(location, mLocation);
        if (distance.hasDistance()) {
            return distance.getDistance(Distance.Unit.Meter);
        }
        return null;
    }
    
    public Double getDistanceInMiles(Location location) {
        Distance distance = new Distance(location, mLocation);
        if (distance.hasDistance()) {
            return distance.getDistance(Distance.Unit.Mile);
        }
        return null;
    }
    
    public Distance getDistance(Location location) {
        Distance distance = new Distance(location, mLocation);
        return distance.hasDistance() ? distance : null;
    }
    
    /*
    public String getDistance(Location location) {
        Double distance = getDistanceInMiles(location);
        if (distance != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            if (distance>10.0) {
                formatter.setMinimumFractionDigits(0);
                formatter.setMaximumFractionDigits(0);
            } else if (distance>1.0){
                formatter.setMinimumFractionDigits(1);
                formatter.setMaximumFractionDigits(1);
            } else {
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
            }
            return String.format("%s %s", formatter.format(distance), "mi");
        }
        return null;
    }
    */
    
    public void setGpsPosition(String strLatitude, String strLongitude) {
        Double longitude = tryGetDouble(strLongitude);
        Double latitude = tryGetDouble(strLatitude);
	boolean hasLocation = (longitude != null && latitude != null);
	if (hasLocation && (longitude==0.0) && (latitude==0.0)) {
	    hasLocation = false;
	}
        if (hasLocation) {
            String locationProvider = null;
            mLocation = new Location(locationProvider);
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
        }
    }

    private Double tryGetDouble(String value) {
        try {
            if (value!=null) {
                return Double.valueOf(value);
            }
        } catch (NumberFormatException error) {
        }
        return null;
    }
}