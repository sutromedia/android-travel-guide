package com.sutromedia.android.core;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.params.HttpClientParams;

import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.http.Server;
import com.sutromedia.android.lib.model.PhotoDownload;
import com.sutromedia.android.lib.model.PhotoSize;
import com.sutromedia.android.lib.util.Timer;
import com.sutromedia.android.lib.util.UtilStream;


class DownloadFilesTask extends AsyncTask<List<PhotoDownload>, PhotoDownload, Void> {


    interface IDownloadNotification {
        void onReceiveDownloadedImage(PhotoDownload download);
        void onAllImagesReceived();
        void onDownloadImagesCancelled();
    }

    class DownloadNotificationDefault implements IDownloadNotification {
        public void onReceiveDownloadedImage(PhotoDownload id) {
        }

        public void onAllImagesReceived() {
        }

        public void onDownloadImagesCancelled() {
        }
    }

    private final Activity                      mActivity;
    private final DownloadNotificationDefault   mClient = new DownloadNotificationDefault();
    private Timer                               mTimer;
    private long                                mBytes;

    DownloadFilesTask(Activity activity) {
        mActivity = activity;
        mBytes = 0;
    }

    protected void onPreExecute() {
        mTimer = new Timer();
    }

    private List<PhotoDownload> getMissingImages(List<PhotoDownload> original) {
        List<PhotoDownload> missing = new ArrayList<PhotoDownload>();
        for (PhotoDownload photo : original) {
            File image = getSavedPhotoPath(photo);
            if (!image.exists()) {
                missing.add(photo);
            }
        }
        return missing;
    }

    protected Void doInBackground(List<PhotoDownload>... ids) {
        List<PhotoDownload> data = getMissingImages(ids[0]);
        int count = data.size();
        Timer downloadTimer = new Timer();

        for (int i = 0; (i < count) && !isCancelled(); i++) {
            PhotoDownload download  = data.get(i);
            File image = getSavedPhotoPath(download);
            if (image.exists()) {
                //it's already there => skip it
            } else {
                if (getApp().isOnline()) {
                    AndroidHttpClient http = getApp().getHttp();
                    synchronized(http) {
                        downloadPhoto(http, download);
                        long elapsed = downloadTimer.getElapsedReset();
                        long xferSpeed = image.length() * 1000 / elapsed;
                        String stats = String.format(
                            "%dKBin %dms (%dKB/s)",
                            image.length()/1024,
                            elapsed,
                            image.length()*1000/elapsed/1024);

                        Report.v("[" + (i+1) + "/" + count
                               + "] Downloading photo ["
                               + download.getId()
                               + "]. "
                               + stats);

                        publishProgress(download);
                        mBytes += image.length();
                    }
                }
            }
        }
        return null;
    }

    protected void onProgressUpdate(PhotoDownload... progress) {
        getClient().onReceiveDownloadedImage(progress[0]);
    }

    protected void onPostExecute(Void result) {
        //all done
        if (mBytes > 0) {
            long elapsed = mTimer.getElapsed();
            long kb = mBytes / 1024;
            long mb = mBytes / 1024 / 1024;
            long displayBytes = mb;
            String displaySizeUnit = "MB";
            if (mb == 0) {
                displayBytes = kb;
                displaySizeUnit = "KB";
            }
            String stats = String.format(
                "Finished download in %sms. [%d%s at (%dKB/s)]",
                elapsed,
                displayBytes,
                displaySizeUnit,
                kb*1000/elapsed);

            Report.v(stats);
        }
        getClient().onAllImagesReceived();
    }

    protected void onCancelled(Void result) {
        getClient().onDownloadImagesCancelled();
    }

    private IDownloadNotification getClient() {
        IDownloadNotification client = mClient;
        if (mActivity instanceof IDownloadNotification) {
            client = (IDownloadNotification) mActivity;
        }
        return client;
    }

    private String getImageName(PhotoDownload download) {
        return download.getLocalFilename();
    }

    private void downloadPhoto(AndroidHttpClient client, PhotoDownload download) {
        final String url = PhotoSize.getDownloadUrlForPhoto(
	        download.getId(),
            download.getSize());

        try {
            File savePhoto = getSavedPhotoPath(download);
            Server.getFile(client, url, savePhoto);
        } catch (Exception error) {
            //TODO: notify client that there was a problem
        }
    }

    private File getSavedPhotoPath(PhotoDownload download) {
        File root = getApp().getDestinationPhotoFolder();
        File savePhoto = new File(root, getImageName(download));
        return savePhoto;
    }

    private MainApp getApp() {
        return (MainApp)mActivity.getApplication();
    }
}
