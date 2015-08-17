package com.sutromedia.android.lib.model;

public class EntrySorter {

    public enum SortField {
        BY_NAME,
        BY_DISTANCE,
        BY_COST,
        BY_NEIGHBORHOOD
    }

    private SortField   mSortField;
    private boolean     mSortOnFavorites;
    
    public EntrySorter() {
        mSortField = SortField.BY_NAME;
        mSortOnFavorites = false;
    }
    
    public EntrySorter(EntrySorter other) {
        mSortField = other.mSortField;
        mSortOnFavorites = other.mSortOnFavorites;
    }
    
    public EntrySorter(SortField sort, boolean onFavorites) {
        mSortField = sort;
        mSortOnFavorites = onFavorites;
    }
    
    public SortField getSortField() {
        return mSortField;
    }
       
    public boolean isSortOnFavorites() {
        return mSortOnFavorites;
    }
       
    public void toogleFavorites() {
        mSortOnFavorites = !mSortOnFavorites;
    }
    
    public void setSortField(SortField sort) {
        mSortField = sort;
    }
    
}