package com.sutromedia.android.lib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.sql.SQLException;

import com.sutromedia.android.lib.diagnostic.Report;

public class GuideDatabase extends SQLiteOpenHelper {

    private Context             mContext;
    private String              mDatabaseName;
    private String              mAssetName;
    private SQLiteDatabase      mDatabase;

    public GuideDatabase(
        Context context,
        String assetName,
        String databaseName) {

    	super(context, databaseName, null, 1);
        mContext = context;
        mAssetName = assetName;
        mDatabaseName = databaseName;
    }

    public void createDataBase(
        final int lastSavedVersion,
        final int appCode) throws IOException, SQLException {
        prepareForUpgradeIfNeeded(lastSavedVersion, appCode);
    	if(!databaseExists()) {
            copyDataBase();
    	}
        openDataBase();
    }

    /**
     * Copies your database from your local assets-folder to the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
	Report.v("About to copy database from assets");
        //Make sure the paths are created correctly:
        // at installation, the DB path does not exist
        ensureCreateDbPaths();

        Report.v("Finding ASSET for DB");
    	InputStream myInput = mContext.getAssets().open(mAssetName);
        Report.v("Opened ASSET InputStream for DB");
    	FileOutputStream myOutput = new FileOutputStream(getDatabaseAbsolutePath());
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}

    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
        Report.v("Database created at " + getDatabaseAbsolutePath());
    }

    public void openDataBase() throws SQLException{
    	mDatabase = SQLiteDatabase.openDatabase(
            getDatabaseAbsolutePath(),
            null,
            SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READWRITE);
        Report.v("Database opened");

        String createViewGroupMembership =
            "CREATE VIEW IF NOT EXISTS "
            + "  view_group_membership AS "
            + "  SELECT M.entryid, E.name AS entry_name, G.name AS group_name, G.rowid as group_id "
            + "  FROM entry_groups M, groups G, entries E "
            + "  WHERE G.rowid=M.groupid AND E.rowid=M.entryid";

        mDatabase.execSQL(createViewGroupMembership);
        Report.v("Creating view_group_membership");

        String createPhotoCollection =
            "CREATE VIEW IF NOT EXISTS "
            + "  view_photos AS "
            + " SELECT EP.entryid, EP.photoid, EP.slideshow_order, E.name, P.author, P.license, P.url, P.caption "
            + " FROM entry_photos EP, entries E, photos P "
            + " WHERE (EP.entryid=E.rowid) AND (EP.photoid=P.rowid)";

        mDatabase.execSQL(createPhotoCollection);
        Report.v("Creating view_photos");


        mDatabase.execSQL("DROP VIEW IF EXISTS view_comments");
        Report.v("Dropping view_comments");

        String createComments =
            "CREATE VIEW IF NOT EXISTS  "
            + "  view_comments AS "
            + "  SELECT  "
            + "    C.rowid AS comment_order, "
            + "    C.created,"
            + "    C.comment,"
            + "    C.entryid,"
            + "    C.subentry_name, "
            + "    C.commenter_alias,"
            + "    C.response, "
            + "    C.response_date, "
            + "    C.responder_name, "
            + "    E.name, "
            + "    E.icon_photo_id "
            + "  FROM comments C LEFT JOIN entries E "
            + "  ON C.entryid=E.rowid ";

        mDatabase.execSQL(createComments);
        Report.v("Creating view_comments");
    }

    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }

        super.close();
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private boolean databaseExists() {
    	SQLiteDatabase database = null;
        boolean hasDatabase = false;
    	try {
            database = SQLiteDatabase.openDatabase(
                getDatabaseAbsolutePath(),
                null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY);

            hasDatabase = true;
    	} catch(SQLiteException e) {
            //database does't exist yet.
            Report.v("Database does not exists: missing file " + getDatabaseAbsolutePath());
    	} finally {
            if (database != null) {
                database.close();
            }
        }
    	return hasDatabase;
    }

    private void ensureCreateDbPaths() {
        File dbFolder = mContext.getDatabasePath(mDatabaseName).getParentFile();
        Report.v("Check if DB folder exists: " + dbFolder);
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
            Report.v("Created DB folder: " + dbFolder);
        }
    }

    private void prepareForUpgradeIfNeeded(final int lastSavedCode, final int appCode) {
        Report.v("Checking if database needs to be upgraded.");
        Report.v("Last saved code = " + lastSavedCode);
        Report.v("Application code = " + appCode);
        boolean needUpdate = (appCode != lastSavedCode);
        if (needUpdate) {
            File database = new File(getDatabaseAbsolutePath());
            if (database.exists()) {
                Report.v("About to remove existing database (new version available): " + database.getAbsolutePath());
                boolean removed = database.delete();
                if (!removed) {
                    Report.v("Unable to remove database " + database.getAbsolutePath());
                }
            }
        }
    }
    
    public String getDatabaseAbsolutePath() {
        File dbLocation = mContext.getDatabasePath(mDatabaseName);
        return dbLocation.getAbsolutePath();
    }
}
