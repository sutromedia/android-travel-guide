package com.sutromedia.android.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.html.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.view.*;
import com.sutromedia.android.lib.model.EntryFilter.FilterType;
import static com.sutromedia.android.lib.diagnostic.Tag.*;

public class GuideWebClient extends WebViewClient {

    private BaseActivity     mActivity;
    private IEntryDetail     mDetails;

    public GuideWebClient(BaseActivity activity) {
        mActivity = activity;
    }

    public boolean shouldOverrideUrlLoading(WebView view,  String url) {
        WebLink link = WebLink.parse(url);
        String errorMessage = null;
	Debug.v("Processing URL=" + url);
        if (link!=null) {
	    Debug.v("Clicked on link [" + link.getType()  + "," + link.getData()+ "]");
            switch (link.getType()) {
                case EntryDetail:
                    mActivity.onReceiveEntry(new NavigationDetailWeb(link.getData()));
                    return true;
                
                case Category:
                    EntryFilter filter = new EntryFilter(
                        FilterType.CATEGORY, 
                        Collections.singleton(link.getData()));
                    mActivity.getApp().setFilter(filter);
                    mActivity.onReceiveEntry(new NavigationMainList());
                    return true;
                    
                case Map:
                    errorMessage = mActivity.getResources().getString(R.string.error_map_no_connection);
                    if (mActivity.checkOnLineAndDisplayMessage(errorMessage)) {
                        mActivity.onReceiveEntry(new NavigationMap(link.getData()));
                    }
                    return true;                    

                case Phone:
                    mActivity.makePhoneCall(link.getData(), mDetails.getPhoneFormatted());
                    return true;                    

	        case Comment:
                    mActivity.onReceiveEntry(new NavigationComment(link.getData()));
                    return true;
            
	        case CommentForm:
		{
		    Intent intent = new Intent(mActivity, SubmitCommentActivity.class);
		    Bundle extras = new Bundle();
		    extras.putString(NavigationBase.KEY_DATA_GENERIC, mDetails.getId());
		    intent.putExtras(extras);
		    mActivity.startActivity(intent);
		    return true;
		}
		
	        case YouTube:
		    startYouTube(link.getData());
		    return true;

                case Web:
                    errorMessage = mActivity.getResources().getString(R.string.error_web_no_connection);
                    if (mActivity.checkOnLineAndDisplayMessage(errorMessage)) {
                        mActivity.onReceiveEntry(new NavigationWeb(link.getData()));
                    }
                    return true;

                case WebDocument:
		    launchViewer(link);
                    return true;

                case ExternalWeb:
		    startExternalWeb(url);
                    return true;
            }
        }
        return false;
    }

    private void startYouTube(String videoId) {
	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
	mActivity.startActivity(i);
    }

    private void launchViewer(WebLink link) {
        DocumentDownloader progress = new DocumentDownloader(
            mActivity,
            "Retrieving document.\nPlease wait...",
	    link);
        progress.execute();
    }

    private void startExternalWeb(String url) {
	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	mActivity.startActivity(i);
    }
   
    public void loadDetailPageForEntry(
        WebView view, 
        String entryId) {

        try {
            mDetails = mActivity.getEntryDetails(entryId);
            List<IEntryComment> comments = mActivity.getEntryComments(entryId);
            loadDetailPageForEntry(view, mDetails, comments);
        } catch (DataException error) {
            Report.logUnexpectedException("Unable to load detail page for entry [" + entryId + "]", error);
        }
    }
    
    public void loadDetailPageForEntry(
	WebView view, 
	IEntryDetail entry,
	List<IEntryComment> comments) {

        if (entry.getDescription() != null) {
            HtmlTemplate template = new HtmlTemplate(getHtmlTemplate());
            template.setAttribute("entry", entry);
            template.setGroups(entry.getGroups());
            if (mActivity.useComments()) {
                template.setComments(comments);
                template.useComments(true);
            }
            String result = template.getResult();
            //Log.v(TAG, "HTML for this page is :" + result);
            loadIntoControl(view, result);
            view.scrollTo (0, 0);
        }
    }
    
    private void loadIntoControl(WebView view, String data) {
        view.loadDataWithBaseURL(
            "sutromedia://unecessary",
            data, 
            "text/html",
            "UTF-8", 
            null);
    }
    
    public String getHtmlTemplate() {  
        String template = "<html><body>Internal error: unable to load html/view_detail.html template</body></html>";
        try {
            BaseActivity activity = (BaseActivity)mActivity;
            template = activity.loadAssetAsString("html/view_detail.html");
        } catch (IOException error) {
        }
        return template;
    }    
}
