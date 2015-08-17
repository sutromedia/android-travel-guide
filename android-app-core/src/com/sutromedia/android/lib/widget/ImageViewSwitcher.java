package com.sutromedia.android.lib.widget;

import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ViewAnimator;

public class ImageViewSwitcher 
    extends ViewAnimator 
{

   public interface IViewConfigure {
        void onSetupView(View view, int viewId);
    }

    private int         mImageViewId = -1;
    private int         mFitWidthLayout = -1;
    private int         mFitHeightLayout = -1;
    
    public ImageViewSwitcher(Context context)
    {
        super(context);
    }
    
    public ImageViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLayouts(int fitWidth, int fitHeight) {
        mFitWidthLayout = fitWidth;
        mFitHeightLayout = fitHeight;
        setupAllViews();
    }
            
    public void setImageId(int resid) {
        mImageViewId = resid;
    }

    public void refresh() {
        configureView(getCurrentView(), getViewIdForIndex(getDisplayedChild()));
    }

    public void setImageDrawable(Drawable drawable) {
        boolean landscapeImage = true;
	if (drawable !=null) {
	    landscapeImage = drawable.getIntrinsicHeight() < drawable.getIntrinsicWidth();
	}
        int newViewIndex = getNextViewIndex(landscapeImage);
        AutoFitImageView image = getImageForViewIndex(newViewIndex);
        image.setImageDrawable(drawable);

        //Setup the next view
        configureView(getChildAt(newViewIndex), getViewIdForIndex(newViewIndex));
        setDisplayedChild(newViewIndex);
    }
    
    private AutoFitImageView getImageForViewIndex(int index) {
        View nextView = getChildAt(index);
        AutoFitImageView imageView = null;
        if (mImageViewId!=-1) {
            View lookupImage = nextView.findViewById(mImageViewId);
            if (lookupImage!=null && AutoFitImageView.class.isInstance(lookupImage)) {
                imageView = (AutoFitImageView)lookupImage;
            } else if (AutoFitImageView.class.isInstance(nextView)){
                imageView = (AutoFitImageView)nextView;
            }
        }
        return imageView;
    }
    
    public int getNextViewIndex(boolean landscapeImage) {
        //figure out what the current orientation is
        boolean hasSpaceAbove = true;
        switch (getScreenOrientation()) {
            case Configuration.ORIENTATION_LANDSCAPE:
                hasSpaceAbove = false;
                break;

            case Configuration.ORIENTATION_PORTRAIT:
                hasSpaceAbove = landscapeImage;
                break;

            case Configuration.ORIENTATION_SQUARE:
                hasSpaceAbove = false;
                break;
        }
        
        int currentSelection = getDisplayedChild() / 2;
        int nextSelection = (currentSelection) == 0 ? 2 : 0;
        if (!hasSpaceAbove) {
            nextSelection += 1;
        }
        return nextSelection;
    }
    
    public View makeView(boolean hasSpaceAbove) {
        int layout = hasSpaceAbove ? mFitWidthLayout : mFitHeightLayout;
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View view = null;
        if (layout>0) {
            view = inflater.inflate(layout, this, false);
        } else {
            AutoFitImageView image = new AutoFitImageView(getContext());
            //image.setBackgroundColor(0xFF000000);
            //image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            view = image;
        }
        return view;
    }

    private void setupAllViews() {
        removeAllViews();
        obtainView(true);
        obtainView(false);
        obtainView(true);
        obtainView(false);
    }
    
    private View obtainView(boolean hasSpaceAbove) {
        View child = makeView(hasSpaceAbove);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }
        addView(child, lp);
        return child;
    }
    
    private int getScreenOrientation() {
        return getResources().getConfiguration().orientation;
    }    

    void configureView(View view, int viewId) {
        if (IViewConfigure.class.isInstance(getContext())) {
            IViewConfigure configure = (IViewConfigure)getContext();
            configure.onSetupView(view, viewId);
        }
    }
    
    int getViewIdForIndex(int index) {
        if (index==0 || index==2) {
            return mFitWidthLayout;
        } else {
            return mFitHeightLayout;
        }
    }
}
