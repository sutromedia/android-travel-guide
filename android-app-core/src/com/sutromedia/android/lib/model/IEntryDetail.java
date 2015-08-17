package com.sutromedia.android.lib.model;

import android.location.Location;
import java.util.List;

public interface IEntryDetail {
    String getId();
    String getSubtitle();
    String getDescription();
    Location getLocation();
    List<IGroup> getGroups();
    String getAddress();
    String getPhoneRaw();
    String getPhoneFormatted();
    String getWebUrl();
    String getAudioUrl();
    String getTwitter();
    CurrencyAmount getAudioPrice();
    String getPriceDetails();
    String getHours();
    String getTwitterAccount();
    String getVideoUrl();
    String getReservationUrl();
    String getFacebookUrl();
    String getFacebookAccount();
}