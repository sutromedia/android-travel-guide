package com.sutromedia.android.core;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;
import android.util.DisplayMetrics;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.android.maps.*;

import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.device.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.model.EntrySorter.SortField;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.map.*;


public class MainListActivity 
    extends BaseActivity
    implements GenericListAdapter.IListViewItemRenderer,
               DownloadFilesTask.IDownloadNotification {

    private GenericListAdapter mAdapter;
    private DownloadFilesTask  mDownloader = null;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInitialNavigation(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean needRefresh = false;	
	int previousEntryCount = 0;
	if (mAdapter != null) {
	    previousEntryCount = mAdapter.getAll().size();
	}

        if (previousEntryCount != getAllEntries().size()) {
            needRefresh = true;
        } else {
	    int i = 0;
	    List<Object> previousEntries = mAdapter.getAll();
            for (IEntrySummary entry : getAllEntries()) {
                IEntrySummary oldEntry = (IEntrySummary)previousEntries.get(i);
                if (!entry.getId().equals(oldEntry.getId())) {
                    needRefresh = true;
                    break;
                }
		i++;
            }
        }
        
        if (needRefresh) {
            Debug.v("MainListActivity.onResume(): refresh was needed");
            initializeView();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mDownloader!=null) {
            mDownloader.cancel(false);
            mDownloader = null;
        }
    }

    public void initializeView() {
        Debug.v("MainListActivity.initializeView()");
        readFromDatabase();
    }
    
    protected void notifyLocationUpdated(Location location) {
        //New location update => redisplay the information on the screen
        // => mostly for distance information, possibly for the sort
        // Currently, update only the distance, not the sort.
        mAdapter.notifyDataSetChanged();
    }
    
    private void makeBold(TextView view, boolean condition) {
        view.setTypeface(null, condition ? Typeface.BOLD : Typeface.NORMAL);
    }

    public void onSetupView(int viewId, View view, Object data) {
        IEntrySummary entry = (IEntrySummary)data;
	if (viewId == R.layout.list_item_entry) {
	    onSetupViewEntry(view, entry);
	} else if (viewId == R.layout.list_item_entry_banner) {
	    onSetupViewBanner(view, entry);
	} else if (viewId == R.layout.list_item_category_banner) {
	    onSetupViewBanner(view, entry);
	}	
    }
    
    public void onSetupViewBanner(View view, IEntrySummary entry) {
        TextView title = (TextView)view.findViewById(R.entry.title);
        title.setText(entry.getName());

        ImageView image = (ImageView)view.findViewById(R.entry.logo);
        HelperImageLoader.setupImage(
	    this, 
	    Storage.getPrivateReadableFolder(this), 
	    image, 
	    entry, 
	    R.drawable.missing_details,
	    getApp().getBestPictureSize());

	DisplayMetrics metrics = getResources().getDisplayMetrics();
	image.setMaxWidth(metrics.heightPixels/2);

	View container = view.findViewById(R.entry.banner_group);
	if (container!= null) {
	    Drawable background = getResources().getDrawable(R.drawable.attrib_outside);
	    background.setAlpha(150);
	    container.setBackgroundDrawable(background);
	}
    }

    public void onSetupViewEntry(View view, IEntrySummary entry) {
        SortField sort = getApp().getSorter().getSortField();
        TextView title = (TextView)view.findViewById(R.entry.title);
        title.setText(entry.getName());
        
        TextView group = (TextView)view.findViewById(R.entry.group);
        group.setText(entry.getGroup());
        makeBold(group, sort==SortField.BY_NEIGHBORHOOD);
        
        TextView price = (TextView)view.findViewById(R.entry.price);
        price.setText(getPrice(entry.getPrice()));
        makeBold(price, sort==SortField.BY_COST);
        
        TextView distance = (TextView)view.findViewById(R.entry.distance);
        Location currentLocation = getGPS();
                
        String formattedDistance = null;
        Distance distanceFromHere = entry.getDistance(currentLocation);
        if (distanceFromHere!=null) {
            Distance.Unit unit = getSettings().getDistanceUnits();
            formattedDistance = distanceFromHere.getFormatted(
                getResources().getString(R.string.distance_close_short),
                getResources().getString(R.string.distance_far_short),
                getSettings().getDistanceUnitName(),
                100.0,
                getSettings().getDistanceUnits());
        }
        distance.setText(formattedDistance);
        makeBold(distance, sort==SortField.BY_DISTANCE);

        ImageView image = (ImageView)view.findViewById(R.entry.logo);
        HelperImageLoader.setupIcon(
	    this, 
	    Storage.getPrivateReadableFolder(this), 
	    image, 
	    entry, 
	    R.drawable.missing_icon);

        ImageView favorite = (ImageView)view.findViewById(R.entry.favorite);
        boolean visible = getApp().isFavorite(entry.getId());
        favorite.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    public void onItemClicked(int viewId, Object data) {
        IEntrySummary entry = (IEntrySummary)data;
        onReceiveEntry(new NavigationDetailWeb(entry.getId()));
    }
        
    private void displayErrorNotification(String message) {
        //TODO: put a toast notification
        Debug.v(message);
    }

    private void readFromDatabase() {
        Debug.v("MainListActivity.readFromDatabase()");
        try {
            setContentView(R.layout.list_entries);
            mAdapter = new GenericListAdapter(this);
	    
	    //IEntrySummary banner = getBannerEntry();
	    IEntrySummary banner = null;
	    if (banner != null) {
                mAdapter.addItem(banner, R.layout.list_item_category_banner);
	    }

	    boolean skipBannerEntry = (banner != null);
            for (IEntrySummary entry : getAllEntries()) {
		if (skipBannerEntry && entry.getId().equals(banner.getId()))  {
		    continue;
		}
                mAdapter.addItem(entry, R.layout.list_item_entry);
            }
            
            Debug.v("Initialize main list with [" + mAdapter.getCount() + "] entries");
            ListView list = (ListView)findViewById(R.entries.main_list);
            mAdapter.setupWith(list);
        } catch (Exception error) {
            Debug.v("MainListActivity.showErrorAndTerminate!!!!!!");
            showErrorAndTerminate("Unable to read/open the database");
        }
    }   
    
    private IEntrySummary getBannerEntry() {
	EntryFilter filter = getApp().getFilter();
	String banner = null;
	if (filter.getType() == EntryFilter.FilterType.NONE) {
	    banner = getSettings().getValue(Settings.Key.TOP_LEVEL_INTRO_ENTRY_ID, null);	    
	} else if (filter.getType() == EntryFilter.FilterType.CATEGORY) {
	    int count = filter.getCategories().size();
	    if (count == 1) {
		String groupId = filter.getCategories().toArray()[0].toString();
		Report.v("Looking for group ID=" + groupId);
		IGroup group = getApp().getGroup(groupId);
		Report.v("Group with ID[" + groupId + "] is " + group.toString());
		banner = group.getMainEntry();
		Report.v("Group banner[" + groupId + "] is " + banner);
	    }
	}

	if (banner != null) {
	    return getApp().getEntry(banner);
	}
	return null;
    }

    public void onReceiveDownloadedImage(PhotoDownload download) {
	mAdapter.notifyDataSetChanged();
    }

    public void onAllImagesReceived() {
        mDownloader = null;
    }    
    
    public void onDownloadImagesCancelled() {
    }
}