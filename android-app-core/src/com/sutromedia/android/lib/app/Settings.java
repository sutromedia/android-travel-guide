package com.sutromedia.android.lib.app;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.HashMap;

import com.sutromedia.android.lib.model.CurrencyAmount;
import com.sutromedia.android.lib.model.DataException;
import com.sutromedia.android.lib.model.Distance;
import com.sutromedia.android.lib.model.PhotoSize;

public final class Settings {

    public enum Key {
        ABSTRACT_PRICE_SYMBOL,
        ADMIN_SUFFIX,
        APP_ID,
        APP_NAME,
        APP_SHORT_NAME,
        APP_URL,
        AUTHORS,
        BUILD_SYNC_LEVEL,
        BUNDLE_VERSION,
        COMMENT_VERSION,
        CURRENCY_STRING,
        DEFAULT_MAP_TYPE,
        DEFAULT_SORT_OPTION,
        DISTANCE_UNITS,
        FLURRY_ID,
        HAS_ABSTRACT_PRICES,
        HAS_COMMENTS,
        HAS_LOCATIONS,
        HAS_PRICES,
        HAS_SPATIAL_GROUPS,
        IPAD_SCREENSHOT_ORDERING,
        IPHONE_SCREENSHOT_ORDERING,
        MAPS_NEED_UPDATING,
        MAP_CENTER_LATITUDE,
        MAP_CENTER_LONGITUDE,
        MAP_DEFAULT_ZOOM_LEVEL,
        MAP_INNERMOST_ZOOM_LEVEL,
        MAP_LATITUDE_DELTA,
        MAP_LATITUDE_MAXIMUM,
        MAP_LATITUDE_MINIMUM,
        MAP_LONGITUDE_DELTA,
        MAP_LONGITUDE_MAXIMUM,
        MAP_LONGITUDE_MINIMUM,
        MAP_OUTERMOST_ZOOM_LEVEL,
        PINCH_ID,
        REVIEW_URL,
        SLIDESHOW_DELAY,
        SPATIAL_GROUPS_NAME_PLURAL,
        SPATIAL_GROUPS_NAME_SINGULAR,
        SUTRO_ENTRY_HTML,
        SVN_REVISION,
        SYNC_TIME,
        TAXI_SERVICE_CHARGE_PER_DISTANCE,
        TAXI_SERVICE_MINIMUM_CHARGE,
	TAXI_SERVICE_PHONE,
	TOP_LEVEL_INTRO_ENTRY_ID
    };
     
    private HashMap<String, String>   m_map;
   
    public Settings(HashMap<String, String> values) {
        m_map = values;
    }
    
    public String getValue(Key name, String defaultValue) {
        String value = getValueforKey(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public boolean getBooleanValue(Key name, boolean defaultValue) {
	boolean value = defaultValue;
        String str = getValueforKey(name);
	if (str != null) {
	    value = str.equals("1");
	}
        return value;
    }
    
    public String getValue(Key name) throws DataException {
        String value = getValueforKey(name);
        if (value == null) {
            throw new DataException("Unknown Application setting [" + name + "]");
        }
        return value.toString();
    }
    
    public boolean hasValue(Key name) {
        String value = getValueforKey(name);
        return (value != null);
    }
    
    public int getIntValue(Key name) throws DataException {
        return Integer.valueOf(getValue(name));
    }
     
    public double getDoubleValue(Key name) throws DataException {
        return Double.valueOf(getValue(name));
    }

    public String formatCurrency(
        CurrencyAmount price,
        String free, 
        String notFree) {
 
	boolean hasAbstractPrice = getBooleanValue(Key.HAS_ABSTRACT_PRICES, false);
        if (price != null) {
	    if (hasAbstractPrice) {
		String currency = getValue(Key.ABSTRACT_PRICE_SYMBOL, "$");
		return price.getFormattedAbstract(free, currency);
	    } else {
		String currency = getValue(Key.CURRENCY_STRING, "$");
		return price.getFormatted(free, notFree, currency);
	    }
        } else {
            return null;
        }
    }    
    
    public String getValueforKey(Key name) {
        String key = name.toString().toLowerCase();
        String value = m_map.get(key);
        return value;
    }
    
    public Distance.Unit getDistanceUnits() {
        String units = getValue(Key.DISTANCE_UNITS, "mi");
        return units.equals("mi") ? Distance.Unit.Mile : Distance.Unit.Kilometer;
    }
    
    public String getDistanceUnitName() {
        switch (getDistanceUnits()) {
            case Mile:
                return "mi";
            case Kilometer:
                return "km";
            default:
                return "mi";
        }
    }
    
    public int getBestPictureSize(Context context) {
        int size = PhotoSize.MEDIUM;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = Math.min(metrics.heightPixels, metrics.widthPixels);
        if (width>=PhotoSize.CUTOFF_MEDIUM) {
            size = PhotoSize.LARGE;
        }
        return size;
    }   

    public boolean isSortEnabled() {
	boolean hasPrices = getBooleanValue(Key.HAS_PRICES, false);
	boolean hasLocation = getBooleanValue(Key.HAS_LOCATIONS, false);
	boolean hasSpatialGroup = getBooleanValue(Key.HAS_SPATIAL_GROUPS, false);

	return hasPrices || hasLocation || hasSpatialGroup;
    }
}