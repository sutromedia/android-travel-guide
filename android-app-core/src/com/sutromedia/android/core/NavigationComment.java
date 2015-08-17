package com.sutromedia.android.core;

import android.app.Activity;
import android.os.Bundle;
import com.sutromedia.android.lib.app.INavigationEntry;

public class NavigationComment extends NavigationBase {

    private String      mEntryId;
    
    public NavigationComment() {
    }
    
    public NavigationComment(String id) {
        mEntryId = id;
    }

    public String getId() {
        return mEntryId;
    }

    public void onForceReloadView(Activity activity) {
        ((CommentActivity)activity).loadCommentPage(mEntryId);
    }
    
    public Class getAcceptedActivity() {
        return CommentActivity.class;
    }
    
    public void serialize(Bundle bundle) {
        super.serialize(bundle);
        bundle.putString(KEY_CLASS_NAME, getClass().getName());
        bundle.putString(KEY_DATA_GENERIC, mEntryId);
    }
    
    public void unserialize(Bundle bundle) {
        super.unserialize(bundle);
        if (bundle!=null) {
            mEntryId = bundle.getString(KEY_DATA_GENERIC);
        }
    }    
}