package com.sutromedia.android.core;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.sutromedia.android.lib.app.Settings;
import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.model.EntryFilter.FilterType;
import com.sutromedia.android.lib.model.EntrySorter.SortField;


public class SortSettingActivity
    extends DialogActivity
    implements GenericListAdapter.IListViewItemRenderer {

    private final static String   SORT_CAPTION = "Sort order";
    private final static String   SORT_NAME = "Name";
    private final static String   SORT_DISTANCE = "Distance";
    private final static String   SORT_COST = "Cost";
    private final static String   SORT_NEIGHBORHOOD = "Neighborhood";
    private final static String   SORT_FAVORITE_FIRST = "Put Favorites at the top";
    private final static String   SORT_OPTIONS = "Options";
  
    private GenericListAdapter          mAdapter;
    private HashMap<String,SortField>   mExclusiveOptions;    
    private EntrySorter                 mSorter;
    
        
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        readFromDatabase();
        mSorter = getApp().getSorter();

        setContentView(R.layout.sort_settings);
        ListView list = (ListView)findViewById(R.sort.main_list);
        mAdapter.setupWith(list);
        
        mExclusiveOptions = new HashMap<String,SortField>();
        mExclusiveOptions.put(SORT_NAME, SortField.BY_NAME);
        mExclusiveOptions.put(SORT_DISTANCE, SortField.BY_DISTANCE);
	mExclusiveOptions.put(SORT_COST, SortField.BY_COST);
        mExclusiveOptions.put(getSpatialGroup(), SortField.BY_NEIGHBORHOOD);
    }

    private String getSpatialGroup() {
	String spatial_group = getApp().getSettings().getValue(
	    Settings.Key.SPATIAL_GROUPS_NAME_SINGULAR, 
	    SORT_NEIGHBORHOOD);
	return spatial_group;
    }

    private boolean hasPrices() {
	return getApp().getSettings().getBooleanValue(
	    Settings.Key.HAS_PRICES, 
	    false);
    }

    
    public void onSetupView(int viewId, View view, Object data) {
        String entryName = data.toString();
        if (viewId == R.layout.caption_separator) {
            TextView title = (TextView)view.findViewById(R.category.name);
            title.setText(entryName);
        } else if (viewId == R.layout.category_entry) {
            TextView title = (TextView)view.findViewById(R.category.name);
            title.setText(entryName);
            
            boolean isChecked = false;
            ImageView selectionIcon = (ImageView)view.findViewById(R.category.selection_icon);
            if (mExclusiveOptions.containsKey(entryName)) {
                SortField currentSortField = mSorter.getSortField();
                isChecked = currentSortField.equals(mExclusiveOptions.get(entryName));
            } else {
                if (SORT_FAVORITE_FIRST.equals(entryName)) {
                    isChecked = mSorter.isSortOnFavorites();
                }
            }
            int iconId = isChecked ? R.drawable.icon_checked : R.drawable.icon_unchecked;
            selectionIcon.setImageResource(iconId);
        }
    }
    
    public void onItemClicked(int viewId, Object data) {
        if (viewId == R.layout.category_entry) {
            String entryName = data.toString();
            if (mExclusiveOptions.containsKey(entryName)) {
                mSorter.setSortField(mExclusiveOptions.get(entryName));
            } else {
                if (SORT_FAVORITE_FIRST.equals(entryName)) {
                    mSorter.toogleFavorites();
                }
            }
        }
        mAdapter.notifyDataSetChanged();       
    }
        
    public void onBackPressed () {
        getApp().setSorter(mSorter);
        finish();
    }
        
    private void readFromDatabase() {
        mAdapter = new GenericListAdapter(this);
 
        mAdapter.addItem(SORT_CAPTION, R.layout.caption_separator);
        mAdapter.addItem(SORT_NAME, R.layout.category_entry);
        mAdapter.addItem(SORT_DISTANCE, R.layout.category_entry);
 	if (hasPrices()) {
	    mAdapter.addItem(SORT_COST, R.layout.category_entry);
	}
        mAdapter.addItem(getSpatialGroup(), R.layout.category_entry);
        mAdapter.addItem(SORT_OPTIONS, R.layout.caption_separator);
        mAdapter.addItem(SORT_FAVORITE_FIRST, R.layout.category_entry);
        mAdapter.addItem("", R.layout.empty_separator);
    }    
}