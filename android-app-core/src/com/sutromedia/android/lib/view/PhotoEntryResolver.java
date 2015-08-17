package com.sutromedia.android.lib.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.sutromedia.android.lib.model.PhotoDownload;

public class PhotoEntryResolver extends PhotoResolver {

    public PhotoEntryResolver(final Context context, final int defaultBitmapId) {
	super(context);
	addResolver(new DefaultResourceImage(defaultBitmapId));
    }

    public void setupImageForDetailsEntry(
        Context context,
        ImageView view, 
        String photoId,
        int defaultIconId,
	int size) {
	
	PhotoDownload download = new PhotoDownload(photoId, size);
        Drawable drawable = getBestDrawable(context, download);
	if (drawable != null) {
	    view.setImageDrawable(drawable);
	}
    }

}