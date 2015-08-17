package com.sutromedia.android.core;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.GestureDetector.OnDoubleTapListener;
import com.google.android.maps.*;

import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.map.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.util.*;
import static com.sutromedia.android.lib.diagnostic.Tag.*;


public class FullMapActivity
    extends BaseActivity
    implements SimpleMapOverlay.ITapNotification {

    private static double ONE_KM = 1000.0;
    private static double ONE_MILE = 1.609344 * 1000;
    
    private static double MAX_DISTANCE_FOR_ESTIMATE_FARE = 300 * ONE_KM;
    private static double MAX_DISTANCE_LOCAL_UNIT_FOR_ESTIMATE_FARE = 200;
    private static double MAX_DISTANCE_FOR_CALLING_CAB = 200 * ONE_MILE;
    
    private static int POP_ARROW_OFFSET = -25;
    

    private static boolean TEST_HAS_TELEPHONY = false;
    private static boolean TEST_HAS_CONNECTIVITY = false;
    private static Double TEST_DEFAULT_MINIMUM_CHARGE = null;
    private static Double TEST_DEFAULT_PRICE_PER_UNIT = null;
    private static String TEST_DEFAULT_TAXI_PHONE = null;
    
    private List<IEntrySummary> mActiveEntries;
    private IEntrySummary       mUniqueEntry;
    private int                 mDefaultZoomFactor;
    private GeoPoint            mCenter;
    private View                mPopup;
    private IEntrySummary       mEntryForPopup;


    class MapInitTask extends AsyncTask<Void, Void, Void> {

	protected void onPreExecute() {
	}

	protected Void doInBackground(final Void... unused) {
	    getApp().getCurrentNavigationEntry().onForceReloadView(FullMapActivity.this);
	    return null;
	}

	protected void onPostExecute(final Void unused) {
	    View progress = findViewById(R.id.wait);
	    progress.setVisibility(View.GONE);
	}
    }

    private NavigationMap getNavigation(Intent intent) {
	NavigationMap navigation = new NavigationMap();
	navigation.unserialize(intent.getExtras());
	Debug.v("Recreated navigation entry for map:" + navigation);
	return navigation;
    }

    private void initializeNavigationStack(Intent intent) {
	getApp().clearNavigationStack();
	getApp().pushNextLocation(getNavigation(intent));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	initializeNavigationStack(getIntent());
	Timer timer = new Timer();
        mUniqueEntry = null;
        readFromDatabase();
        setContentView(R.layout.map);
	MapInitTask init = new MapInitTask();
	init.execute();
    }
    
    @Override
    protected void onPause() {
        removePopup();
        super.onPause();
    }

    public void onNewIntent(Intent intent) {
	Debug.v(this.getClass().getName() + ".onNewIntent");
	initializeNavigationStack(intent);
    }

    void synchronizeWithEntry(INavigationEntry entry) {
	if (entry instanceof NavigationMap) {
	    NavigationMap detail = (NavigationMap)entry;
	    if (mUniqueEntry == null) {
		if (detail.getId() == null) {
		    //Both want "Display all" => nothing to do
		} else {
		    //One is null, the other is not => reload
		    entry.onForceReloadView(this);
		}
	    } else {
		if (mUniqueEntry.equals(detail.getId())) {
		    //Nothing to do => the right entry is already being displayed
		} else {
		    //There is a new entry to display
		    entry.onForceReloadView(this);
		}
	    }
	} else {
	    Debug.v("#################################################");
	    Debug.v("Unexpected type");
	    Debug.printStackTrace();
	}	
    }

    public void onReceiveEntry(INavigationEntry destination) {
	NavigationBase base = (NavigationBase)destination;
	if (!getClass().equals(base.getAcceptedActivity())) {
	    setupNavigationMapData(base);
	    Bundle extras = new Bundle();
	    extras.putBoolean(NavigationBase.KEY_DIRECTION, true);
	    base.serialize(extras);
	    Intent intent = new Intent(this, base.getAcceptedActivity());
	    intent.putExtras(extras);
	    startActivity(intent);
	} else {
	    getApp().navigateNextLocation(this, destination);
	}
    }


    protected boolean isFullMap() {
        return true;
    }

    private void setupNavigationMapData(final NavigationBase navigation) {
        MapView map = (MapView) findViewById(R.id.mapview);
	Bundle mapData = new Bundle();
	map.onSaveInstanceState(mapData);
	navigation.setMapData(mapData);
    }

    private void restoreMapState(final Bundle mapData) {
        MapView map = (MapView) findViewById(R.id.mapview);
	if (mapData != null) {
	    map.onRestoreInstanceState(mapData);
	}
    }

    protected void onMenuCustomize(Menu menu) {
	super.onMenuCustomize(menu);
	menu.removeItem(R.menu.sort);
    }

    
    protected void notifyLocationUpdated(Location location) {
        //New location update => redisplay distance information
        if (location!=null && mPopup!=null && mEntryForPopup!=null) {
            setupPopup(mEntryForPopup);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if ((item.getItemId()==R.menu.maps) && (mUniqueEntry!=null)) {
            loadMarkersForAll(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }    

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	//Debug.v(TAG_PERF, "Back from dialog");	
        switch (requestCode) {
            case CODE_CATEGORIES:
		if (mUniqueEntry == null) {
		    loadMarkersForAll(null);
		}
                break;
        }
    }

    public boolean itemClicked(final int index, final GeoPoint point) {
        removePopup();
        IEntrySummary entry = mActiveEntries.get(index);
        mPopup = setupPopup(entry);
        
        MapView.LayoutParams mapParams = new MapView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, 
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        point,
                        0,
                        POP_ARROW_OFFSET,
                        MapView.LayoutParams.BOTTOM_CENTER);
        
        final MapView map = (MapView) findViewById(R.id.mapview);
        map.addView(mPopup, mapParams);

        final ViewTreeObserver observer = mPopup.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw () {
                Projection projection = map.getProjection();
                android.graphics.Point pixLocation = projection.toPixels(point, null);
                android.graphics.Point popupCenter = new android.graphics.Point(
                    mPopup.getLeft() + mPopup.getWidth()/2,
                    mPopup.getTop() + mPopup.getHeight()/2);
                    
                int xPadding = 10;
                int yPaddingTop = 10;
                int yPaddingBottom = 40;
                int xOffset = computeOffset(map.getWidth(), xPadding, xPadding, mPopup.getWidth(), popupCenter.x);
                int yOffset = computeOffset(map.getHeight(), yPaddingTop, yPaddingBottom, mPopup.getHeight(), popupCenter.y);
                    
                observer.removeOnPreDrawListener(this);
                
                GeoPoint geoPointToCenter = projection.fromPixels(
                    map.getWidth()/2 - xOffset, 
                    map.getHeight()/2 - yOffset);
                map.getController().animateTo(geoPointToCenter);
                return true;
            }
        });
        
        return true;
    }
    
    int computeOffset(int totalLength, int paddingStart, int paddingEnd, int innerLength, int innerCenter) {
        int offset = 0;
        if (innerCenter < totalLength/2) {
            //on the left side
            offset = (innerCenter > (innerLength/2 + paddingStart)) 
                ? 0 
                : paddingStart + innerLength/2 - innerCenter;
        } else {
            offset = (innerCenter < totalLength - innerLength/2 - paddingEnd) 
                ? 0 
                : (totalLength - innerLength/2 - paddingEnd) - innerCenter;
        }
        return offset;
    }

    public boolean mapClickedNothing() {
        removePopup();
        return false;
    }

    public void loadMarkersForAll(final Bundle mapData) {
	getApp().refreshPreferences();
        mUniqueEntry = null;
        setupOverlays(getAllEntries(), mapData);
        MapView map = (MapView) findViewById(R.id.mapview);
	if (mapData==null) {        
	    Debug.v("No MAPDATA => use entry bounding box and default zoom factor");
	    map.getController().setCenter(mCenter);
	    map.getController().setZoom(mDefaultZoomFactor);
	} else {
	    restoreMapState(mapData);
	}
    }

    public void loadMarkersForEntry(final String id, final Bundle mapData) {
        IEntrySummary entry = getEntry(id);
        mUniqueEntry = entry;
        List<IEntrySummary> entries = new ArrayList<IEntrySummary>();
        entries.add(entry);
        setupOverlays(entries, mapData);
	MapView map = (MapView)findViewById(R.id.mapview);
	if (mapData == null) {
	    Debug.v("No MAPDATA => use entry location and default zoom factor");
	    GeoPoint point = makeGeoPoint(entry.getLocation());
	    map.getController().animateTo(point);
	    int newZoomFactor = Math.min(21, mDefaultZoomFactor+5);
	    map.getController().setZoom(newZoomFactor);
	} else {
	    restoreMapState(mapData);
	}
    }
    
    private void readFromDatabase() {
        try {
            mDefaultZoomFactor = getSettings().getIntValue(Settings.Key.MAP_DEFAULT_ZOOM_LEVEL);
            mCenter = makeGeoPoint( 
                getSettings().getDoubleValue(Settings.Key.MAP_CENTER_LATITUDE), 
                getSettings().getDoubleValue(Settings.Key.MAP_CENTER_LONGITUDE));
                
        } catch (Exception error) {
            showErrorAndTerminate("Unable to read/open the database");
        }
    }
    
    private void setupOverlays(final List<IEntrySummary> entries, final Bundle mapData) {
	//Debug.v(TAG_PERF, "begin setup overlay");
	//Debug.v(TAG_PERF, "Loading " + entries.size() + " items");
	Timer timer = new Timer();
        mActiveEntries = new ArrayList<IEntrySummary>();
        MapView map = (MapView)findViewById(R.id.mapview);
        Drawable drawable = this.getResources().getDrawable(R.drawable.simple_map_marker);
        SimpleMapOverlay overlay = new SimpleMapOverlay(drawable, this);                
        for (IEntrySummary entry : entries) {
            if (entry.getLocation()!=null) {
                GeoPoint point = makeGeoPoint(entry.getLocation());
                OverlayItem overlayitem = new OverlayItem(point, "Hi there!", "Some information!");
                overlay.addOverlay(overlayitem);
                mActiveEntries.add(entry);
            } 
        }

        List<Overlay> mapOverlays = map.getOverlays();
        mapOverlays.clear();
        mapOverlays.add(overlay);
	//Debug.v(TAG_PERF, "end setup overlay in " + timer.getElapsed());
    }
    
    private View setupPopup(final IEntrySummary entry) {
        View popup = null;
        try {
            MapView map = (MapView) findViewById(R.id.mapview);
            popup = getLayoutInflater().inflate(R.layout.map_popup, map, false);

            IEntryDetail details = getEntryDetails(entry.getId());
            setText(popup, R.popup.name, entry.getName());
            if (mUniqueEntry!=null) {
                setTextColor(popup, R.popup.name, Color.WHITE);
            }
            
            setText(popup, R.popup.subtitle, details.getSubtitle());
            setText(popup, R.popup.address, details.getAddress());

            Distance distance = entry.getDistance(getGPS());
            String formattedDistance = null;
            if (distance!=null) {
                Distance.Unit unit = getSettings().getDistanceUnits();
                formattedDistance = distance.getFormatted(
                    getResources().getString(R.string.distance_close_full),
                    getResources().getString(R.string.distance_far_full),
                    getSettings().getDistanceUnitName(),
                    100.0,
                    getSettings().getDistanceUnits());
            }
            setText(popup, R.popup.distance, formattedDistance);
            setupTouchEntryName(popup.findViewById(R.popup.entry_group), entry);

            setupDirection(popup, entry, getGPS());
            setupFareEstimate(popup, entry, getGPS());
            
            mEntryForPopup = entry;
        } catch (DataException error) {
            Report.logUnexpectedException("Unable to load map popup for entry [" + entry.getId() + "]", error);
        }
        return popup;
    }
    
    private void setText(View view, int childId, String value) {
        TextView text = (TextView)view.findViewById(childId);
        if (value!=null) {
            text.setText(value);
        } else {
            text.setVisibility(View.GONE);
       }
    }
    
    private void removePopup() {
        MapView map = (MapView) findViewById(R.id.mapview);
        if (mPopup!=null) {
            map.removeView(mPopup);
            mPopup = null;
            mEntryForPopup = null;
        }
    }
        
    private void setupDirection(
        final View popup,
        final IEntrySummary entry,
        final Location location) {
        
        Double distance = entry.getDistanceInMeters(location);
        boolean hasDistance = (distance != null);
        
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean hasConnectivity = TEST_HAS_CONNECTIVITY || (connectivityManager.getActiveNetworkInfo() != null);
        boolean canShowDirections = hasDistance && hasConnectivity;
        boolean canCallTaxi = hasDistance && (distance < 200*ONE_MILE);
        boolean displayDirection = canShowDirections || canCallTaxi;
        
        setVisibleOrGone(popup, R.popup.directions, displayDirection);
        setVisibleOrGone(popup, R.popup.distance, displayDirection);
        if (displayDirection) {
            setupTouchDirections(popup.findViewById(R.popup.directions), entry);
            setupTouchDirections(popup.findViewById(R.popup.distance), entry);
        }
    }

    private void setVisibleOrGone(final View parent, int id, boolean visible) {
        View control = parent.findViewById(id);
        setVisibleOrGone(control, visible);
    }

    private void setVisibleOrGone(final View control, boolean visible) {
        if (control != null) {
            control.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private boolean hasTelephony() {
        TelephonyManager manager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
	return manager != null; 
    }

    private void setupFareEstimate(
        final View control,
        final IEntrySummary entry,
        final Location location) {
        
        Double distance = entry.getDistanceInMeters(location);
        boolean hasDistance = (distance != null);

	boolean canMakeCall = true;
        boolean isCloseEnough = hasDistance && ((distance/1000.0) < (MAX_DISTANCE_FOR_CALLING_CAB));

        Double estimatedFare = getEstimatedFare(entry, location);
        boolean canShowFareEstimate = (estimatedFare != null);
        String formattedFareEstimate = "";
        if (canShowFareEstimate) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            formattedFareEstimate = String.format("%s%s", "$", formatter.format(estimatedFare));
        }
        
        String phone = TEST_DEFAULT_TAXI_PHONE;
        if (phone==null) {
            phone = getApp().getSettings().getValue(Settings.Key.TAXI_SERVICE_PHONE, TEST_DEFAULT_TAXI_PHONE);
        }

        boolean hasTaxiPhone = (phone!=null) && (phone.length()>=10);
        boolean canCallTaxi = isCloseEnough && hasTaxiPhone && canMakeCall;
        
        setVisibleOrGone(control, R.popup.taxi, canShowFareEstimate);        
        setVisibleOrGone(control, R.popup.fare, canShowFareEstimate);
        setText(control, R.popup.fare, formattedFareEstimate);

	/*
        Debug.v(TAG, "Setup Fare estimate :");
        Debug.v(TAG, "Can make call       : " + canMakeCall);
        Debug.v(TAG, "Is close enough     : " + isCloseEnough);
        Debug.v(TAG, "Estimated fare      : " + estimatedFare);
        Debug.v(TAG, "Phone               : " + phone);
        Debug.v(TAG, "Can call taxi       : " + canCallTaxi);
	*/

        if (canCallTaxi) {
            setupTouchCallTaxi(control.findViewById(R.popup.taxi), phone);
            setupTouchCallTaxi(control.findViewById(R.popup.fare), phone);
        }        
    }
    
    private Double getEstimatedFare(
        final IEntrySummary entry,
        final Location location) {
        
        Double distanceMeters = entry.getDistanceInMeters(location);
        Double distanceLocalUnits = null;
        if (distanceMeters!=null && distanceMeters<(MAX_DISTANCE_FOR_ESTIMATE_FARE)) {
            String units = getApp().getSettings().getValue(Settings.Key.DISTANCE_UNITS, "mi");
            distanceLocalUnits = units.equals("mi") ? distanceMeters/ONE_MILE : distanceMeters/ONE_KM;
        }

        Double chargePerDistance = getSettingsValue(Settings.Key.TAXI_SERVICE_CHARGE_PER_DISTANCE, TEST_DEFAULT_PRICE_PER_UNIT);
        Double minimumCharge = getSettingsValue(Settings.Key.TAXI_SERVICE_MINIMUM_CHARGE, TEST_DEFAULT_MINIMUM_CHARGE);
        
        if (chargePerDistance!=null && distanceLocalUnits!=null && distanceLocalUnits<MAX_DISTANCE_LOCAL_UNIT_FOR_ESTIMATE_FARE) {
            return minimumCharge + (distanceLocalUnits*chargePerDistance);
        }
        return null;
    }
    
    
    private Double getSettingsValue(Settings.Key key, Double defaultValue) {
        try {
            return getApp().getSettings().getDoubleValue(key);
        } catch (Exception error) {
        }
        return defaultValue;
    }    
    
    private Double getSettingsValue(Settings.Key key) {
        return getSettingsValue(key, null);
    }
    
    private void setupTouchEntryName(final View control, final IEntrySummary entry) {
        if (mUniqueEntry==null) {
            control.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    View l =  ((View) v.getParent()).findViewById(R.popup.root);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        NavigationDetailWeb navigation = new NavigationDetailWeb(entry.getId());
                        onReceiveEntry(navigation);
                    }
                    return true;
                }
            });
        }
    }

    private void showNoTelephonyWarning(String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	String template = getResources().getString(R.string.error_no_telephony);
	String message = String.format(template, phone);
        builder.setMessage(message);  
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
          public void onClick(DialogInterface dialog, int which) {  
            dialog.cancel();  
	  } 
	});       
        
        builder.create().show();
    }
    
    private void setupTouchDirections(
        final View control,
        final IEntrySummary entry) {

        control.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				View l =  ((View) v.getParent()).findViewById(R.popup.root);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String url = String.format(
                        "http://maps.google.com/maps?daddr=%s,%s", 
                        entry.getLocation().getLatitude(), 
                        entry.getLocation().getLongitude());
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
				}
                return true;
			}
		});
    }    
    
    private void setupTouchCallTaxi(
        final View control,
        final String phone) {
        
        control.setOnTouchListener(new OnTouchListener() {
	    public boolean onTouch(View v, MotionEvent event) {
		View l =  ((View) v.getParent()).findViewById(R.popup.root);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String url = String.format("tel:%s", phone);
		    if (!hasTelephony()) {
			showNoTelephonyWarning(phone);
		    } else {
			makePhoneCall(url, phone);
		    }
		}
                return true;
	    }
	});
    }    
    
}
