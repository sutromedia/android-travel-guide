package com.sutromedia.android.lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.params.HttpClientParams;

import com.sutromedia.android.lib.diagnostic.Report;
import com.sutromedia.android.lib.hash.Md5Hash;
import com.sutromedia.android.lib.http.Server;
import com.sutromedia.android.lib.util.Timer;

public class GuideComment extends DbUtil {

    public String checkUpdate(
        final Context context,
        final GuideDatabase guide,
        final String hash,
        final String appId) throws Exception {

        AndroidHttpClient connection = null;
        File commentDatabase = new File(context.getCacheDir(), "comments.sqlite3");
        try {
            if (commentDatabase.exists()) {
                commentDatabase.delete();
            }

            //Figure out where to load the database from
            String url = String.format(
                "http://www.sutromedia.com/published/comments/%s.sqlite3",
                appId);

            connection = AndroidHttpClient.newInstance("SutroMedia");
            HttpClientParams.setRedirecting(connection.getParams(), true);
            Server.getFile(connection, url, commentDatabase);

            //Downloaded => figure out if it's different
            String md5 = Md5Hash.calculateHash(commentDatabase.getAbsolutePath());
            if (!md5.equals(hash)) {
                //Yep, it's different
                Report.v("There is a new comment database");
                transferComments(commentDatabase, guide);
            } else {
                Report.v("Comment database has not changed");
            }
            return md5;
        } finally {
            if (connection != null) {
                connection.close();
            }
            commentDatabase.delete();
        }
    }

    private void transferComments(
        final File database,
        final GuideDatabase guide) {

        SQLiteDatabase sqlite = null;
        SQLiteDatabase destination = null;
        Cursor srcCursor = null;
        try {
            sqlite = SQLiteDatabase.openDatabase(
                database.getAbsolutePath(),
                null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY);
            Report.v("Comments database opened");

            srcCursor = sqlite.query(
                "comments",
                new String[] {
                    "created",
                    "comment",
                    "entryid",
                    "subentry_name",
                    "commenter_alias",
                    "response",
                    "response_date",
                    "responder_name",
                },
                null,     //selection
                null,     //selection args
                null,     //group by
                null,     //having
                "rowid");  //order);

            if (srcCursor.moveToFirst()) {
                destination = guide.getWritableDatabase();
                destination.beginTransaction();
                destination.execSQL("DELETE from comments");
                InsertHelper inserter = new InsertHelper(destination, "comments");

                Timer timer = new Timer();
                ContentValues values = new ContentValues();
                int count = 0;
                do {
                    values.clear();
                    DatabaseUtils.cursorRowToContentValues(srcCursor, values);
                    inserter.insert(values);
                    count++;
                } while (srcCursor.moveToNext());
                destination.setTransactionSuccessful();
                Report.v("Transfered [" + count + "] records in " + timer.getElapsed() + "ms");
            }
        } finally {
            if (destination != null) {
                destination.endTransaction();
            }
            closeSilent(srcCursor);
            if (sqlite != null) {
                sqlite.close();
            }
        }

    }
}