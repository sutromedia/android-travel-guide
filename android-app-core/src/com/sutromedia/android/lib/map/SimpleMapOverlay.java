package com.sutromedia.android.lib.map;

import java.util.ArrayList;
import android.graphics.drawable.Drawable;
import com.google.android.maps.*;

import com.sutromedia.android.lib.diagnostic.Debug;

public class SimpleMapOverlay extends ItemizedOverlay {

    public interface ITapNotification {
        boolean itemClicked(int index, GeoPoint point);
        boolean mapClickedNothing();
    };

    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private ITapNotification       mParent;
    
    public SimpleMapOverlay(Drawable defaultMarker, ITapNotification notify) {
        super(boundCenterBottom(defaultMarker));
        mParent = notify;
    }
    
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }    
    
    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }    
    
    @Override
    public int size() {
        return mOverlays.size();
    }    
    
    @Override
    protected boolean onTap(int index) {
        if (mParent!=null) {
            Debug.v("OnTap(index)");
            OverlayItem item = mOverlays.get(index);
            GeoPoint point = item.getPoint();
            return mParent.itemClicked(index, point);
        }
        return false;
    }    
    
    public boolean onTap (final GeoPoint point, final MapView mapView) {
        boolean tapped = super.onTap(point, mapView);
        if (!tapped) {
            //No item was hit
            Debug.v("OnTap => nothing was hit");
            if (mParent!=null) {
                return mParent.mapClickedNothing();
            }
        }                   
        return tapped;
    }    
}
