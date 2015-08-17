package com.sutromedia.android.lib.db;

import android.database.Cursor;

public class DbUtil {
    static protected void closeSilent(Cursor cursor) {
        if (cursor!=null) {
            cursor.close();
        }
    }
}