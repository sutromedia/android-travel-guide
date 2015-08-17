package com.sutromedia.android.lib.app;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.sutromedia.android.lib.model.EntryFilter;
import com.sutromedia.android.lib.model.EntryFilter.FilterType;
import com.sutromedia.android.lib.model.EntrySorter;
import com.sutromedia.android.lib.model.EntrySorter.SortField;

public class Preferences {
    
    private final static String  PREFERENCES           = "Favorite";
    private final static String  PREFERENCE_FAVORITES  = "PreferenceFavorite";
    private final static String  PREFERENCE_FILTERS    = "PreferenceFilters";
    
    private final static String  KEY_SORT_FIELD        = "SortField";
    private final static String  KEY_SORT_ON_FAVORITES = "SortFavorites";
    private final static String  KEY_FILTER_TYPE       = "FilterOn";
    private final static String  KEY_FILTER_VALUES     = "FilterValues";

    private final static String  KEY_USER_ALIAS        = "UserAlias";
    private final static String  KEY_USER_EMAIL        = "UserEmail";
    private final static String  KEY_COMMENT_HASH      = "LastDownloadedComment";

    private SharedPreferences   mPreferences;
    private SharedPreferences   mPreferenceFavorites;
    private SharedPreferences   mPreferenceFilters;

    
    public Preferences(Context parent) {
        mPreferences = parent.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        mPreferenceFavorites = parent.getSharedPreferences(PREFERENCE_FAVORITES, Context.MODE_PRIVATE);
        mPreferenceFilters = parent.getSharedPreferences(PREFERENCE_FILTERS, Context.MODE_PRIVATE);
    }
    
    Enum getEnumForIndex(int index, Enum[] values) {
        Enum selected = values[0];
        if (index<values.length) {
            selected = values[index];
        }
        return selected;
    }
    
    public void setFilter(EntryFilter filter) {
        //Set the type in one of the preference files
        SharedPreferences.Editor editor1 = mPreferences.edit();  
        editor1.putInt(KEY_FILTER_TYPE, filter.getType().ordinal());
        editor1.commit();
    
        //Set the categories in another preference file
        SharedPreferences.Editor editor2 = mPreferenceFilters.edit();  
        editor2.clear();
        for (String category : filter.getCategories()) {
            editor2.putBoolean(category, true);
        }
        editor2.commit();
    }

    public EntryFilter getFilter() {
        int index = mPreferences.getInt(KEY_FILTER_TYPE, 0);
        EntryFilter.FilterType filterOn = (EntryFilter.FilterType)getEnumForIndex(
            index, 
            EntryFilter.FilterType.values());
       
        Set<String> filters = mPreferenceFilters.getAll().keySet();
        return new EntryFilter(filterOn, filters);
    }

    public void initDefaultSort(String sortOn) {
        if (!mPreferences.contains(KEY_SORT_FIELD)) {
	    if (sortOn.toLowerCase().equals("month")) {
		setSorter(new EntrySorter(SortField.BY_NEIGHBORHOOD, false));
	    }
	}
    }

    public void setSorter(EntrySorter sorter) {
        SharedPreferences.Editor editor = mPreferences.edit();  
        editor.putInt(KEY_SORT_FIELD, sorter.getSortField().ordinal());
        editor.putBoolean(KEY_SORT_ON_FAVORITES, sorter.isSortOnFavorites());
        editor.commit();
    }
    
    public EntrySorter getSorter() {
        int index = mPreferences.getInt(KEY_SORT_FIELD, 0);
        EntrySorter.SortField sortOn = (EntrySorter.SortField)getEnumForIndex(
            index, 
            EntrySorter.SortField.values());
            
        boolean sortFavorites = mPreferences.getBoolean(KEY_SORT_ON_FAVORITES, false);
        return new EntrySorter(sortOn, sortFavorites);
    }

    public Set<String> getFavorites() {
        return mPreferenceFavorites.getAll().keySet();
    }

    public void toggleFavorite(String id) {
        if (!mPreferenceFavorites.contains(id)) {
            addFavorite(id);
        } else {
            removeFavorite(id);
        }
    }
    
    public void addFavorite(String id) {
        if (!mPreferenceFavorites.contains(id)) {
            SharedPreferences.Editor editor = mPreferenceFavorites.edit();  
            editor.putBoolean(id, true);
            editor.commit();
        }
    }
    
    public void removeFavorite(String id) {
        if (mPreferenceFavorites.contains(id)) {
            SharedPreferences.Editor editor = mPreferenceFavorites.edit();  
            editor.remove(id);
            editor.commit();
        }
    }
    
    public void clearFavorites() {
        SharedPreferences.Editor editor = mPreferenceFavorites.edit();  
        editor.clear();
        editor.commit();
    }

    public void setUserInformation(
        final String alias,
        final String email) {

        SharedPreferences.Editor editor = mPreferences.edit();  
        editor.putString(KEY_USER_ALIAS, alias);
        editor.putString(KEY_USER_EMAIL, email);
        editor.commit();
    }

    public String getUserAlias() {
        return mPreferences.getString(KEY_USER_ALIAS, null);
    }

    public String getUserEmail() {
        return mPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getCommentHash() {
        return mPreferences.getString(KEY_COMMENT_HASH, null);
    }

    public void setCommentHash(final String hash) {
        SharedPreferences.Editor editor = mPreferences.edit();  
        editor.putString(KEY_COMMENT_HASH, hash);
        editor.commit();
    }
}
