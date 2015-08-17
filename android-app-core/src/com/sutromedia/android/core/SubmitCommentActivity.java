package com.sutromedia.android.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import com.sutromedia.android.lib.model.IEntryComment;
import com.sutromedia.android.lib.model.EntryComment;
import com.sutromedia.android.lib.model.Comment;
import com.sutromedia.android.lib.http.Server;

public class SubmitCommentActivity extends Activity {

    private String mAppId;
    private String mEntryId;
    private String mEntryName;
    private String mEmail;
    private String mAlias;
    private String mMessage;

    class SubmitCommentProgress extends SutroProgressDialog {
        SubmitCommentProgress(
            final Context context,
            final String message) {

            super(context, message);
        }

        protected void doWork() {
            AndroidHttpClient client = null;
            try {
                client = AndroidHttpClient.newInstance("SutroMedia");
                Server.postComment(
                    client,
                    "http://sutroproject.com/contact",
                    SubmitCommentActivity.this.mAppId,
                    SubmitCommentActivity.this.mEntryId,
                    SubmitCommentActivity.this.mEntryName,
                    SubmitCommentActivity.this.mEmail,
                    SubmitCommentActivity.this.mAlias,
                    SubmitCommentActivity.this.mMessage);
                Thread.sleep(2000);
            } catch (Throwable error) {
                error.printStackTrace();
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }

        protected void onActionCompleted() {
            Toast toast = Toast.makeText(
                SubmitCommentActivity.this,
                "Thanks for submitting your comment.",
                Toast.LENGTH_SHORT);
            toast.show();
            SubmitCommentActivity.this.finish();
        }
    }


    private CommentWebClient mWebClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.submit_comment);
        setText(R.comment.alias, getApp().getUserAlias());
        setText(R.comment.email, getApp().getUserEmail());
    }

    public void onPause() {
        super.onPause();
        getApp().setUserInformation(
            getTextFromView(R.comment.alias),
            getTextFromView(R.comment.email));
    }

    public void onCancelClicked(View view) {
        finish();
    }

    public void onSubmitClicked(View view) {
        mAlias = getTextFromView(R.comment.alias);
        mEmail = getTextFromView(R.comment.email);
        mMessage = getTextFromView(R.comment.text);
        mAppId = getApp().getAppId();
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        if (extra != null) {
            mEntryId = extra.getString(NavigationBase.KEY_DATA_GENERIC);
        }

        if (mEntryId != null) {
            mEntryName = getApp().getEntry(mEntryId).getName();
        }

        SubmitCommentProgress progress = new SubmitCommentProgress(
            this,
            "Submitting comment.\nPlease wait...");
        progress.execute();
    }

    private void setText(int id, String text) {
        EditText edit = (EditText)findViewById(id);
        edit.setText(text);
    }

    private String getTextFromView(int id) {
        EditText edit = (EditText)findViewById(id);
        if (edit.getText() != null) {
            return edit.getText().toString();
        }
        return null;
    }

    protected MainApp getApp() {
        return (MainApp)getApplication();
    }
}