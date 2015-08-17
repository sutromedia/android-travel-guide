package com.sutromedia.android.lib.model;

import android.location.Location;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class EntryDetail extends ModelBase implements IEntryDetail {
    String              mId;
    String              mSubtitle;
    String              mDescription;
    String              mAddress;
    String              mPhoneFormatted;
    String              mPhoneRaw;
    String              mWebUrl;
    String              mAudioUrl;
    String              mTwitter;
    String              mTwitterAccount;
    String              mPriceDetails;
    String              mHours;
    String              mReservationUrl;
    String              mVideoUrl;
    String              mFacebookUrl;
    String              mFacebookAccount;
    
    CurrencyAmount      mAudioPrice;
    Location            mLocation;
    List<IGroup>        mGroups;
    
    public EntryDetail(String id) throws DataException {
        mId = ensureNotEmptyTrimmed(id, "Entry.id is required");
        mGroups = new ArrayList<IGroup>();
    }
    
    public String getId() {
        return mId;
    }
    
    public void setSubtitle(String subtitle) {
        mSubtitle = ensureNullIfEmpty(subtitle);
    }

    public String getSubtitle() {
        return mSubtitle;
    }
    
    public void setDescription(String description) {
        mDescription = ensureNullIfEmpty(description);
	if (mDescription!=null) {
	    mDescription = mDescription.replace('\u25BA','\u25B6' );
	}
    }
    
    public String getDescription() {
        return mDescription;
    }
    
    public void setLocation(double longitude, double latitude) {
        String provider = null;
        mLocation = new Location(provider);
        mLocation.setLongitude(longitude);
        mLocation.setLatitude(latitude);
    }

    public void resetLocation() {
        mLocation = null;
    }
    
    public Location getLocation() {
        return mLocation;
    }

    public void resetGroups() {
        mGroups.clear();
    }

    public void addGroups(List<IGroup> group) throws DataException {
        mGroups.addAll(group);
    }
    
    public void addGroup(IGroup group) throws DataException {
        mGroups.add(group);
    }
    
    public List<IGroup> getGroups() {
        return mGroups;
    }
    
    public void setAddress(String address) {
        mAddress = ensureNullIfEmpty(address);
    }
    
    public String getAddress() {
        return mAddress;
    }
    
    public void setPhoneRaw(String phone) {
        mPhoneRaw = ensureNullIfEmpty(phone);
    }
    
    public String getPhoneRaw() {
        return mPhoneRaw;
    }
    
    public void setPhoneFormatted(String phone) {
        mPhoneFormatted = ensureNullIfEmpty(phone);
    }
    
    public String getPhoneFormatted() {
        return mPhoneFormatted;
    }
    
    public void setWebUrl(String url) {
        mWebUrl = ensureNullIfEmpty(url);
    }
    
    public String getWebUrl() {
        return mWebUrl;
    }
    
    public void setAudioUrl(String url) {
        mAudioUrl = ensureNullIfEmpty(url);
    }

    public String getAudioUrl() {
        return mAudioUrl;
    }
    
    public void setTwitter(String twitter) {
        mTwitter = ensureNullIfEmpty(twitter);
    }
    
    public String getTwitter() {
        return mTwitter;
    }

    public void setAudioPrice(String amount) {
        mAudioPrice = new CurrencyAmount(amount);
    }
    
    public CurrencyAmount getAudioPrice() {
        return mAudioPrice;
    }
    
    public void setPriceDetails(String detail) {
        mPriceDetails = ensureNullIfEmpty(detail);
    }
    
    public String getPriceDetails() {
        return mPriceDetails;
    }
 
    public void setHours(String hours) {
        mHours = ensureNullIfEmpty(hours);
    }
    
    public String getHours() {
        return mHours;
    }

    public void setTwitterAccount(String account) {
	mTwitterAccount = ensureNullIfEmpty(account);
    }

    public String getTwitterAccount() {
	return mTwitterAccount;
    }

    public void setVideoUrl(String video) {
	mVideoUrl = ensureNullIfEmpty(video);
    }

    public String getVideoUrl() {
	return mVideoUrl;
    }

    public void setReservationUrl(String reservation) {
	mReservationUrl = ensureNullIfEmpty(reservation);
    }

    public String getReservationUrl() {
	return mReservationUrl;
    }

    public void setFacebookUrl(String url) {
	mFacebookUrl = ensureNullIfEmpty(url);
    }

    public String getFacebookUrl() {
	return mFacebookUrl;
    }

    public void setFacebookAccount(String account) {
	mFacebookAccount = ensureNullIfEmpty(account);
    }

    public String getFacebookAccount() {
	return mFacebookAccount;
    }
}