package com.sutromedia.android.core;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.TouchDelegate;
import android.webkit.WebView;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.*;

import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.map.*;

import com.sutromedia.android.lib.diagnostic.Report;

public class WebEntryActivity extends BaseActivity
    implements DownloadFilesTask.IDownloadNotification {

    private GuideWebClient     mWebClient;
    private IEntrySummary      mCurrentDisplayedEntry;
    private DownloadFilesTask  mDownloader = null;
    
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.webkit);
        WebView engine = (WebView) findViewById(R.id.web_engine);
        mWebClient = new GuideWebClient(this);
        engine.setWebViewClient(mWebClient);
        
        checkInitialNavigation(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDownloader!=null) {
            mDownloader.cancel(false);
            mDownloader = null;
        }
    }

    void synchronizeWithEntry(INavigationEntry entry) {
        if (entry instanceof NavigationDetailWeb) {
            NavigationDetailWeb detail = (NavigationDetailWeb)entry;
            if (mCurrentDisplayedEntry != null) {
                if (mCurrentDisplayedEntry.getId().equals(detail.getId())) {
                    //Nothing to do => the right entry is already being displayed
                } else {
                    //There is a new entry to display
                    entry.onForceReloadView(this);
                }
            } else {
                //There is no previous entry => should not happen
                Report.handleUnexpected("synchronizeWithEntry should not be called without a prior entry");
            }
        } else {
            Debug.v("#################################################");
            Debug.v("Unexpected type");
            Debug.printStackTrace();
        }        
    }

    @Override
    public void onResume() {
        super.onResume();
        mDownloader = new DownloadFilesTask(this);
        List<PhotoDownload> missing = new ArrayList<PhotoDownload>();
        if (mCurrentDisplayedEntry != null) {
            String mainIcon = mCurrentDisplayedEntry.getIconPhoto();
	    missing.add(new PhotoDownload(mainIcon, getApp().getBestPictureSize()));
        }
        mDownloader.execute(missing);
    }

    protected void notifyLocationUpdated(Location location) {
        //New location update => redisplay distance information
        if (location!=null && mCurrentDisplayedEntry!=null) {
            setupDistanceInformation(location, mCurrentDisplayedEntry);
        }
    }

    private void setupControl(int id, String value) {
        TextView control = (TextView)findViewById(id);
        control.setText(value);
    }

    private boolean setupControlOrHide(int id, String value) {
        TextView control = (TextView)findViewById(id);
        control.setText(value);
        boolean isVisible = (value != null) && (value.length()>0); 
        control.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        return isVisible;
    }

    private void setupDistanceInformation(
        Location location,
        IEntrySummary entry) {
 
       
        Distance distance = entry.getDistance(location);
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
        
        boolean distanceIsVisible = setupControlOrHide(R.detail.distance, formattedDistance);
        boolean priceIsVisible = setupControlOrHide(R.detail.price, getPriceInTitle(entry.getPrice()));
        TextView price = (TextView)findViewById(R.detail.price);
        if (!distanceIsVisible) {
            price.setGravity(Gravity.CENTER);            
        } else {
            price.setGravity(Gravity.RIGHT);
        }
    }

    private String getPriceInTitle(CurrencyAmount amount) {
        String price = null;
        if (amount != null) {
            return getSettings().formatCurrency(
                amount,
                getResources().getString(R.string.price_free),
                getResources().getString(R.string.price_per_adult));
        }
        return price;
    }    
    
    private void setupPhoto(IEntrySummary entry) {
        ImageView mainImage = (ImageView)findViewById(R.detail.main_image);
	DisplayMetrics metrics = getResources().getDisplayMetrics();
 	mainImage.setMaxHeight(metrics.heightPixels/2);
	mainImage.setMaxWidth(metrics.widthPixels/2);

	PhotoEntryResolver resolver = new PhotoEntryResolver(this, R.drawable.missing_details);
	resolver.setupImageForDetailsEntry(
	    this, 
	    mainImage, 
	    entry.getIconPhoto(), 
	    R.drawable.missing_details,
	    getApp().getBestPictureSize());
    }

    public void loadWebPage(final String id) {
        IEntrySummary entry = getEntry(id);
        Report.v("Loading entry [" + id + "]");

        WebView engine = (WebView) findViewById(R.id.web_engine);
        
        setupControl(R.detail.title, entry.getName());
        Location currentLocation = getGPS();
        setupDistanceInformation(currentLocation, entry);
        
        ImageView mainImage = (ImageView)findViewById(R.detail.main_image);
        mainImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onReceiveEntry(new NavigationPhoto(id));
            }
        });
        
        final ImageView favorite = (ImageView)findViewById(R.detail.favorite);
        setupFavoriteIcon(favorite, id);
        favorite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getApp().toggleFavorite(id);
                setupFavoriteIcon(favorite, id);
            }
        });
        
        final ImageView backToTop = (ImageView)findViewById(R.detail.goto_top);
        setupBackToTopIcon(backToTop);
        setupPhoto(entry);
        
        MapView map = (MapView)findViewById(R.detail.map);
        View noMap = findViewById(R.detail.no_map);
        if (entry.getLocation()!=null) {
            if (isOnline()) {
                map.setVisibility(View.VISIBLE);
                noMap.setVisibility(View.GONE);
                map.setBuiltInZoomControls(false);
                GeoPoint center = makeGeoPoint(entry.getLocation());
                    
                Drawable drawable = this.getResources().getDrawable(R.drawable.simple_map_marker);
                SimpleMapOverlay overlay = new SimpleMapOverlay(
                    drawable, 
                    new SimpleMapOverlay.ITapNotification() {
                        public boolean itemClicked(int index, GeoPoint point) {
                            return mapClickedNothing();
                        }

                        public boolean mapClickedNothing() {
                            String errorMessage = getResources().getString(R.string.error_map_no_connection);
                            if (checkOnLineAndDisplayMessage(errorMessage)) {
                                onReceiveEntry(new NavigationMap(id));
                            }
                            return true;
                        }
                    });
                //###JMV: change this with the appropriate strings
                OverlayItem overlayitem = new OverlayItem(center, "Hi there!", "Some information!");
                overlay.addOverlay(overlayitem);
                
                List<Overlay> mapOverlays = map.getOverlays();
                mapOverlays.clear();
                mapOverlays.add(overlay);
                
                map.getController().setCenter(center);
                map.getController().setZoom(getMapZoomLevel(2.0));
            } else {
                map.setVisibility(View.GONE);
                noMap.setVisibility(View.VISIBLE);
            }
        } else {
            //hide the map
            map.setVisibility(View.GONE);
            noMap.setVisibility(View.GONE);
        }
        
        mWebClient.loadDetailPageForEntry(engine, entry.getId());
        ScrollView root = (ScrollView)findViewById(R.detail.root_scrollview);
        root.scrollTo (0, 0);
        mCurrentDisplayedEntry = entry;
    }
        
    private static int getMapZoomLevel(double distanceInKm) {
        int zoom = 1;
        double E = 40075;
        zoom = (int) Math.round(Math.log(E/distanceInKm)/Math.log(2)+1);
        // to avoid exeptions
        if (zoom>21) zoom = 21;
        if (zoom<1) zoom = 1;
    
        return zoom;
    }
    
    private void setupFavoriteIcon(final ImageView view, String entryId) {
        boolean isFavorite = getApp().isFavorite(entryId);
        int icon = isFavorite ? R.drawable.heart_on : R.drawable.heart_off;
        view.setImageResource(icon);
    }
 
    private void setupBackToTopIcon(final ImageView view) {
        boolean hasBackToTop = getApp().hasBackToTopEntries();
        view.setVisibility(hasBackToTop ? View.VISIBLE : View.GONE);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getApp().backToTop(WebEntryActivity.this);
            }
        });
        
        Rect expandedSize = new Rect();
        view.getHitRect(expandedSize);
        expandedSize.inset(5, 5);
        TouchDelegate delegate = new TouchDelegate(expandedSize, view);
        View parent = (View)view.getParent();
        parent.setTouchDelegate(hasBackToTop ? delegate : null);
    }
    
    private void displayErrorNotification(String message) {
        //TODO: put a toast notification
        Report.v(message);
    }

    public void onReceiveDownloadedImage(PhotoDownload download) {
	if (mCurrentDisplayedEntry != null && mCurrentDisplayedEntry.getIconPhoto().equals(download.getId())) {   
	    Report.v("Notified that a high-res image for [" + download.getId() + "] was received");
	    setupPhoto(mCurrentDisplayedEntry);
	}
    }

    public void onAllImagesReceived() {
        mDownloader = null;
    }    
    
    public void onDownloadImagesCancelled() {
    }

}