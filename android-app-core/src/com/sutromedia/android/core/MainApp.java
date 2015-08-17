package com.sutromedia.android.core;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Set;

import org.apache.http.client.params.HttpClientParams;

import com.sutromedia.android.lib.app.INavigationEntry;

import com.sutromedia.android.lib.app.Preferences;
import com.sutromedia.android.lib.app.Settings;
import com.sutromedia.android.lib.db.GuideComment;
import com.sutromedia.android.lib.db.GuideDatabase;
import com.sutromedia.android.lib.db.ModelFactory;
import com.sutromedia.android.lib.device.Storage;
import com.sutromedia.android.lib.model.DataException;
import com.sutromedia.android.lib.model.EntryFilter;
import com.sutromedia.android.lib.model.GroupEntries;
import com.sutromedia.android.lib.model.IEntryComment;
import com.sutromedia.android.lib.model.IEntryDetail;
import com.sutromedia.android.lib.model.IEntrySummary;
import com.sutromedia.android.lib.model.IGroup;
import com.sutromedia.android.lib.model.IPhoto;
import com.sutromedia.android.lib.model.EntryComparator;
import com.sutromedia.android.lib.model.EntrySorter;
import com.sutromedia.android.lib.model.PhotoDownload;
import com.sutromedia.android.lib.util.Timer;
import com.sutromedia.android.lib.diagnostic.*;


import org.acra.*;
import org.acra.annotation.*;

//cloudant password/key: wasedgederylicaldedisamn/QXRHo2ulYMKouv3xiqdBXKpk

@ReportsCrashes(
    formKey = "",
    formUri = "https://jvally.cloudant.com/acra-sutromedia-guides-android/_design/acra-storage/_update/report",
    reportType = org.acra.sender.HttpSender.Type.JSON,
    httpMethod = org.acra.sender.HttpSender.Method.PUT,
    formUriBasicAuthLogin="wasedgederylicaldedisamn",
    formUriBasicAuthPassword="QXRHo2ulYMKouv3xiqdBXKpk"
)
public class MainApp extends Application {

    private static final String KEY_DATABASE_VERSION = "LAST_SAVED_DATABASE";

    private GuideDatabase               mGuide;

    private List<IEntrySummary>         mAllEntries;
    private List<IPhoto>                mPhotos;
    private Settings                    mSettings;
    private GroupEntries                mGroups;
    private HashSet<String>             mMissingPhotos;
    private HashSet<String>             mAssetPhotos;
    private List<IEntryComment>         mAllComments;

    private Preferences                 mPreferences;
    private EntrySorter                 mSorter;
    private Location                    mLocation;
    private AndroidHttpClient           mHttpConnection = null;


    private Stack<INavigationEntry>     mHistory;

    @Override
    public void onCreate() {
	Report.enable(true);
        super.onCreate();
        ACRA.init(this);
        mHistory = new Stack<INavigationEntry>();
        Report.v("Application onCreate called");
        refreshPreferences();
    }

    boolean needInitialization() {
	return mGuide == null || mAllEntries == null ;
    }

    void refreshComments() throws Exception {
        if (isOnline()) {
            String hash = mPreferences.getCommentHash();
            GuideComment comments = new GuideComment();

            hash = comments.checkUpdate(getApplicationContext(), mGuide, hash, getAppId());
            mPreferences.setCommentHash(hash);
            mAllComments = ModelFactory.getComments(mGuide);
        }
    }

    String getAppId() {
        return mSettings.getValue(Settings.Key.APP_ID, null);
    }

    int getApplicationCode() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException error) {
            //This should never happen => we should always be able to retrieve our own package info
        }
        return 0;
    }

    int getSavedDatabaseCode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getInt(KEY_DATABASE_VERSION, 0);
    }

    void saveDatabaseCode(final int lastSaved, final int appCode) {
        if (lastSaved != appCode) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_DATABASE_VERSION, appCode);
            editor.commit();
            Report.v("Committing new database version: " + appCode);
        }
    }

    void ensureDatabase() throws Exception {
        if (mGuide==null) {
            Report.v("About to load the databases");
            mGuide = new GuideDatabase(this, "content.sqlite3.jet", "content.sqlite3");

            int lastSaved = getSavedDatabaseCode();
            int appCode = getApplicationCode();
            mGuide.createDataBase(lastSaved, appCode);
            Report.v("Database ready to be used");

            mSettings = ModelFactory.getSettings(mGuide);
            mGroups = ModelFactory.getGroupEntries(mGuide);
            Report.v("Loaded all groups [" + mGroups.size() + " entries]" );
            mAllEntries = ModelFactory.getEntries(getGuide());
            Report.v("Loaded all entries [" + mAllEntries.size() + " entries]");
            mAllComments = ModelFactory.getComments(getGuide());
            Report.v("Loaded all comments [" + mAllComments.size() + " entries]");

            checkMissingPhotos();
            checkStorageSpace();
            getPhotos();
            mPreferences.initDefaultSort(mSettings.getValue(Settings.Key.DEFAULT_SORT_OPTION, ""));
            onUserPreferenceChanged();
            Report.v("All data was loaded successfully");
            saveDatabaseCode(lastSaved, appCode);
        }
    }

    AndroidHttpClient getHttp() {
        synchronized(this) {
            if (mHttpConnection == null) {
                mHttpConnection = AndroidHttpClient.newInstance("SutroMedia");
                HttpClientParams.setRedirecting(mHttpConnection.getParams(), true);
            }
        }
        return mHttpConnection;
    }

    List<IEntryComment> getComments() {
        return mAllComments;
    }

    List<IEntryComment> getComments(String entryId) {

        List<IEntryComment> filtered = new ArrayList<IEntryComment>();
        for (IEntryComment comment : getComments()) {
            String id = comment.getEntryId();
            if (id != null && id.equals(entryId)) {
                filtered.add(comment);
            }
        }
        return filtered;
    }

    void refreshPreferences() {
        mPreferences = new Preferences(this);
    }

    void onUserPreferenceChanged() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean allowReport = prefs.getBoolean("DIAGNOSTIC_REPORT", true);
        boolean allowDebug = prefs.getBoolean("DIAGNOSTIC_DEBUG", false);
        boolean sendReportNow = prefs.getBoolean("DIAGNOSTIC_SEND_NOW", false);
        Report.enable(allowReport);
        Debug.enable(allowDebug);
        Debug.enable(true);

        if (sendReportNow) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("DIAGNOSTIC_SEND_NOW", false);
            editor.commit();
            submitUnexpectedException(new Exception("Report submitted by user"));
            CharSequence text = "Thank you! The diagnostic report has been sent.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
    }

    void submitUnexpectedException(Exception error) {
        error.printStackTrace();
        ErrorReporter.getInstance().handleSilentException(error);
    }

    private void displayStorageStats(File path, String type) {
	if (path != null) {
	    StatFs stat = new StatFs(path.getPath());
	    long bytesTotal = (long)stat.getBlockSize() *(long)stat.getBlockCount();
	    long mbTotal = bytesTotal / (1024 * 1024);
	    long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
	    long mbFree = bytesAvailable / (1024 * 1024);

	    Report.v(type + ": Storage location = " + path.getPath());
	    Report.v("Storage " + type + " Total = " + mbTotal + "MB");
	    Report.v("Storage " + type + " Free = " + mbFree + "MB");
	} else {
	    Report.v(type + ": Storage type [" + type + "] is unavailable");
	}
    }

    private void checkStorageSpace() {
        displayStorageStats(Environment.getDataDirectory(), "Internal");
        displayStorageStats(Environment.getExternalStorageDirectory(), "SD");
    }

    int getBestPictureSize() {
        return mSettings.getBestPictureSize(this);
    }

    public boolean isOnline() {
        Object service = getSystemService(CONNECTIVITY_SERVICE);
        if (service!=null) {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

            return (cm.getActiveNetworkInfo()!=null)
                && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
        return false;
    }

    void setLocation(Location location) {
        mLocation = location;
    }

    private GuideDatabase getGuide() {
        return mGuide;
    }

    public void setFilter(EntryFilter filter) {
        mPreferences.setFilter(filter);
    }

    public EntryFilter getFilter() {
        return mPreferences.getFilter();
    }

    public void setSorter(EntrySorter sorter) {
        mPreferences.setSorter(sorter);
        mSorter = mPreferences.getSorter();
    }

    public EntrySorter getSorter() {
        if (mSorter==null) {
            mSorter = mPreferences.getSorter();
        }
        return mSorter;
    }

    public void setUserInformation(
        final String alias,
        final String email) {
        mPreferences.setUserInformation(alias, email);
    }

    public String getUserAlias() {
        return mPreferences.getUserAlias();
    }

    public String getUserEmail() {
        return mPreferences.getUserEmail();
    }

    List<IEntrySummary> getAllEntries() {
        return getSortedEntries();
    }

    List<IPhoto> getPhotos() {
        if (mPhotos==null) {
            try {
                List<IPhoto> photos = ModelFactory.getPhotos(getGuide());
		List<IPhoto> downloaded = new ArrayList<IPhoto>();
		List<IPhoto> missing = new ArrayList<IPhoto>();
		for (IPhoto photo : photos) {
		    if (mAssetPhotos.contains(photo.getId())) {
			downloaded.add(photo);
		    } else {
			missing.add(photo);
		    }
		}
		Report.v("Pictures installed = " + downloaded.size());
		Report.v("Pictures missing = " + missing.size() );
                Collections.shuffle(downloaded);
                Collections.shuffle(missing);

		mPhotos = new ArrayList<IPhoto>();
		mPhotos.addAll(downloaded);
		mPhotos.addAll(missing);
            } catch (Exception error) {
                //TODO: Can't open the database => Error?
            }
        }
        return getFilteredPhotos();
    }

    void toggleFavorite(String entryId) {
        mPreferences.toggleFavorite(entryId);
    }

    boolean hasBackToTopEntries() {
        return getDetailEntryDepth()>1;
    }

    int getDetailEntryDepth() {
        int depth = 0;
        for (int i = mHistory.size()-1; i>=0; i--) {
            INavigationEntry entry = mHistory.get(i);
            if (!entry.getClass().equals(NavigationDetailWeb.class)) {
                break;
            }
            depth++;
        }
        return depth;
    }

    void backToTop(Activity activity) {
        int entriesToPop = getDetailEntryDepth() - 1;
        for (int i=0;i<entriesToPop;i++) {
            mHistory.pop();
            displayNavigationStack();
        }
        activity.onBackPressed();
    }

    void enterAppGotoMainScreen(final Activity activity) {
	mHistory.clear();
        navigateNextLocation(activity, new NavigationRoot());
    }

    boolean isFavorite(String entryId) {
        return mPreferences.getFavorites().contains(entryId);
    }

    List<IPhoto> getPhotosForEntry(String entryId) {
        List<IPhoto> photos = null;
        try {
            photos = ModelFactory.getPhotos(getGuide(), entryId);
        } catch (Exception error) {
            //TODO: Can't open the database => Error?
        }
        return photos;
    }

    IEntryDetail getEntryDetails(String entryId) throws DataException {
        return ModelFactory.getEntryDetails(getGuide(), entryId);
    }

    IEntrySummary getEntry(String id) {
        for (IEntrySummary entry : mAllEntries) {
            if (entry.getId().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    Settings getSettings() {
        return mSettings;
    }

    List<IGroup> getGroups() throws DataException {
        return ModelFactory.getGroups(mGuide);
    }

    IGroup getGroup(String groupId) {
	try {
	    for (IGroup group : getGroups()) {
		if (group.getId().equals(groupId)) {
		    return group;
		}
	    }
	} catch (Exception error) {
	}
	return null;
    }

    void clearNavigationStack() {
        mHistory.clear();
    }

    INavigationEntry getCurrentNavigationEntry() {
        return mHistory.peek();
    }

    boolean hasNavigationEntry() {
        return mHistory.size()>0;
    }


    boolean hasPreviousNavigationEntry() {
        return mHistory.size()>1;
    }

    void navigateNextLocation(Activity activity, INavigationEntry next) {
	//Debug.printStackTrace();
	if (hasNavigationEntry()) {
	    getCurrentNavigationEntry().onLeave(activity);
	}
        mHistory.add(next);
        displayNavigationStack();
        next.onNavigate(activity);
    }

    void setMapDataForCurrentLocation(Bundle mapData) {
	INavigationEntry current = getCurrentNavigationEntry();
	if (current instanceof NavigationMap) {
	    NavigationMap navigation = (NavigationMap)current;
	    navigation.setMapData(mapData);
	} else {
	    Report.handleUnexpected("setMapDataForCurrentLocation should be called only when coming back from a map");
	}
    }

    void pushNextLocation(INavigationEntry next) {
	//Debug.printStackTrace();
        mHistory.add(next);
        displayNavigationStack();
    }

    void popCurrentLocation() {
	//Debug.printStackTrace();
        mHistory.pop();
        displayNavigationStack();
    }

    void navigateBack(Activity activity) {
	if (hasNavigationEntry()) {
	    getCurrentNavigationEntry().onLeave(activity);
	    mHistory.pop();
	    if (hasNavigationEntry()) {
		getCurrentNavigationEntry().onBackPressed(activity);
	    } else {
		activity.finish();
	    }
	} else {
	    activity.finish();
	}
    }

    void displayNavigationStack() {
        int i=1;
        Debug.v("========================================================");
        for (INavigationEntry entry : mHistory) {
            Debug.v("Entry [" + i + "]:" + entry.toString());
            i++;
        }
        Debug.v("========================================================");
    }

    private List<IEntrySummary> getEntriesInSet(Set<String> entries) {
        List<IEntrySummary> all = new ArrayList<IEntrySummary>();
        for (IEntrySummary entry : mAllEntries) {
            if (entries.contains(entry.getId())) {
                all.add(entry);
            }
        }

        if (all.size()==0) {
            CharSequence text = "No favorites have been setup!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
            return mAllEntries;
        } else {
            return all;
        }
    }

    private List<IPhoto> getPhotosInSet(Set<String> entries) {
        List<IPhoto> all = new ArrayList<IPhoto>();
        for (IPhoto photo : mPhotos) {
            if (entries.contains(photo.getEntryId())) {
                all.add(photo);
            }
        }
        return (all.size() == 0) ? mPhotos : all;
    }

    private List<IEntrySummary> getFilteredEntries() {
        switch (getFilter().getType()) {
            case FAVORITE:
                return getEntriesInSet(mPreferences.getFavorites());

            case CATEGORY:
                Set<String> filteredEntries = mGroups.getEntries(getFilter().getCategories());
                return getEntriesInSet(filteredEntries);

            default:
                return mAllEntries;
        }
    }

    private List<IEntrySummary> getSortedEntries() {
        ArrayList<IEntrySummary> all = new ArrayList<IEntrySummary>(getFilteredEntries());
        EntryComparator comparator = new EntryComparator(getSorter().getSortField());
        comparator.setLocation(mLocation);
        if (getSorter().isSortOnFavorites()) {
            comparator.setFavorites(mPreferences.getFavorites());
        }
        Collections.sort(all, comparator);
        return all;
    }

    private List<IPhoto> getFilteredPhotos() {
        switch (getFilter().getType()) {
            case FAVORITE:
                return getPhotosInSet(mPreferences.getFavorites());

            case CATEGORY:
                Set<String> filteredEntries = mGroups.getEntries(getFilter().getCategories());
                return getPhotosInSet(filteredEntries);

            default:
                return mPhotos;
        }
    }

    File getDestinationPhotoFolder() {
	List<File> folders = Storage.getPrivateWriteableFolder(this);
	if (folders != null && folders.size() > 0) {
	    File root = folders.get(0);
	    return root;
	}
	return null;
    }


    private void checkMissingPhotos() throws DataException {
	int bestPictureSize = getBestPictureSize();
        ArrayList<String> photos = ModelFactory.getPhotosToDownload(getGuide(), bestPictureSize);

	Timer timer = new Timer();

	List<String> privateFiles = new ArrayList<String>();
	for (File folder : Storage.getPrivateReadableFolder(this)) {
	    Collections.addAll(privateFiles, folder.list());
	}

	String[] assets = getAssetImages();
	HashSet<String> allFiles = new HashSet<String>(privateFiles.size() + assets.length);
	allFiles.addAll(privateFiles);
	Collections.addAll(allFiles, assets);

	mMissingPhotos = new HashSet(photos.size());
	for (String id : photos) {
	    PhotoDownload download = new PhotoDownload(id, bestPictureSize);
	    String name = download.getLocalFilename();
	    if (!allFiles.contains(name)) {
		mMissingPhotos.add(id);
	    }
	}

        ArrayList<String> present = ModelFactory.getPhotosFoundInAssets(getGuide());
	mAssetPhotos = new HashSet<String>();
	for (String id : present) {
	    mAssetPhotos.add(id);
	}
    }

    private String[] getAssetImages() {
	String[] assets = null;
	try {
	    assets = getAssets().list("images/");
	} catch (IOException error) {
	    //No assets?
	    //Just ignore it
	    assets = new String[0];
	}
	return assets;
    }
}
