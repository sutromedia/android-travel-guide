package com.sutromedia.android.core;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.MotionEvent;

public class DialogActivity extends Activity {

    public boolean onCreateOptionsMenu(Menu menu) {
	onBackPressed();
        return true;
    }    

    public boolean onTouchEvent (MotionEvent event) {
	final int action = event.getAction();  
	switch (action & MotionEvent.ACTION_MASK) {  
	    case MotionEvent.ACTION_DOWN: {  
		if (isOutOfBounds(event)) {
		    onBackPressed();  
		    return true;
		}
	    }  
	}
  	return super.onTouchEvent(event);  
    }  
   
    private boolean isOutOfBounds(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.getWindowTouchSlop();
        final View decorView = getWindow().getDecorView();
        return (x < -slop) || (y < -slop)
	    || (x > (decorView.getWidth()+slop))
	    || (y > (decorView.getHeight()+slop));
    }

    protected MainApp getApp() {
        return (MainApp)getApplication();
    }

}