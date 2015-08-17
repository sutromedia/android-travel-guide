package com.sutromedia.android.core;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.sutromedia.android.lib.diagnostic.*;

public class SplashActivity extends Activity {

    class SutroProgressDialog extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
        }

        protected Void doInBackground(final Void... unused) {
            try {
                Report.v("About to call getApp().ensureDatabase()");
                getApp().ensureDatabase();
                Report.v("Done with ensureDatabase");
                getApp().refreshComments();
                Report.v("Done with refreshComments");
            } catch (Throwable error) {
                Report.v("Error in ensureDatabase");
                error.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(final Void unused) {
            gotoMainScreen();
        }
    }


    @Override
        public void onCreate(Bundle savedInstance) {
        Report.v("SplashActivity.onCreate()");
        super.onCreate(savedInstance);
        if (getApp().needInitialization()) {
            setContentView(R.layout.splash);
            SutroProgressDialog dialog = new SutroProgressDialog();
            dialog.execute();
        } else {
            gotoMainScreen();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Report.v(this.getClass().getName() + ".onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    protected void onDestroy() {
        Report.v(this.getClass().getName() + ".onDestroy");
        super.onDestroy();
    }

    private void gotoMainScreen() {
        if (getApp().hasNavigationEntry()) {
            Report.v("Restore application state");
        } else {
            Report.v("Go to the root screen");
            getApp().enterAppGotoMainScreen(this);
        }
        finish();
    }

    private MainApp getApp() {
        MainApp application = (MainApp) getApplication();
        return application;
    }

}
