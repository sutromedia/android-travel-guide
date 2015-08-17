package com.sutromedia.android.lib.model;


public class Group extends ModelBase implements IGroup {
    
    private String  mId;
    private String  mName;
    private String  mMainEntryId;

    public Group(String id, String name) throws DataException {
        mId = ensureNotEmptyTrimmed(id, "Group.id is required");
        mName = ensureNotEmptyTrimmed(name, "Group.name is required");
	mMainEntryId = null;
    }
    public Group(String id, String name, String mainEntryId) throws DataException {
	this(id, name);
	mMainEntryId = mainEntryId;
    }

    
    public String getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }

    public String getMainEntry() {
	return mMainEntryId;
    }

}