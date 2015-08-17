package com.sutromedia.android.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.model.EntryFilter.FilterType;


public class CategoryListActivity
    extends DialogActivity
    implements GenericListAdapter.IListViewItemRenderer {

    private final static String   UNIQUE_ID_FOR_EVERYTHING = "ID_EVERYTHING";
    private final static String   UNIQUE_ID_FOR_FAVORITE = "ID_FAVORITE";
    private final static String   UNIQUE_ID_FOR_CATEGORY = "ID_CATEGORY";
    
    private final static String   DESC_FOR_EVERYTHING = "Everything";
    private final static String   DESC_FOR_FAVORITE = "Favorites";
    private final static String   DESC_FOR_CATEGORY = "Categories";

  
    private GenericListAdapter mAdapter;
    private EntryFilter        mFilter;
        
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        readFromDatabase();
        mFilter = getApp().getFilter();

        setContentView(R.layout.list_categories);
        ListView list = (ListView)findViewById(R.category.main_list);
        mAdapter.setupWith(list);
    }

    
    public void onSetupView(int viewId, View view, Object data) {
        if (viewId == R.layout.empty_separator) {
            return;
        }
    
        IGroup entry = (IGroup)data;
        TextView title = (TextView)view.findViewById(R.category.name);
        title.setText(entry.getName());
        
        ImageView selectionIcon = (ImageView)view.findViewById(R.category.selection_icon);
        boolean grayedOut = false;
        boolean selected = false;
        if (entry.getId().equals(UNIQUE_ID_FOR_EVERYTHING)) {
            title.setTextColor(0xFFFFFFFF);
            selected = (mFilter.getType()==FilterType.NONE);
        } else if (entry.getId().equals(UNIQUE_ID_FOR_FAVORITE)) {
            title.setTextColor(0xFFFFFFFF);
            selected = (mFilter.getType()==FilterType.FAVORITE);
        } else if (entry.getId().equals(UNIQUE_ID_FOR_CATEGORY)) {
            title.setTextColor(0xFFFFFFFF);
            selected = (mFilter.getType()==FilterType.CATEGORY);
        } else {
            grayedOut = !(mFilter.getType()==FilterType.CATEGORY);
            title.setTextColor(grayedOut ? 0xFFAAAAAA : 0xFFFFFFFF);
            selected = mFilter.getCategories().contains(entry.getId());
        }
        
        int iconChecked = grayedOut ? R.drawable.icon_checked_gray : R.drawable.icon_checked;
        selectionIcon.setImageResource(selected ? iconChecked : R.drawable.icon_unchecked);
    }
    
    public void onItemClicked(int viewId, Object data) {
        IGroup entry = (IGroup)data;
        String selectedCategory = entry.getId();
        
        if (selectedCategory.equals(UNIQUE_ID_FOR_EVERYTHING)) {
            mFilter = new EntryFilter(FilterType.NONE, mFilter.getCategories());
        } else if (selectedCategory.equals(UNIQUE_ID_FOR_FAVORITE)) {
            mFilter = new EntryFilter(FilterType.FAVORITE, mFilter.getCategories());
        } else if (selectedCategory.equals(UNIQUE_ID_FOR_CATEGORY)) {
            mFilter = new EntryFilter(FilterType.CATEGORY, mFilter.getCategories());
        } else {
            mFilter = mFilter.toggleCategory(selectedCategory);
        }

        mAdapter.notifyDataSetChanged();
    }
        
    public void onBackPressed () {
        getApp().setFilter(mFilter);
        finish();
    }

    private void displayErrorNotification(String message) {
        //TODO: put a toast notification
        Debug.v(message);
    }

    private void readFromDatabase() {
        try {
            mAdapter = new GenericListAdapter(this);
            mAdapter.addItem(
                new Group(UNIQUE_ID_FOR_EVERYTHING, DESC_FOR_EVERYTHING), 
                R.layout.category_entry_big);
                
            mAdapter.addItem(
                new Group(UNIQUE_ID_FOR_FAVORITE, DESC_FOR_FAVORITE),
                R.layout.category_entry_big);
                
           mAdapter.addItem(
                new Group(UNIQUE_ID_FOR_CATEGORY, DESC_FOR_CATEGORY),
                R.layout.category_entry_big);
                 
            List<IGroup> groups = getApp().getGroups();
            for (IGroup entry : groups) {
                mAdapter.addItem(entry, R.layout.category_entry);
            }
            
            mAdapter.addItem("", R.layout.empty_separator);

        } catch (Exception error) {
	    getApp().submitUnexpectedException(error);
            displayErrorNotification("Unable to read/open the database");
        }
    }    
    
    protected MainApp getApp() {
        return (MainApp)getApplication();
    }
    
    
}