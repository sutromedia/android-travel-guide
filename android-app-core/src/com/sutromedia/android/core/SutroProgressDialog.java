package com.sutromedia.android.core;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

abstract class SutroProgressDialog extends AsyncTask<Void, Void, Void> {

    private ProgressDialog     mProgress;
    private Context            mContext;
    private String             mMessage;

    SutroProgressDialog(
        final Context context,
        final String message) {

        mContext = context;
        mMessage = message;
    }

    protected Context getContext() {
	return mContext;
    }

    protected MainApp getApp() {
        return (MainApp)getContext().getApplicationContext();
    }

    protected void onPreExecute() {
        mProgress = new ProgressDialog(mContext);
        mProgress.setMessage(mMessage);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    protected Void doInBackground(final Void... unused) {
        try {
            doWork();
        } catch (Throwable error) {
            error.printStackTrace();
            }
        return null;
    }

    protected void onPostExecute(final Void unused) {
        mProgress.dismiss();
        onActionCompleted();
    }

    protected abstract void doWork();
    protected abstract void onActionCompleted();
}

