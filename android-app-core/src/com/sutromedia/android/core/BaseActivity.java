package com.sutromedia.android.core;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import com.google.android.maps.*;

import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.model.*;

public class BaseActivity extends MapActivity 
    implements INavigationReceiver, LocationListener {

    public static final int CODE_CATEGORIES = 0x100;
    public static final int CODE_SORT = 0x101;
    public static final int CODE_PREFERENCE = 0x102;

    private LocationManager mLocationManager;

    protected MainApp getApp() {
        return (MainApp)getApplication();
    }
    
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    protected boolean isFullMap() {
        return false;
    }

    private void handleNavigateFromFullMap(Intent intent) {
        if (!isFullMap()) {
            if (intent.getExtras()!=null) {
                Bundle extras = intent.getExtras();
                boolean forward = extras.getBoolean(NavigationBase.KEY_DIRECTION);
                if (forward) {
                    try {
                        Debug.v(this.getClass().getName() + ".handleNavigateFromFullMap: moving forward from the map");
                        String klassName = extras.getString(NavigationBase.KEY_CLASS_NAME);
                        NavigationBase klass = (NavigationBase)Class.forName(klassName).newInstance();
                        klass.unserialize(extras);
                        Bundle mapData = klass.getMapData();
                        Debug.v("Looking for map data: + " + mapData);
                        getApp().setMapDataForCurrentLocation(mapData);
                        getApp().pushNextLocation(klass);
                    } catch (ClassNotFoundException error) {
                        error.printStackTrace();
                    } catch (IllegalAccessException error) {
                        error.printStackTrace();
                    } catch (InstantiationException error) {
                        error.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void handleReturnFromMapActivity() {
        if (!isFullMap()) {
            int skipped = 0;
            while (getApp().hasNavigationEntry()) {
                NavigationBase current = (NavigationBase)getApp().getCurrentNavigationEntry();
                if (!current.getAcceptedActivity().equals(this.getClass())) {
                    Debug.v(this.getClass().getName() + ".handleReturnFromMapActivity: returning from the map");
                    //We are not identical to the current entry in the history stack
                    // => we are back from the Map view => just pop the history
                    if (skipped>0) {
                        Report.v(String.format(
                            "PANIC:\n    activity [%s]\n    navigation [%s]\n    don't match => keep looking back",
                            this.getClass().getName(),
                            current.toString()));
                    }
                    getApp().popCurrentLocation();
                } else {
                    break;
                }
                skipped += 1;
                if (getApp().hasNavigationEntry()) {
                    //Something is really wrong: we don't have any navigation entries anymore
                    getApp().enterAppGotoMainScreen(this);		    
                }		
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        Debug.v(this.getClass().getName() + ".onCreate");
        super.onCreate(savedInstance);
        getApp().displayNavigationStack();
        
        handleNavigateFromFullMap(getIntent());
        handleReturnFromMapActivity();
        
        getApp().setLocation(getGPS());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            getApp().ensureDatabase();
        } catch (Exception error) {
            getApp().submitUnexpectedException(error);
            showErrorAndTerminate("Unable to read/open the database");
        }
    }


    @Override
    public void onResume() {
        Debug.v(this.getClass().getName() + ".onResume");
        super.onResume();
        handleReturnFromMapActivity();
        startListening();
    }
    
    void synchronizeWithEntry(INavigationEntry entry) {
        Debug.v("####################################################################");
        Debug.v(this.getClass().getName() + ".synchronizeWithEntry is not implemented");
    }

    public void onNewIntent(Intent intent) {
        Debug.v(this.getClass().getName() + ".onNewIntent");
        super.onNewIntent(intent);
        handleNavigateFromFullMap(intent);
        handleReturnFromMapActivity();
        if (getApp().hasNavigationEntry()) {
            getApp().getCurrentNavigationEntry().onSynchronizeWithEntry(this);
        }
    }

    @Override
    protected void onPause() {
        Debug.v(this.getClass().getName() + ".onPause");
        stopListening();
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        Debug.v(this.getClass().getName() + ".onDestroy");
        stopListening();
        super.onDestroy();
    }    
    
    void makePhoneCall(final String phone, final String formatted) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Call " + formatted + " ?");  
        builder.setPositiveButton("Call now", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
                Intent callIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(phone)); 
                startActivity(callIntent);
            } 
            });       
        
        builder.setNegativeButton("Cancel call", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.cancel();  
            } 
        });       
        builder.create().show();
    }

    private void startListening() {
        try {
            mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 
                1000 * 10, 
                5, 
                this);
        } catch (Exception error) {
            //Silently fail
        }
    }

    private void stopListening() {
        try {
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(this);
                mLocationManager = null;
            }
        } catch (Exception error) {
            //Silently fail
        }
    }

    public void onReceiveEntry(INavigationEntry destination) {
        getApp().navigateNextLocation(this, destination);
    }
    
    public void onBackPressed() {
        Debug.v(this.getClass().getName() + ".onBackPressed()");
        getApp().displayNavigationStack();
        getApp().navigateBack(this);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        onMenuCustomize(menu);
        return true;
    }    

    protected void onMenuCustomize(Menu menu) {
        boolean hasMaps = getSettings().getBooleanValue(Settings.Key.HAS_LOCATIONS, false);
        if (!hasMaps) {
            menu.removeItem(R.menu.maps);
        }

        if (!useComments()) {
            menu.removeItem(R.menu.comment);
        }

        if (!getSettings().isSortEnabled()) {
            menu.removeItem(R.menu.sort);
        }
    }

    boolean useComments() {
        return getSettings().getBooleanValue(Settings.Key.HAS_COMMENTS, false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        
        if (item.getItemId() == R.menu.browse) {
            getApp().enterAppGotoMainScreen(this);
            return true;
        } else if (item.getItemId() == R.menu.photos) {
            onReceiveEntry(new NavigationPhoto());
            return true;
        } else if (item.getItemId() == R.menu.maps) {
            onReceiveEntry(new NavigationMap());
            return true;
        } else if (item.getItemId() == R.menu.category) {
            startActivityForResult (new Intent(this, CategoryListActivity.class), CODE_CATEGORIES);
            return true;
        } else if (item.getItemId() == R.menu.sort) {
            startActivityForResult (new Intent(this, SortSettingActivity.class), CODE_SORT);
            return true;
        } else if (item.getItemId() == R.menu.comment) {
            onReceiveEntry (new NavigationComment());
	    return true;
        }
        return super.onOptionsItemSelected(item);
    }    
    
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_CATEGORIES:
            case CODE_SORT:
                if (getApp().hasNavigationEntry()) {
                    getApp().getCurrentNavigationEntry().onForceReloadView(this);
                }
                break;
	
            case CODE_PREFERENCE:
                getApp().onUserPreferenceChanged();
                break;
                
        }
    }
    
    protected void checkInitialNavigation(Bundle savedInstance) {
        if (savedInstance!=null && savedInstance.getBoolean("use-history-navigation")) {
            Debug.v("Initialize view with some data");
        } else {
            if (getApp().hasNavigationEntry()) {
                getApp().getCurrentNavigationEntry().onForceReloadView(this);
            }
        }
    }
        
    List<IEntrySummary> getAllEntries() {
        return getApp().getAllEntries();
    }

    IEntrySummary getEntry(String id) {
        return getApp().getEntry(id);
    }

    IEntryDetail getEntryDetails(String entryId) throws DataException {
        return getApp().getEntryDetails(entryId);
    } 
    
    List<IPhoto> getPhotos() {
        return getApp().getPhotos();
    }

    List<IEntryComment> getEntryComments(String entryId) {
        return getApp().getComments(entryId);
    }

    List<IPhoto> getPhotosForEntry(String entryId) {
        return getApp().getPhotosForEntry(entryId);
    }
    
    Settings getSettings() {
        return getApp().getSettings();
    }

    protected GeoPoint makeGeoPoint(Location location) {
        if (location!=null) {
            GeoPoint point = new GeoPoint(
                (int)(location.getLatitude()*1E6),
                (int)(location.getLongitude()*1E6));
            return point;
        }
        return null;
    }
    
    protected GeoPoint makeGeoPoint(double latitude, double longitude) {
        GeoPoint point = new GeoPoint(
            (int)(latitude*1E6),
            (int)(longitude*1E6));
            
        return point;
    }

    protected void setTextColor(View parent, int id, int color) {
        TextView text = (TextView)parent.findViewById(id);
        if (text!=null) {
            text.setTextColor(color);
        }
    }

    protected void setText(int id, int stringId) {
        TextView textView = (TextView)findViewById(id);
        if (textView!=null) {
            textView.setText(stringId);
        }
    }

    protected void setText(int id, String text) {
        TextView textView = (TextView)findViewById(id);
        if (textView!=null) {
            textView.setText(text);
        }
    }

    protected void showErrorAndTerminate(String message) {
        AlertDialog errorDialog = new AlertDialog.Builder(this).create();
        errorDialog.setTitle("Internal error");
        errorDialog.setMessage(message);
        errorDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                BaseActivity.this.finish();
            }
        });
    }

    protected void showToastNotification(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(
            R.layout.toast_error,
            (ViewGroup)findViewById(R.toast.root));

        ImageView image = (ImageView) layout.findViewById(R.toast.image);
        TextView text = (TextView) layout.findViewById(R.toast.text);
        text.setText(message);
        
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();        
    }

    protected Location getGPS() {
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        if (lm != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(criteria.ACCURACY_FINE);
            String provider = lm.getBestProvider(criteria, true);
            if (provider == null) {
                //Provide a suitable default if there is not "best" provider"
                provider = LocationManager.GPS_PROVIDER;
            }
            Location location = lm.getLastKnownLocation(provider);
            return location;
        }
        return null;
    }
    
    protected String getPrice(CurrencyAmount price) {
        return getSettings().formatCurrency(
            price,
            getResources().getString(R.string.price_free),
            getResources().getString(R.string.price_main_list));
    }

    protected void notifyLocationUpdated(Location location) {
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            //New location => let the view render the new information
            Debug.v("New GPS location received");
            notifyLocationUpdated(location);
        } catch (Exception error) {
            //Something went wrong => ignore the exception, there is 
            //nothing we can do anyway
            error.printStackTrace();
        }
    }    

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onStatusChanged(
        final String provider, 
        final int status, 
        final Bundle extras) {
    }
    
    public String loadAssetAsString(String fileName) throws IOException {  
        InputStream in = getAssets().open(fileName);  
        byte[] buffer = new byte[in.available()];  
        in.read(buffer);  
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        out.write(buffer);  
        out.close();  
        in.close();  
        return out.toString();  
    }          
    
    public boolean isOnline() {
        return getApp().isOnline();
    }
    
    public boolean checkOnLineAndDisplayMessage(String errorMessage) {
        boolean online = isOnline();
        if (!online) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setMessage(errorMessage);
            builder.setPositiveButton(getResources().getString(R.string.error_okay), null);
            AlertDialog alert = builder.create();
            alert.show();
        }
        return online;
    }
}
