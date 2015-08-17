package com.sutromedia.android.core;

import android.app.Activity;
import android.os.Bundle;
import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationWeb extends NavigationBase {

    private String      mUrl;

    public NavigationWeb() {
    }
    
    public NavigationWeb(String url) {
        mUrl = url;
    }
       
    public void onForceReloadView(Activity activity) {
        ((WebActivity)activity).loadWebPage(mUrl);
    }
    
    public Class getAcceptedActivity() {
        return WebActivity.class;
    }
    
    public void serialize(Bundle bundle) {
	super.serialize(bundle);
        bundle.putString(KEY_CLASS_NAME, getClass().getName());
        bundle.putString(KEY_DATA_GENERIC, mUrl);
    }
    
    public void unserialize(Bundle bundle) {
	super.unserialize(bundle);
        if (bundle!=null) {
            mUrl = bundle.getString(KEY_DATA_GENERIC);
        }
    }    

    public String toString() {
        return String.format("%s: url=%s", getClass().getName(), mUrl);
    }
}