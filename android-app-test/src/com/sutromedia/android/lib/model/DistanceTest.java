package com.sutromedia.android.lib.db;

import android.location.Location;
import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class DistanceTest extends AndroidTestCase {

    private Location make(double latitude, double longitude) {
        String provider = null;
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public void testShouldSucceedWithNullValue() {
        Distance distance = new Distance(null, null);
        assertFalse(distance.hasDistance());
        
        distance = new Distance(make(0,0), null);
        assertFalse(distance.hasDistance());
        
        distance = new Distance(null, make(0,0));
        assertFalse(distance.hasDistance());
    }
    
    public void testShouldFormatCorrectlyForZeroDistance() {
        Distance distance = new Distance(make(0,0), make(0,0));
        assertTrue(distance.hasDistance());
        assertEquals(0.0, distance.getDistance());
        assertEquals(0.0, distance.getDistance(Distance.Unit.Mile));
        assertEquals(0.0, distance.getDistance(Distance.Unit.Kilometer));
        assertEquals("0.0 mi", distance.getFormatted("%1$.1f %2$s", null, "mi", 1.0, Distance.Unit.Mile));
        assertEquals("0.0 km", distance.getFormatted("%1$.1f %2$s", null, "km", 1.0, Distance.Unit.Kilometer));
        assertEquals("0 mi", distance.getFormatted(null, "%1$.0f %2$s", "mi", -1.0, Distance.Unit.Mile));
        assertEquals("0 km", distance.getFormatted(null, "%1$.0f %2$s", "km", -1.0, Distance.Unit.Kilometer));
    }    
}