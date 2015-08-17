package com.sutromedia.android.lib.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.app.*;

public final class ModelFactory extends DbUtil {

    static public List<IEntrySummary> getEntries(SQLiteOpenHelper guide) throws DataException {

        SQLiteDatabase database = guide.getReadableDatabase();
        ArrayList<IEntrySummary> all = new ArrayList<IEntrySummary>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "entries",
                new String[] { "_rowid_", "name" , "spatial_group_name", "price", "icon_photo_id", "latitude", "longitude" },
                null,     //selection
                null,     //selection args
                null,     //group by
                null,     //having
                "name");  //order);

            if (cursor.moveToFirst()) {
                do {
                    EntrySummary entry = new EntrySummary(
                        getLong(cursor, 0).toString(),
                        get(cursor, 1),
                        get(cursor, 2),
                        get(cursor, 3),
                        get(cursor, 4));

                    entry.setGpsPosition(get(cursor, 5), get(cursor, 6));
                    all.add(entry);
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return all;
    }

    static public IEntryDetail getEntryDetails(
        SQLiteOpenHelper guide,
        String itemId) throws DataException {

        String columns[] = new String[] {
            "subtitle",
            "description",
            "address",
            "latitude",
            "longitude",
            "phone",
            "formatted_phone",
            "website",
            "twitter_name",
            "audio_url",
            "audio_price",
            "pricedetails",
            "hours",
	    "twitter_username",
	    "make_a_reservation_url",
	    "video_url",
	    "facebook_profile_id",
	    "facebook_url"
        };

        SQLiteDatabase database = guide.getReadableDatabase();
        Cursor cursor = null;
        EntryDetail details = null;
        try {
            cursor = database.query(
                "entries",
                columns,
                "rowid = ?" ,                   //selection
                new String[] { itemId } ,       //selection args
                null,                           //group by
                null,                           //having
                "name");                        //order)

            if (cursor.moveToFirst()) {
                details = new EntryDetail(itemId);
                details.setSubtitle(get(cursor, 0));
                details.setDescription(get(cursor, 1));
                details.setAddress(get(cursor, 2));

                Double latitude = cursor.getDouble(3);
                Double longitude = cursor.getDouble(4);
                if (longitude!=null && latitude!=null) {
                    details.setLocation(longitude, latitude);
                }

                details.setPhoneRaw(get(cursor, 5));
                details.setPhoneFormatted(get(cursor, 6));
                details.setWebUrl(get(cursor, 7));
                details.setTwitter(get(cursor, 8));
                details.setAudioUrl(get(cursor, 9));
                details.setAudioPrice(get(cursor, 10));
                details.setPriceDetails(get(cursor, 11));
                details.setHours(get(cursor, 12));
		details.setTwitterAccount(get(cursor, 13));
		details.setReservationUrl(get(cursor, 14));
		details.setVideoUrl(get(cursor, 15));
		details.setFacebookAccount(get(cursor, 16));
		details.setFacebookUrl(get(cursor, 17));

                details.addGroups(getGroupsForEntry(guide, itemId));
                return details;
            }
        } finally {
            closeSilent(cursor);
        }

        return null;
    }

    static public List<IGroup> getGroupsForEntry(
        SQLiteOpenHelper guide,
        String entryId) throws DataException {

        SQLiteDatabase database = guide.getReadableDatabase();

        ArrayList<IGroup> all = new ArrayList<IGroup>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "view_group_membership",
                new String[] { "group_id", "group_name" },
                "entryid = ?" ,                   //selection
                new String[] {entryId},         //selection args
                null,                           //group by
                null,                           //having
                "group_name");                  //order);

            if (cursor.moveToFirst()) {
                do {
		    Group group = new Group(get(cursor, 0), get(cursor, 1));
                    all.add(group);
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return all;
    }

    static public List<IPhoto> getPhotos(SQLiteOpenHelper guide) throws DataException {
        return queryGetPhotos(guide, null, null);
    }

    static public List<IPhoto> getPhotos(
        SQLiteOpenHelper guide,
        String entryId) throws DataException {

        return queryGetPhotos(guide, "entryid = ?", new String[] { entryId });
    }

    static public List<IPhoto> queryGetPhotos(
        SQLiteOpenHelper guide,
        String filterExpression,
        String[] filterValue) throws DataException {

        String columns[] = new String[] {
            "photoid",
            "entryid",
            "name",
            "caption",
            "author",
            "url",
            "license",
            "slideshow_order"
        };

        SQLiteDatabase database = guide.getReadableDatabase();
        List<IPhoto> all = new ArrayList<IPhoto>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "view_photos",
                columns,
                filterExpression,               //selection
                filterValue,                    //selection args
                null,                           //group by
                null,                           //having
                "slideshow_order");             //order)

            if (cursor.moveToFirst()) {
                do {
                    Photo photo = new Photo(get(cursor, 0), get(cursor, 1), get(cursor, 2));
                    photo.setCaption(get(cursor, 3));
                    photo.setAuthor(get(cursor, 4));
                    photo.setUrl(get(cursor, 5));
                    photo.setLicense(getInteger(cursor, 6, 0));

                    all.add(photo);
                } while (cursor.moveToNext());
            }
            return all;
        } finally {
            closeSilent(cursor);
        }
    }

    static public GroupEntries getGroupEntries(SQLiteOpenHelper guide) throws DataException {
        SQLiteDatabase database = guide.getReadableDatabase();

        GroupEntries entries = new GroupEntries();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "entry_groups",
                new String[] { "groupid", "entryid" },
                null,     //selection
                null,     //selection args
                null,     //group by
                null,     //having
                null);  //order);

            if (cursor.moveToFirst()) {
                do {
                    entries.add(cursor.getString(0), cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return entries;
    }

    static public List<IGroup> getGroups(SQLiteOpenHelper guide) throws DataException {
        SQLiteDatabase database = guide.getReadableDatabase();
        ArrayList<IGroup> all = new ArrayList<IGroup>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "groups",
                new String[] { "_rowid_", "name", "intro_entry_id" },
                null,          //selection
                null,          //selection args
                null,          //group by
                null,          //having
                "name");   //order);

            if (cursor.moveToFirst()) {
                do {
                    all.add(new Group(cursor.getString(0), cursor.getString(1), get(cursor, 2)));
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return all;
    }


    static public Settings getSettings(SQLiteOpenHelper guide) throws DataException {
        SQLiteDatabase database = guide.getReadableDatabase();

        HashMap<String, String> all = new HashMap<String, String>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "app_properties",
                new String[] { "key", "value" },
                null,     //selection
                null,     //selection args
                null,     //group by
                null,     //having
                "key");  //order);

            if (cursor.moveToFirst()) {
                do {
                    all.put(cursor.getString(0), cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return new Settings(all);
    }

    static public ArrayList<String> getPhotosToDownload(
        SQLiteOpenHelper guide,
        int requestedPhotoSize) throws DataException {

        String columnName = PhotoSize.getColumnNameForSize(requestedPhotoSize);
	String filter = columnName + " is not null";
        SQLiteDatabase database = guide.getReadableDatabase();
        ArrayList<String> all = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "photos",
                new String[] { "_rowid_" },
                filter,                     //selection
                null,                       //selection args
                null,                       //group by
                null,                       //having
                null);                      //order);

            if (cursor.moveToFirst()) {
                do {
                    all.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return all;
    }

    static public ArrayList<String> getPhotosFoundInAssets(
        SQLiteOpenHelper guide) throws DataException {

	String filter = "(downloaded_320px_photo is not null) or (downloaded_768px_photo is not null)";
        SQLiteDatabase database = guide.getReadableDatabase();
        ArrayList<String> all = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "photos",
                new String[] { "_rowid_" },
                filter,                     //selection
                null,                       //selection args
                null,                       //group by
                null,                       //having
                null);                      //order);

            if (cursor.moveToFirst()) {
                do {
                    all.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }

        return all;
    }

    static public ArrayList<IEntryComment> getComments(
        SQLiteOpenHelper guide) throws DataException {

        SQLiteDatabase database = guide.getReadableDatabase();
        ArrayList<IEntryComment> all = new ArrayList<IEntryComment>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                "view_comments",
                new String[] {
                    "entryid", "name" , "icon_photo_id",
                    "comment", "created", "commenter_alias",
                    "response", "response_date", "responder_name" },
                null,     //selection
                null,     //selection args
                null,     //group by
                null,     //having
                "comment_order DESC");  //order);

            if (cursor.moveToFirst()) {
                do {
                    try {
                        EntryComment comment = new EntryComment(
                            get(cursor, 0),
                            get(cursor, 1),
                            get(cursor, 2));
                        comment.setComment(new Comment(get(cursor, 3), get(cursor, 4), get(cursor, 5)));
                        boolean hasAnswer = get(cursor,6).trim().length() > 0;
                        if (hasAnswer) {
                            comment.setAnswer(new Comment(get(cursor, 6), get(cursor, 7), get(cursor, 8)));
                        }
                        all.add(comment);
                    } catch (DataException error) {
                        //This comment is bad => ignore it...
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            closeSilent(cursor);
        }
        return all;
    }

    static private String get(Cursor cursor, int index) {
        String data = null;
        if (!cursor.isNull(index)) {
            data = cursor.getString(index);
            if (data.equals("null")) {
                data = null;
            }
        }
        return data;
    }

   static private Integer getInteger(Cursor cursor, int index, int defaultValue) {
        if (!cursor.isNull(index)) {
            return cursor.getInt(index);
        }
        return defaultValue;
    }

   static private Double getDouble(Cursor cursor, int index) {
        if (!cursor.isNull(index)) {
            return cursor.getDouble(index);
        }
        return null;
    }

   static private Long getLong(Cursor cursor, int index) {
        if (!cursor.isNull(index)) {
            return cursor.getLong(index);
        }
        return null;
    }
}