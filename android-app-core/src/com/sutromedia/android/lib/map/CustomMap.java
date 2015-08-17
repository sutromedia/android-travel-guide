package com.sutromedia.android.lib.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.google.android.maps.MapView;

public class CustomMap extends MapView {
    private long mLastTouchTime = -1;

    public CustomMap(Context c, AttributeSet attrs) {
	super(c, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
	if (ev.getAction() == MotionEvent.ACTION_DOWN) {
	    long thisTime = System.currentTimeMillis();
	    if (thisTime - mLastTouchTime < 250) {
		this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
		mLastTouchTime = -1;
	    } else {
		mLastTouchTime = thisTime;
	    }
	}
	return super.onInterceptTouchEvent(ev);
    }
}
