package com.sutromedia.android.lib.db;

import java.util.Set;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;

 
public class GroupEntriesTest extends AndroidTestCase {


    public void testShouldReturnEmptyListForUnknownGroupid() {
        GroupEntries groups = new GroupEntries();
        assertEquals(0, groups.getEntries("not there").size());
    }

    public void testShouldFailWhenAddingNullGroup() {
        GroupEntries groups = new GroupEntries();
        try {
            groups.add(null, "entry");
            fail("should have thrown an exception");
        } catch (DataException error) {
            assertEquals("groupId can not be null", error.getMessage());
        }
    }

    public void testShouldFailWhenAddingNullEntry() throws DataException {
        GroupEntries groups = new GroupEntries();
        try {
            groups.add("group", null);
            fail("should have thrown an exception");
        } catch (DataException error) {
            assertEquals("entryId can not be null", error.getMessage());
        }
    }

    public void testShouldReturnEntriesForValidGroupId() throws DataException {
        GroupEntries groups = new GroupEntries();
        groups.add("group1", "entry3");
        groups.add("group1", "entry2");
        groups.add("group1", "entry1");
        groups.add("group2", "entry4");
        assertEquals(2, groups.size());
        Set<String> entries = groups.getEntries("group1");
        assertEquals(3, entries.size());
        assertTrue(entries.contains("entry3"));
        assertTrue(entries.contains("entry2"));
        assertTrue(entries.contains("entry1"));
        assertTrue(!entries.contains("entry4"));
    }
    
    public void testShouldIgnoreDuplicateEntries() throws DataException {
        GroupEntries groups = new GroupEntries();
        groups.add("group1", "entry");
        groups.add("group1", "entry");
        Set<String> entries = groups.getEntries("group1");
        assertEquals(1, entries.size());
        assertTrue(entries.contains("entry"));
    }
}