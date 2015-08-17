package com.sutromedia.android.lib.model;


public class Photo extends ModelBase implements IPhoto {

    private String      mId;
    private String      mEntryId;
    private String      mEntryName;
    private String      mCaption = null;
    private String      mAuthor = null;
    private int         mLicense = 0;
    private String      mUrl = null;

    public Photo(
        String id,
        String entryId,
        String entryName) throws DataException {
        
        mId = ensureNotEmptyTrimmed(id, "Photo.id can not be null or empty");
        mEntryId = ensureNotEmptyTrimmed(entryId, "Photo.entryId can not be null or empty");
        mEntryName = ensureNotEmptyTrimmed(entryName, "Photo.entryName can not be null or empty");
    }

    public String getId() {
        return mId;
    }
    
    public String getEntryId() {
        return mEntryId;
    }
    
    public String getEntryName() {
        return mEntryName;
    }
    
    public void setCaption(String caption) {
        mCaption = ensureNullIfEmpty(caption);
    }
    
    public String getCaption() {
        return mCaption;
    }
    
    public void setAuthor(String author) {
        mAuthor = ensureNullIfEmpty(author);
    }
    
    public String getAuthor() {
        return mAuthor;
    }
    
    public void setLicense(int license) {
        mLicense = license;
    }
    
    public int getLicense() {
        return mLicense;
    }
    
    public void setUrl(String url) {
        mUrl = ensureNullIfEmpty(url);
    }
    
    public String getUrl() {
        return mUrl;
    }
}