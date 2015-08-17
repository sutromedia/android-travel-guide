package com.sutromedia.android.core;

import android.app.Activity;
import android.os.Bundle;
import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationMainList extends NavigationBase {

    public NavigationMainList() {
    }
    
    public void onForceReloadView(Activity activity) {
        ((MainListActivity)activity).initializeView();
    }
    
    public Class getAcceptedActivity() {
        return MainListActivity.class;
    }
    
    public void serialize(Bundle bundle) {
	super.serialize(bundle);
        bundle.putString(KEY_CLASS_NAME, getClass().getName());
    }
    
    public void unserialize(Bundle bundle) {
	super.unserialize(bundle);
    }    
}