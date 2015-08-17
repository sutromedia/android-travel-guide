package com.sutromedia.android.core;

import android.app.Activity;
import android.webkit.*;

import java.io.IOException;

import java.util.List;

import com.sutromedia.android.lib.html.*;
import com.sutromedia.android.lib.model.*;
import com.sutromedia.android.lib.view.*;

public class CommentWebClient extends WebViewClient {

    private BaseActivity     mActivity;

    public CommentWebClient(BaseActivity activity) {
        mActivity = activity;
    }

    public boolean shouldOverrideUrlLoading(WebView view,  String url) {
        WebLink link = WebLink.parse(url);
        String errorMessage = null;
        if (link!=null && link.getType() == WebLink.LinkType.EntryDetail) {
            mActivity.onReceiveEntry(new NavigationDetailWeb(link.getData()));
            return true;

        }
        return false;
    }

    public void loadDetailPage(
                               final WebView view,
                               final String entryId,
                               final List<IEntryComment> comments) {

        HtmlTemplate template = new HtmlTemplate(getHtmlTemplate());
        template.setAttribute("comments", comments);
        if (entryId == null) {
            template.setAttribute("showEntryName", true);
        }

        String result = template.getResult();
        loadIntoControl(view, result);
        view.scrollTo (0, 0);
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
        String template = "<html><body>Internal error: unable to load html/comment_all.html template</body></html>";
        try {
            BaseActivity activity = (BaseActivity)mActivity;
            template = activity.loadAssetAsString("html/comment_all.html");
        } catch (IOException error) {
        }
        return template;
    }
}
