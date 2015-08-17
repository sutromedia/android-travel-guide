package com.sutromedia.android.lib.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.sutromedia.android.lib.model.*;

public class HelperImageLoader {

    public static Drawable getDrawableForSize(
        Activity activity,
	List<File> folders,
        String photoId,
	int size,
        int defaultImage) {

        Drawable drawable = getDrawableForSize(activity, folders, photoId, size);
	if (drawable == null) {
	    drawable = getDrawableFromResource(activity, defaultImage);
	}
        return drawable;
    }

    public static Drawable getDrawableForSize(
        Activity activity,
	List<File> folders,
        String photoId,
	int size) {

        Drawable drawable = getBestDownloadedDrawable(activity, folders, photoId, size);
        if (drawable == null) {
            drawable = getAssetDrawable(activity, photoId);
        }
        return drawable;
    }

    public static Drawable getBestDownloadedDrawable(
        Activity activity,
	List<File> folders,
        String photoId,
	int size) {

	PhotoDownload download = new PhotoDownload(photoId, size);
	if (folders != null) {
	    for (File folder : folders) {
		File image = new File(folder, download.getLocalFilename());
		if (image.exists() && image.isFile()) {
		    return new BitmapDrawable(activity.getResources(), image.getAbsolutePath());
		}
	    }
	}
	return null;
    }


    public static Drawable getBestDrawable(
        Activity activity,
	List<File> folders,
        String photoId,
	int size) {
        
        while (size>0) {
	    Drawable drawable = getDrawableForSize(activity, folders, photoId, size);
	    if (drawable != null) {
		return drawable;
	    }
	    size = size - 1;
	}
	return null;
    }

    public static Drawable getAssetDrawable(
        Activity activity,
        String photoId) {

        try {
            String assetFile = "images/" + photoId + ".jpg";
            InputStream stream = activity.getAssets().open(assetFile);
            return Drawable.createFromStream(stream, null);
        } catch (Exception error) {
        }
	return null;
    }

    public static Drawable getAssetDrawable(
        Activity activity,
        String photoId,
        int defaultImage) {

	Drawable drawable = getAssetDrawable(activity, photoId);
	if (drawable == null) {
	    drawable = getDrawableFromResource(activity, defaultImage);
	}
	return drawable;
    }
        
    public static Drawable getDrawableFromResource(Activity activity, int id) {
	if (id>0) {
	    return activity.getResources().getDrawable(id);
	}
	return null;
    }

    public static void setupImage(
        Activity activity,
	List<File> folders,
        ImageView view, 
        IEntrySummary entry,
        int defaultIconId,
	int size) {
        
        Drawable drawable = getBestDrawable(activity, folders, entry.getIconPhoto(), size);
	if (drawable == null) {
	    drawable = getDrawableFromResource(activity, defaultIconId);
	}
        view.setImageDrawable(drawable);
    }
    
    public static void setupIcon(
        Activity activity,
	List<File> folders,
        ImageView view, 
        IEntrySummary entry,
        int defaultIconId) {
        
        ArrayList<String> possibleIcons = new ArrayList<String>();
        possibleIcons.add("images/" + entry.getIconPhoto() + "_x100.jpg");
        possibleIcons.add("images/" + entry.getIconPhoto() + "-icon.jpg");
        
        for (String candidate : possibleIcons) {
            if (loadAssetImage(view, candidate)) {
                //we are done, we found it
                return;
            }
        }

	Drawable drawable = getDrawableForSize(
            activity, 
	    folders, 
	    entry.getIconPhoto(), 
	    PhotoSize.SMALL, 
	    defaultIconId);
        view.setImageDrawable(drawable);
    }
    
    
    private static void loadAssetImage(
        ImageView view, 
        String filename,
        int defaultIconId) {
        
        InputStream input = null;
        try {
            input = view.getContext().getAssets().open(filename);
            Bitmap icon = BitmapFactory.decodeStream(input);
            view.setImageBitmap(icon);
            input.close();
        } catch (Exception error) {
            view.setImageResource(defaultIconId);
        } finally {
            silentClose(input);
        }
    }

    private static boolean loadAssetImage(
        ImageView view, 
        String filename) {
        
        InputStream input = null;
        try {
            input = view.getContext().getAssets().open(filename);
            Bitmap icon = BitmapFactory.decodeStream(input);
            view.setImageBitmap(icon);
            input.close();
            return true;
        } catch (Exception error) {
        } finally {
            silentClose(input);
        }
        return false;
    }
    
    private static void silentClose(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception error) {
            //should never happen
        } finally {
        }
    }    
}

