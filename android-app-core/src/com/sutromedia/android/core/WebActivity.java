package com.sutromedia.android.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import android.widget.FrameLayout.LayoutParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.google.android.maps.*;

import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.map.*;
import com.sutromedia.android.lib.diagnostic.*;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;

public class WebActivity extends BaseActivity {

    class InternalWebClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view,  String url) {
	    Debug.v("Callback - shouldOverrideUrlLoading:" + url);
            WebLink link = WebLink.parse(url);
            return false;
        }
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
	    Debug.v("Error while loading SSL page:" + error.toString());
	    handler.proceed(); // Ignore SSL certificate errors
	}
        public void onReceivedError(
            WebView view, 
            int errorCode, 
            String description, 
            String failingUrl) {

            String errorMessage = "Internal error. Missing resource";
            try {
                if (isOnline()) {
                    loadAssetAsString("html/view_error_loading.html");
                } else {
                    errorMessage = loadAssetAsString("html/view_no_connectivity.html");
                }
            } catch (IOException error) {
                //something wrong with the software => it should have found these assets
            }
            loadIntoControl(view, errorMessage);

        }
        
        private void loadIntoControl(WebView view, String data) {
            view.loadDataWithBaseURL(
                "sutromedia://unecessary",
                data, 
                "text/html",
                "UTF-8", 
                null);
        }
    }
    
       
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	Debug.v("Webctivity.onCreate()");
        setContentView(R.layout.webkit_standalone);
        WebView engine = (WebView) findViewById(R.id.web_engine);
        engine.setWebViewClient(new InternalWebClient());
        CookieSyncManager.createInstance(this);
        checkInitialNavigation(savedInstanceState);
    }
    
    public void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }

    public void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }

    public void loadWebPage(final String url) {
        WebView engine = (WebView) findViewById(R.id.web_engine);
        engine.getSettings().setJavaScriptEnabled(true);
        engine.getSettings().setBuiltInZoomControls(true);
        engine.getSettings().setLoadWithOverviewMode(true);
        engine.getSettings().setUseWideViewPort(true);

	engine.setDownloadListener(new DownloadListener() {
	    public void onDownloadStart(
	        String url, 
		String userAgent,
		String contentDisposition,
		String mimeType,
		long contentLength) {

		Debug.v("onDownloadStart URL [" + url + "]");
		Debug.v("onDownloadStart USER AGENT [" + userAgent + "]");
		Debug.v("onDownloadStart CONTENT DISPOSITION [" + contentDisposition + "]");
		Debug.v("onDownloadStart MIME TYPE [" + mimeType + "]");
		
		try {
		    DocumentDownloader downloader = new DocumentDownloader(
		        WebActivity.this,
		        "Retrieving document.\nPlease wait...",
			WebLink.parse(url),
			mimeType);

		    downloader.execute();
		} catch (Throwable error) {
		    error.printStackTrace();
		}
	    }
	    });
	Debug.v("Loading URL [" + url + "]");
        engine.loadUrl(url);
    }

    public void goAway() {
	getApp().popCurrentLocation();
	finish();
    }
}