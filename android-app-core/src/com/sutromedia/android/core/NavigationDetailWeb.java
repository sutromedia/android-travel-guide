package com.sutromedia.android.core;

import android.app.Activity;
import android.os.Bundle;
import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationDetailWeb extends NavigationBase {

    private String      mId;

    public NavigationDetailWeb() {
    }

    public NavigationDetailWeb(String id) {
        mId = id;
    }

    public String getId() {
	return mId;
    }

    public void onForceReloadView(Activity activity) {
        ((WebEntryActivity)activity).loadWebPage(mId);
    }
    
    public Class getAcceptedActivity() {
        return WebEntryActivity.class;
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
        return String.format("%s: EntryID=%s", getClass().getName(), mId);
    }
}