package com.sutromedia.android.core;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import com.sutromedia.android.lib.app.INavigationEntry;
import com.sutromedia.android.lib.model.IEntryComment;
import com.sutromedia.android.lib.model.EntryComment;
import com.sutromedia.android.lib.model.Comment;

public class CommentActivity extends BaseActivity {

    private CommentWebClient mWebClient;
    private String           mEntryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.comment);
        WebView engine = (WebView) findViewById(R.comment.web_engine);
	engine.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebClient = new CommentWebClient(this);
        engine.setWebViewClient(mWebClient);

        checkInitialNavigation(savedInstanceState);
    }

    void loadCommentPage(final String id) {
        List<IEntryComment> comments = getApp().getComments();
        mEntryId = id;
        if (mEntryId != null) {
            comments = getApp().getComments(mEntryId);
        }
        WebView engine = (WebView) findViewById(R.comment.web_engine);
        mWebClient.loadDetailPage(engine, mEntryId, comments);
    }

    void synchronizeWithEntry(INavigationEntry entry) {
        if (entry instanceof NavigationComment) {
            NavigationComment comment = (NavigationComment)entry;
            String newId = comment.getId();
            if (newId == null && mEntryId == null) {
                //Old ID and new ID are the same => nothing to do
            } else if (newId == null || mEntryId == null) {
                //One of them is null => force redraw
                comment.onForceReloadView(this);
            } else if (!newId.equals(mEntryId)) {
                //Both are non null, but the values are different => force redraw
                comment.onForceReloadView(this);
            }
        }
    }

    public void onSubmitComment(View view) {
        startActivity(new Intent(this, SubmitCommentActivity.class));
    }
}