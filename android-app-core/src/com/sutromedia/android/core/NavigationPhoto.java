package com.sutromedia.android.core;

import android.app.Activity;
import android.os.Bundle;
import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationPhoto extends NavigationBase {

    private String      mId;

    public NavigationPhoto() {
        mId = null;
    }

    public NavigationPhoto(String id) {
        mId = id;
    }

    public String getId() {
	return mId;
    }
    
    public void onLeave(Activity activity) {
	if (activity instanceof PhotoActivity) {
	    ((PhotoActivity)activity).onEndSlideShow();
	}
	super.onLeave(activity);
    }
    
    public void onForceReloadView(Activity activity) {
        if (mId==null) {
            ((PhotoActivity)activity).loadImagesForAll();
        } else {
            ((PhotoActivity)activity).loadImagesForEntry(mId);
        }
    }
    
    public Class getAcceptedActivity() {
        return PhotoActivity.class;
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
        return String.format("%s: PhotoId=%s", getClass().getName(), id);
    }

}