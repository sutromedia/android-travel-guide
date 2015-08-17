package com.sutromedia.android.lib.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EntryFilter {

    public enum FilterType {
        NONE, 
        FAVORITE,
        CATEGORY
    }

    private FilterType  mFilterType;
    private Set<String> mCategories;
    
    public EntryFilter() {
        mFilterType = FilterType.NONE;
        mCategories = Collections.emptySet();
    }
    
    public EntryFilter(FilterType filter, Set<String> categories) {
        mFilterType = filter;
        if (categories != null) {
            mCategories = new HashSet<String>(categories);
        } else {
            mCategories = new HashSet<String>();
        }
    }
    
    public EntryFilter(EntryFilter other) {
        this(other.mFilterType, other.mCategories);
    }
    
    public FilterType getType() {
        return mFilterType;
    }
       
    public Set<String> getCategories() {
        return mCategories;
    }
    
    public EntryFilter toggleCategory(String category) {
        HashSet<String> newCategories = new HashSet<String>(mCategories);    
        if (mCategories.contains(category)) {
            newCategories.remove(category);
        } else {
            newCategories.add(category);
        }
        
        return new EntryFilter(getType(), newCategories);
    }
    
}