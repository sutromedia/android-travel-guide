package com.sutromedia.android.core;

import java.io.File;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.widget.Toast;

import com.sutromedia.android.lib.device.Storage;
import com.sutromedia.android.lib.http.Mime;
import com.sutromedia.android.lib.http.Server;
import com.sutromedia.android.lib.view.WebLink;
import com.sutromedia.android.lib.diagnostic.*;

class DocumentDownloader extends SutroProgressDialog {

    private WebLink  mLink;
    private File     mPath;
    private String   mMimeType;
    private String   mError;

    DocumentDownloader(
        final Context context,
	final String message,
	final WebLink link) {

	super(context, message);
	mLink = link;
	mMimeType = Mime.guessFromExtension(mLink.guessExtension());
	Debug.v("Guessed mime from extension [" + mLink.guessExtension() + "] is: " + mMimeType);
    }

    DocumentDownloader(
        final Context context,
	final String message,
	final WebLink link,
	final String mimeType) {

	super(context, message);
	mLink = link;
	mMimeType = mimeType;
    }

    protected File guessFilename() throws IOException {
	File sharedFolder = Storage.getPreferredSharedWritableFolder(getContext());
	Debug.v("Trying to write document to folder [" + sharedFolder + "]");
	if (sharedFolder != null) {
	    File destination = null;
	    String filename = mLink.guessFilename();
	    if (filename == null) {
		destination = File.createTempFile("sutro", ".download", sharedFolder);
	    } else {
		destination = new File(sharedFolder, filename);
	    }
	    return destination;
	}
	return null;
    }

    protected void doWork() {
	try {
	    File destination = guessFilename();
	    if (destination == null) {
		//Can't write to the SD card => bail
		mError = "Unable to write document to SD card.\n"
		       + "Please check that the SD card is properly mounted, and try again." ;
	    } else {
		if (!destination.exists()) {
		    if (getApp().isOnline()) {
			AndroidHttpClient http = getApp().getHttp();
			synchronized(http) {
			    Debug.v("Saving document to: " + destination);
			    Server.getFile(http, mLink.getData(), destination);
			    mPath = destination;
			}
		    } else {
			//Not online => show a message
			mError = "Unable to download document:\nNo internet connection found.";
		    }
		} else {
		    //The file was already downloaded => just show it
		    Debug.v("Document was already downloaded: " + destination);
		    mPath = destination;
		}
		Thread.sleep(1000);
	    }
	} catch (Throwable error) {
	    error.printStackTrace();
	}
    }

    protected void onActionCompleted() {
	try {
	    if (mPath != null) {
		Uri uri = Uri.fromFile(mPath);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		Debug.v("About to start intent URL [" + uri + "]");
		if (mMimeType != null) {
		    Debug.v("onDownloadStart MIME TYPE [" + mMimeType + "]");
		    intent.setDataAndType(uri, mMimeType);
		}
		getContext().startActivity(intent);
	    } else {
		if (mError != null) {
		    throw new RuntimeException(mError);
		} else {
		    throw new RuntimeException("Unable to view this document");
		}
	    }
	} catch (ActivityNotFoundException error) {
	    String filename = mLink.guessFilename();
	    showError(
	        "Unable to view this document.\n"
		+ "No application is registered to view the file:\n"
		+ filename);
	} catch (RuntimeException error) {
	    showError(error.getMessage());
	}
	if (getContext() instanceof WebActivity) {
	    WebActivity activity = (WebActivity) getContext();
	    activity.goAway();
	}
    }

    void showError(final String message) {
	Toast toast = Toast.makeText(
            getContext(),
	    message,
	    Toast.LENGTH_LONG);
	toast.show();
    }
}
