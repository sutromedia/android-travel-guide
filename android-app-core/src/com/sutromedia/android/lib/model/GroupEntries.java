package com.sutromedia.android.lib.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.google.common.collect.*;


public class GroupEntries extends ModelBase {
    private LinkedListMultimap<String, String> mValues;

    public GroupEntries() {
        mValues = LinkedListMultimap.create();
    }
    
    public void add(String groupId, String entryId) throws DataException {
        ensureNotEmptyTrimmed(groupId, "groupId can not be null");
        ensureNotEmptyTrimmed(entryId, "entryId can not be null");
        if (!mValues.containsEntry(groupId, entryId)) {
            mValues.put(groupId, entryId);
        }
    }
    
    public int size() {
        return mValues.keySet().size();
    }
    
    public Set<String> getEntries(String groupId) {
        return getEntries(Collections.singleton(groupId));
    }
    
    public Set<String> getEntries(Collection<String> groupIds) {
        Set<String> all = Sets.newHashSetWithExpectedSize(mValues.size());
        for (String group : groupIds) {
            all.addAll(mValues.get(group));
        }
        return all;
    }    
}
