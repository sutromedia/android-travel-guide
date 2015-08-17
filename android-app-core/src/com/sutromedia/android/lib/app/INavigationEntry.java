package com.sutromedia.android.lib.app;

import android.app.Activity;
import android.os.Bundle;

public interface INavigationEntry {

    Class getAcceptedActivity();
    void onNavigate(Activity activity);
    void onBackPressed(Activity activity);
    void onLeave(Activity activity);
    void onForceReloadView(Activity activity);
    void onSynchronizeWithEntry(Activity activity);
    void serialize(Bundle bundle);
    void unserialize(Bundle bundle);
}