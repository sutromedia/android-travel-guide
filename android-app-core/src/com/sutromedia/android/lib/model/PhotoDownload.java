package com.sutromedia.android.lib.model;


public class PhotoDownload {
    private String  mId;
    private int     mSize;

    public PhotoDownload(String id, int size) {
	mId = id;
	mSize = size;
    }

    public String getId() {
	return mId;
    }
    
    public int getSize() {
	return mSize;
    }

    public String getLocalFilename() {
	return PhotoSize.getLocalFilename(getId(), getSize());
    }
}
