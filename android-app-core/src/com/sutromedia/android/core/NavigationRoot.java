package com.sutromedia.android.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationRoot extends NavigationBase {

    public NavigationRoot() {
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

    public void onNavigate(Activity fromActivity) {
	Class newActivity = getAcceptedActivity();
	Intent intent = new Intent(fromActivity, newActivity);
	fromActivity.startActivity(intent);
    }

    public void onBackPressed(Activity activity) {
	activity.finish();
    }
}