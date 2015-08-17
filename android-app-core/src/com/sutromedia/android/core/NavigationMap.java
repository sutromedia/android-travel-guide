package com.sutromedia.android.core;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationMap extends NavigationBase {

    private String      mId;

    public NavigationMap() {
        mId = null;
    }

    public NavigationMap(String id) {
        mId = id;
    }

    String getId() {
	return mId;
    }

    public void onNavigate(Activity activity) {
        if (activity.getClass().equals(getAcceptedActivity())) {
            onForceReloadView(activity);
        } else {
            Intent intent = new Intent(activity, getAcceptedActivity());
            Bundle extras = new Bundle();
            serialize(extras);
            intent.putExtras(extras);
            activity.startActivity(intent);
	    com.sutromedia.android.lib.diagnostic.Debug.v("About to activate entry: " + this.toString());
        }
    }
    
    public void onForceReloadView(Activity activity) {
        if (mId==null) {
            ((FullMapActivity)activity).loadMarkersForAll(getMapData());
        } else {
            ((FullMapActivity)activity).loadMarkersForEntry(mId, getMapData());
        }
    }
    
    public Class getAcceptedActivity() {
        return FullMapActivity.class;
    }
    
    public void serialize(Bundle bundle) {
	super.serialize(bundle);
        bundle.putString(KEY_CLASS_NAME, getClass().getName());
        bundle.putString(KEY_DATA_GENERIC, mId);
    }
    
    public void unserialize(Bundle bundle) {
	super.unserialize(bundle);
        if (bundle!=null) {
            mId = bundle.getString(KEY_DATA_GENERIC);
        }
    }    

    public String toString() {
	String id = (mId == null) ? "All" : mId;
        return String.format("%s: LocationId=%s", getClass().getName(), id);
    }

}