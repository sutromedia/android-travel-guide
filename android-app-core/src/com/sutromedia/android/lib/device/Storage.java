package com.sutromedia.android.lib.device;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Environment;

public class Storage {
    public static List<File> getPrivateReadableFolder(final Context context) {
	ArrayList<File> folders = new ArrayList<File>(); 
	if ( isExternalStorageReadable()) {
	    folders.add(context.getExternalFilesDir(null));
	    folders.add(context.getExternalCacheDir());
	}
        folders.add(context.getFilesDir());	
        folders.add(context.getCacheDir());
	return folders;
    }

    public static List<File> getPrivateWriteableFolder(final Context context) {
	ArrayList<File> folders = new ArrayList<File>(); 
	if ( isExternalStorageWriteable()) {
	    folders.add(context.getExternalFilesDir(null));
	    folders.add(context.getExternalCacheDir());
	}
        folders.add(context.getFilesDir());	
        folders.add(context.getCacheDir());
	return folders;
    }

    public static List<File> getSharedWritableFolder(final Context context) {
	if ( isExternalStorageWriteable()) {
	    ArrayList<File> folders = new ArrayList<File>(); 
	    folders.add(context.getExternalFilesDir(null));
	    folders.add(context.getExternalCacheDir());
	    return folders;
	}
	return null;
    }

    public static File getPreferredSharedWritableFolder(final Context context) {
	List<File> folders = getSharedWritableFolder(context);
	if (folders != null && folders.size() > 0) {
	    return folders.get(0);
	}
	return null;
    }

    public static boolean isExternalStorageReadable() {
	String state = Environment.getExternalStorageState();
	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    return true;
	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    return true;
	}
	return false;
    }

    public static boolean isExternalStorageWriteable() {
	String state = Environment.getExternalStorageState();
	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    return true;
	}
	return false;
    }
}