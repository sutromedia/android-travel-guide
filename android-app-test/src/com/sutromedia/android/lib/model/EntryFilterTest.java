package com.sutromedia.android.lib.db;

import java.util.HashSet;
import java.util.Set;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class EntryFilterTest extends AndroidTestCase {

    private Set<String> makeCategories() {
        HashSet<String> all = new HashSet<String>();
        all.add("Travel");
        all.add("Children");
        return all;
    }
    
    private void checkCategories(Set<String> categories) {
        assertTrue(categories.contains("Travel"));
        assertTrue(categories.contains("Children"));
    }

    public void testShouldSucceedCreatingEmptyFilter() {
        EntryFilter filter = new EntryFilter();
        assertEquals(EntryFilter.FilterType.NONE, filter.getType());
        assertEquals(0, filter.getCategories().size());
    }
    
    public void testShouldSucceedCreatingEmptyFilterWithCategories() {
        EntryFilter filter = new EntryFilter(EntryFilter.FilterType.NONE, makeCategories());
        assertEquals(EntryFilter.FilterType.NONE, filter.getType());
        assertEquals(2, filter.getCategories().size());
        checkCategories(filter.getCategories());
    }
    
    public void testShouldSucceedCreatingFavoriteFilter() {
        EntryFilter filter = new EntryFilter(EntryFilter.FilterType.FAVORITE, makeCategories());
        assertEquals(EntryFilter.FilterType.FAVORITE, filter.getType());
        assertEquals(2, filter.getCategories().size());
        checkCategories(filter.getCategories());
    }    
}