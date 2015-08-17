package com.sutromedia.android.lib.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sutromedia.android.lib.model.PhotoDownload;
import com.sutromedia.android.lib.device.Storage;
import com.sutromedia.android.lib.diagnostic.Debug;
import com.sutromedia.android.lib.diagnostic.Report;

public class PhotoResolver {

    interface IResolver {
	Drawable resolve(Context context, PhotoDownload photo);
        void printDebug(PhotoDownload photo);
    }

    private List<File>            mFolders = new ArrayList<File>();
    private List<IResolver>  mResolvers = new ArrayList<IResolver>(); 

    public PhotoResolver(final Context context) {
	mFolders = Storage.getPrivateReadableFolder(context);
	for (File folder : mFolders) {
	    mResolvers.add(new FSResolver(folder));
	}
	mResolvers.add(new AssetResolver());
    }
    
    protected void addResolver(IResolver resolver) {
	mResolvers.add(resolver);
    }

    public Drawable getBestDrawable(final Context context, final PhotoDownload download) {
	for (IResolver resolver : mResolvers) {
	    Drawable drawable = resolver.resolve(context, download);
	    if (drawable != null) {
		resolver.printDebug(download);
		return drawable;
	    }
	}
	Report.handleUnexpected("Unable to find suitable Drawable for photoId=" + download.getId());
	return null;
    }

    class FSResolver implements IResolver {
	File   mRoot;

	FSResolver(File root) {
	    mRoot = root;
	}

	public Drawable resolve(Context context, PhotoDownload photo) {
	    File image = new File(mRoot, photo.getLocalFilename());
	    if (image.exists() && image.isFile()) {
		return loadFromFile(context, image);
	    }
	    //The image was not found in this folder
	    return null;
	}

	public void printDebug(PhotoDownload photo) {
	    File image = new File(mRoot, photo.getLocalFilename());
	    Debug.v("Resolved image [" + photo.getId() + "] as [" + image.toString());
	}

	private Drawable loadFromFile(final Context context, final File filename) {
	    try {
		BitmapDrawable drawable = new BitmapDrawable(context.getResources(), filename.getAbsolutePath());
		if (drawable.getBitmap() == null) {
		    throw new Exception("Unable to decode bitmap [" + filename + "]");
		}
	        return drawable;
	    } catch (Exception error) {
		//This function is called only if the file exists. If there is an error, we want 
		//to know about it.
		Report.logUnexpectedException(error);
	    }
	    return null;
	} 
    }

    class AssetResolver implements IResolver {
	public Drawable resolve(Context context, PhotoDownload photo) {
	    try {
		String assetFile = "images/" + photo.getId() + ".jpg";
		InputStream stream = context.getAssets().open(assetFile);
		return Drawable.createFromStream(stream, null);
	    } catch (Exception error) {
	    }
	    return null;
	}

	public void printDebug(PhotoDownload photo) {
	    Report.v("Resolved image [" + photo.getId() + "] as application asset");
	}

    }

    class DefaultResourceImage implements IResolver {
	private int mId;

	DefaultResourceImage(int id) {
	    mId = id;
	}

	public Drawable resolve(Context context, PhotoDownload photo) {
	    try {
		return context.getResources().getDrawable(mId);
	    } catch (Exception error) {
		//If something goes wrong, we want to know about it
		Report.logUnexpectedException(error);
	    }
	    return null;
	}

	public void printDebug(PhotoDownload photo) {
	    Report.v("Unable [" + photo.getId() + "]. Using default resource");
	}

    }
}