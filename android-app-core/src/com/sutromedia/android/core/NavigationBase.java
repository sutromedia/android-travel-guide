package com.sutromedia.android.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sutromedia.android.lib.app.INavigationEntry;

public abstract class NavigationBase implements INavigationEntry {

    protected static final String KEY_CLASS_NAME = "navigation-class";
    protected static final String KEY_DATA_GENERIC = "navigation-data";
    protected static final String KEY_DATA_MAP = "navigation-data-map";
    protected static final String KEY_DIRECTION = "navigation-forward";
    protected static final String KEY_FROM_MAP = "navigation-other-process";

    private Bundle      mMapData;

    public NavigationBase() {
    }
    
    public void setMapData(final Bundle bundle) {
	mMapData = bundle;
    }

    public Bundle getMapData() {
	return mMapData;
    }

    public void onNavigate(Activity activity) {
        if (activity.getClass().equals(getAcceptedActivity())) {
            onSynchronizeWithEntry(activity);
        } else {
            Class newActivity = getAcceptedActivity();
            Intent intent = new Intent(activity, newActivity);
            activity.startActivity(intent);
        }
    }

    public void onSynchronizeWithEntry(Activity activity) {
        //TODO: make the activity load itself from the entry
        BaseActivity base = (BaseActivity)activity;
        base.synchronizeWithEntry(this);
    }

    public void onBackPressed(Activity activity) {
        onNavigate(activity);
    }

    public void onLeave(Activity activity) {
        //nothing to do
    }

    protected MainApp getApp(final Activity activity) {       
        MainApp app = (MainApp)activity.getApplication();
	return app;
    }

    public void serialize(Bundle bundle) {
        if (mMapData != null) {
            bundle.putBundle(KEY_DATA_MAP, mMapData);
        }
    }

    public void unserialize(Bundle bundle) {
        if (bundle!=null) {
            mMapData = bundle.getBundle(KEY_DATA_MAP);
        }
    }    
}