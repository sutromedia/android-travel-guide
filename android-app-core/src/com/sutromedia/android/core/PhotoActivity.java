package com.sutromedia.android.core;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.params.HttpClientParams;

import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.widget.AutoFitImageView;
import com.sutromedia.android.lib.widget.ImageViewSwitcher;
import com.sutromedia.android.lib.widget.ImageViewSwitcher.IViewConfigure;

public class PhotoActivity 
    extends BaseActivity 
    implements ImageViewSwitcher.IViewConfigure,
               DownloadFilesTask.IDownloadNotification {
    
    private static final int    SLIDESHOW_START_DELAY_MEDIUM = 1500;
    private static final int    SLIDESHOW_DELAY = 2000;
    private static final int    HIDE_CONTROLS_DELAY = 4000;
    
    private boolean            mShowSlideShowControls = true;
    private boolean            mInSlideShow = false;
    private String             mEntryIdSubset = null;
    private int                mCurrentImage = 0;
    private boolean            mMissingPhoto = false;
    private List<IPhoto>       mSelectedPhotos = null;
    private ImageViewSwitcher  mSwitcher;
    private GestureDetector    mFlingDetector;
    private final Handler      mHandler = new Handler();

    private DownloadFilesTask  mDownloader = null;
    
            
    private Runnable mSlideShowRunnable = new Runnable() {
        public void run()  {
	    handleFlingEvent(true);
            mHandler.postDelayed(this, SLIDESHOW_DELAY);
        }
    };    
    
    private Runnable mSlideHideControlsSoon = new Runnable() {
        public void run() {
            mShowSlideShowControls = false;
            mSwitcher.refresh();
        }
    };    
    
    class MyGestureDetector extends SimpleOnGestureListener {
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    
            float distance = e1.getX() - e2.getX();
            if (Math.abs(distance) > 50) {
                onEndSlideShow();
                handleFlingEvent(distance > 0 ? true : false);
                return true;
            }
            return false;
        }
        
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mInSlideShow) {
                onEndSlideShow();
            } else {
                mShowSlideShowControls = !mShowSlideShowControls;
                mSwitcher.refresh();
                if (mShowSlideShowControls) {
                    hideControlsSoon();
                }
            }
            return false;
        }
    }    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image);
        
        mSwitcher = (ImageViewSwitcher) findViewById(R.image.switcher);
        mSwitcher.setImageId(R.image.photo);
        mSwitcher.setLayouts(R.layout.image_view_outside, R.layout.image_view_inside);
        
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
        
        mFlingDetector = new GestureDetector(new MyGestureDetector());        
        checkInitialNavigation(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
	onEndSlideShow();
	stopDownload();
    }

    @Override
    public void onResume() {
        super.onResume();
	setupDownload();
	mMissingPhoto = false;
    }

    void synchronizeWithEntry(INavigationEntry entry) {
	if (entry instanceof NavigationPhoto) {
	    NavigationPhoto detail = (NavigationPhoto)entry;
	    if (mEntryIdSubset == null) {
		if (detail.getId() == null) {
		    //Both want "Display all" => nothing to do
		} else {
		    //One is null, the other is not => reload
		    entry.onForceReloadView(this);
		}
	    } else {
		if (mEntryIdSubset.equals(detail.getId())) {
		    //Nothing to do => the right entry is already being displayed
		} else {
		    //There is a new entry to display
		    entry.onForceReloadView(this);
		}
	    }
	} else {
	    Debug.v("#################################################");
	    Debug.v("Unexpected type");
	    Debug.printStackTrace();
	}
	
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig);
    }

    private void setupDownload(List<PhotoDownload> missing, int index) {
        index = index % mSelectedPhotos.size();
        if (index<0) {
            index += mSelectedPhotos.size();
        }
	IPhoto photo = getPhotoAtIndex(index);
	if (photo!=null) {
	    missing.add(new PhotoDownload(photo.getId(), getApp().getBestPictureSize()));
	}
    }
    /*
    private void setupDownloadAll() {
	stopDownload();
        mDownloader = new DownloadFilesTask(this);	
        List<PhotoDownload> missing = new ArrayList<PhotoDownload>();

	for (IPhoto photo : mSelectedPhotos) {
	    missing.add(new PhotoDownload(photo.getId(), getApp().getBestPictureSize()));
	}
    }
    */

    private void setupDownload() {
	stopDownload();
	int pipelineSize = 5;
        mDownloader = new DownloadFilesTask(this);	
        List<PhotoDownload> missing = new ArrayList<PhotoDownload>();
	setupDownload(missing, mCurrentImage);
	for (int i=0;i<pipelineSize;i++) {
	    setupDownload(missing, mCurrentImage+i);
	    setupDownload(missing, mCurrentImage-i);
	}
        mDownloader.execute(missing);
    }

    private void stopDownload() {
        if (mDownloader!=null) {
            mDownloader.cancel(false);
            mDownloader = null;
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        onEndSlideShow();
	return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        onEndSlideShow();
        return super.onCreateOptionsMenu(menu);
    }    

    public boolean onOptionsItemSelected(MenuItem item) {        
        if (item.getItemId() == R.menu.photos) {
	    if (isSubsetOnEntry()) {
		onReceiveEntry(new NavigationPhoto());
	    }
	    return true;
	}
        return super.onOptionsItemSelected(item);
    }    

    public boolean onTouchEvent(MotionEvent event) {
        if (mFlingDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void doStartSlideShow(int startDelay) {
        mHandler.removeCallbacks(mSlideHideControlsSoon);
        mInSlideShow = true;
        mShowSlideShowControls = false;
        mSwitcher.refresh();
        mHandler.postDelayed(mSlideShowRunnable, startDelay);
    }

    public void onStartSlideShow(View view) {
        ImageView image = (ImageView)view;
        image.setImageResource(R.drawable.pause_button);
        doStartSlideShow(SLIDESHOW_START_DELAY_MEDIUM);
    }

    public void onEndSlideShow() {
        if (mInSlideShow) {
            mInSlideShow = false;
            mHandler.removeCallbacks(mSlideShowRunnable);
            mShowSlideShowControls = true;
            
            ImageView image = (ImageView)findViewById(R.image.play_slideshow);
            image.setImageResource(R.drawable.play_slideshow);
            hideControlsSoon();
            mSwitcher.refresh();
        }
    }
    
    public void hideControlsSoon() {
        mHandler.postDelayed(mSlideHideControlsSoon, HIDE_CONTROLS_DELAY);
    }

    protected void onMenuCustomize(Menu menu) {
	super.onMenuCustomize(menu);
	menu.removeItem(R.menu.sort);
    }
    
    void handleFlingEvent(boolean left) {
        if (left) {
            mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
            mCurrentImage += 1;
        } else {
            mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
            mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
            mCurrentImage -= 1;
        }
        
        mCurrentImage = mCurrentImage % mSelectedPhotos.size();
        if (mCurrentImage<0) {
            mCurrentImage += mSelectedPhotos.size();
        }
	setupDownload();
        loadImagePreview(getCurrentPhoto());
    }
    
    public void onSetupView(View view, int viewId) {
        final IPhoto photo = getCurrentPhoto();
        if (photo!=null) {            
        
            int backgroundId = 
                (viewId == R.layout.image_view_inside) 
                ? R.drawable.attrib_inside 
                : R.drawable.attrib_outside;
                
             
            Drawable background = getResources().getDrawable(backgroundId);
            background.setAlpha(155);
            view.findViewById(R.image.licenseGroup).setBackgroundDrawable(background);
            setVisibility(view, R.image.caption, !mInSlideShow && !isSubsetOnEntry());
            TextView caption = (TextView)view.findViewById(R.image.caption);
            String entryName = photo.getEntryName();
            if (entryName!=null) {
		entryName = entryName.replace(' ','\u00A0' );
                entryName += "\u00A0\u00A0\u25B6";
            }
            caption.setText(entryName);
	    caption.setSingleLine(true);
            caption.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onReceiveEntry(new NavigationDetailWeb(photo.getEntryId()));
                }
            });
            
            setVisibility(view, R.image.licenseGroup, !mInSlideShow);
            Integer[] icons = PhotoLicence.getIcons(photo);
            setImageView(view, R.image.license1, 0, icons);
            setImageView(view, R.image.license2, 1, icons);
            TextView owner = (TextView)view.findViewById(R.image.owner);
            if (icons.length > 0) {
                owner.setText(photo.getAuthor());
            } else {
                view.findViewById(R.image.licenseGroup).setVisibility(View.GONE);
            }
            
            String url = photo.getUrl();
            View licenceGroup = view.findViewById(R.image.licenseGroup);
            if (url != null && url.length()>0) {
                owner.setTextColor(Color.rgb(0x19,0x49,0x90));
                owner.setTypeface(null, Typeface.BOLD);
                
                licenceGroup.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        onReceiveEntry(new NavigationWeb(photo.getUrl()));
                    }
                });
            } else {
                owner.setTypeface(null, Typeface.NORMAL);
                owner.setTextColor(Color.WHITE);
                licenceGroup.setOnClickListener(null);
            }
            
            setupTouchOnPlayButton();
            setVisibility(R.image.play_slideshow, !mInSlideShow && mShowSlideShowControls);
	    setVisibility(R.image.loading, mMissingPhoto);
	    setVisibility(R.image.wait, mMissingPhoto && isOnline());
	    
	    
	    String missingTextTemplate = getString(mMissingPhoto && isOnline() ? R.string.missing_photo : R.string.missing_not_online);
	    String missingText = String.format(
		missingTextTemplate,
		mCurrentImage + 1,
		getImageCountInSet());
					       
	    setText(R.image.missing, missingText);
        }
    }

    private int getImageCountInSet() {
	return mSelectedPhotos.size();
    }
    
    private void setupTouchOnPlayButton() {
        View playButton = findViewById(R.image.play_slideshow);
        Rect expandedSize = new Rect();
        playButton.getHitRect(expandedSize);
        expandedSize.inset(30, 30);
        TouchDelegate delegate = new TouchDelegate(expandedSize, playButton);
        View parent = (View)playButton.getParent();
        parent.setTouchDelegate(delegate);
    }

    private void setVisibility(View view, int child, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        view.findViewById(child).setVisibility(visibility);
    }
    
    private void setVisibility(int child, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        findViewById(child).setVisibility(visibility);
    }
    
    private void setImageView(View view, int resourceId, int index, Integer[] licences) {
        ImageView image = (ImageView)view.findViewById(resourceId);
        if (index<licences.length) {
            setVisibility(view, resourceId, true);
            image.setImageResource(getLicenceBitmap(licences[index]));
        } else {
            setVisibility(view, resourceId, false);
            image.setImageDrawable(null);
        }
    }
    
    int getLicenceBitmap(int license) {
        switch (license) {
            case PhotoLicence.COPYRIGHT:
                return R.drawable.copyright;
                
            case PhotoLicence.ATTRIBUTION:
                return R.drawable.attribution;
                
            case PhotoLicence.SHARE_ALIKE:
                return R.drawable.share_alike;
                
            case PhotoLicence.NODERIVATIVE:
                return R.drawable.no_derivative;
                
            default:
                return 0;
        }
    }
    
    private void loadImagePreview(final IPhoto photo) {
        if (photo!=null) {
	    PhotoSlideShowResolver resolver = new PhotoSlideShowResolver(this);
	    Drawable drawable = resolver.getBestDrawable(
	        this, 
		new PhotoDownload(photo.getId(), getApp().getBestPictureSize()));
	    
	    mMissingPhoto = (drawable == null) ? true : false;
	    if (drawable == null) {
		HelperImageLoader.getDrawableFromResource(this, R.drawable.missing_photo);
	    }

            mSwitcher.setImageDrawable(drawable);
        } else {
            //Invalid photo => what should happen?
        }
    }

    void loadImagesForEntry(String entryId) {
        try {
            mEntryIdSubset = entryId;
            mCurrentImage = 0;
            mSelectedPhotos = getPhotosForEntry(entryId);
            loadImagePreview(getCurrentPhoto());
            doStartSlideShow(SLIDESHOW_START_DELAY_MEDIUM);
        } catch (Exception error) {
            showToastNotification("Internal error: unable to load photos");
        }
    }

    void loadImagesForAll() {
        try {
            mSelectedPhotos = getPhotos();            
            mEntryIdSubset = null;
            mCurrentImage = 0;
            loadImagePreview(getCurrentPhoto());
            doStartSlideShow(SLIDESHOW_START_DELAY_MEDIUM);
        } catch (Exception error) {
            showToastNotification("Internal error: unable to load photos");
        }
    }

    boolean isSubsetOnEntry() {
        return mEntryIdSubset!=null;
    }
    

    private IPhoto getCurrentPhoto() {
	return getPhotoAtIndex(mCurrentImage);
    }

    private IPhoto getPhotoAtIndex(int index) {
        if (mSelectedPhotos!=null && mSelectedPhotos.size()>0 && index < mSelectedPhotos.size()) {
            return mSelectedPhotos.get(index);
        }
        return null;
    }
    
    public void onReceiveDownloadedImage(PhotoDownload download) {
        IPhoto photo = getCurrentPhoto();
        if (photo != null && photo.getId().equals(download.getId())) {
	    Report.v("Notified that a high-res image for [" + download.getId() + "] was received");
            mSwitcher.setInAnimation(null);
            mSwitcher.setOutAnimation(null);
            loadImagePreview(photo);
        }
    }

    public void onAllImagesReceived() {
        mDownloader = null;
    }    
    
    public void onDownloadImagesCancelled() {
    }
}
