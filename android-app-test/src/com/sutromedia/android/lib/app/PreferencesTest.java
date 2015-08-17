package com.sutromedia.android.lib.app;

import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

import com.sutromedia.android.lib.model.EntryFilter;
import com.sutromedia.android.lib.model.EntryFilter.FilterType;
import com.sutromedia.android.lib.model.EntrySorter;
import com.sutromedia.android.lib.model.EntrySorter.SortField;

public class PreferencesTest extends AndroidTestCase {

    private Preferences  mPreferences;

    protected void setUp() throws Exception {
        mPreferences = new Preferences(getContext());
        mPreferences.clearFavorites();
        assertEquals(0, mPreferences.getFavorites().size());
    }

    protected void tearDown() throws Exception {
        mPreferences.clearFavorites();
        super.tearDown();
    }
    
    public void testShouldSucceedAddingFavorite() {
        mPreferences.addFavorite("entry1");
        assertTrue(mPreferences.getFavorites().contains("entry1"));
        assertEquals(1, mPreferences.getFavorites().size());
        //add it again => nothing should change
        mPreferences.addFavorite("entry1");
        assertTrue(mPreferences.getFavorites().contains("entry1"));
        assertEquals(1, mPreferences.getFavorites().size());
    }
    
    public void testShouldSucceedAddingMultipleFavorites() {
        mPreferences.addFavorite("entry1");
        mPreferences.addFavorite("entry2");
        assertTrue(mPreferences.getFavorites().contains("entry1"));
        assertTrue(mPreferences.getFavorites().contains("entry2"));
        assertEquals(2, mPreferences.getFavorites().size());
    }    
    
    public void testShouldSucceedDeletingValidFavorite() {
        mPreferences.addFavorite("entry1");
        mPreferences.addFavorite("entry2");
        mPreferences.removeFavorite("entry2");
        assertEquals(1, mPreferences.getFavorites().size());
        assertTrue(mPreferences.getFavorites().contains("entry1"));
        assertFalse(mPreferences.getFavorites().contains("entry2"));
    }        
    
    public void testShouldSucceedTogglingFavorite() {
        assertFalse(mPreferences.getFavorites().contains("entry1"));
        mPreferences.toggleFavorite("entry1");
        assertTrue(mPreferences.getFavorites().contains("entry1"));
        mPreferences.toggleFavorite("entry1");
        assertFalse(mPreferences.getFavorites().contains("entry1"));
    }    
    
    public void testShouldSucceedSetttingFilterNone() {
        mPreferences.setFilter(new EntryFilter());
        assertEquals(FilterType.NONE, mPreferences.getFilter().getType());
        assertEquals(0, mPreferences.getFilter().getCategories().size());
    }

    public void testShouldSucceedSetttingFilterFavorites() {
        mPreferences.setFilter(new EntryFilter(FilterType.FAVORITE, null));
        assertEquals(FilterType.FAVORITE, mPreferences.getFilter().getType());
        assertEquals(0, mPreferences.getFilter().getCategories().size());
    }
    
    public void testShouldSucceedSetttingFilterCategoriesNull() {
        mPreferences.setFilter(new EntryFilter(FilterType.CATEGORY, null));
        assertEquals(FilterType.CATEGORY, mPreferences.getFilter().getType());
        assertEquals(0, mPreferences.getFilter().getCategories().size());
    }
    
    public void testShouldSucceedSettingFilterCategoriesEmpty() {
        Set<String> categories = new HashSet<String>();
        mPreferences.setFilter(new EntryFilter(FilterType.CATEGORY, categories));
        assertEquals(FilterType.CATEGORY, mPreferences.getFilter().getType());
        assertEquals(0, mPreferences.getFilter().getCategories().size());
    }
    
    public void testShouldSucceedSettingFilterCategoriesNotEmpty() {
        Set<String> categories = new HashSet<String>();
        categories.add("Sports");
        categories.add("Nightlife");
        mPreferences.setFilter(new EntryFilter(FilterType.CATEGORY, categories));
        assertEquals(FilterType.CATEGORY, mPreferences.getFilter().getType());
        assertEquals(2, mPreferences.getFilter().getCategories().size());
        assertTrue(mPreferences.getFilter().getCategories().contains("Sports"));
        assertTrue(mPreferences.getFilter().getCategories().contains("Nightlife"));
    }
    
    
}

