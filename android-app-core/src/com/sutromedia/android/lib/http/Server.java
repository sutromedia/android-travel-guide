package com.sutromedia.android.lib.http;

import android.net.http.AndroidHttpClient;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;

import com.sutromedia.android.lib.diagnostic.*;
import com.sutromedia.android.lib.util.UtilStream;

public class Server {

    public static void getFile(
        final AndroidHttpClient client,
        final String url,
        final File destination) throws Exception {

        final HttpGet getRequest = new HttpGet(url);
        HttpResponse response = null;
        try {
            Report.v("Start downloading file from " + url);
            response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Report.v("Error " + statusCode + " while retrieving file from " + url);
            } else {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = entity.getContent();
                        File tempFile = getTemporaryFile(destination);
                        FileOutputStream out = new FileOutputStream(tempFile);
                        Report.v("Saving file to " + tempFile.getAbsolutePath());
                        UtilStream.copy(inputStream, out);
                        out.close();
                        tempFile.renameTo(destination);
                        Report.v("Renaming file to " + destination.getAbsolutePath());
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Report.logUnexpectedException("Error while retrieving file from " + url, e);
            throw e;
        }
    }

    public static void postComment(
        final AndroidHttpClient client,
        final String url,
        final String appId,
        final String entryId,
        final String entryName,
        final String email,
        final String alias,
        final String message) throws IOException {

        Report.v("About to send comment:\n" + message);
        if (message != null) {
            ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            setupParam(params, "email_address", email);
            setupParam(params, "username", alias);
            setupParam(params, "feedback", message);
            setupParam(params, "appid", appId);
            setupParam(params, "entryid", entryId);
            setupParam(params, "deviceid", null);
            setupParam(params, "entryname", entryName);
	    setupParam(params, "channel", "1");
            setupParamIfNotNull(params, "latitude", null);
            setupParamIfNotNull(params, "longitude", null);
            setupParam(params, "osversion", Integer.toString(Build.VERSION.SDK_INT));
            setupParam(params, "devicetype", "android");
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
            ByteArrayOutputStream debug = new ByteArrayOutputStream();
            entity.writeTo(debug);
            Report.v("Sending POST request to URL=" + url);
            Report.v("Sending POST request:\n" + new String(debug.toByteArray()));

            HttpPost post = new HttpPost(url);
            post.setEntity(entity);

             // Execute HTTP Post Request
            HttpResponse response = client.execute(post);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Report.v("Error " + statusCode + " while posting comment to " + url);
            } else {
                final HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    responseEntity.consumeContent();
                }
            }
        }
    }

    private static void setupParam(
        final ArrayList<BasicNameValuePair> params,
        final String key,
        final String value) {

        String actual = (value == null) ? "" : value;
        params.add(new BasicNameValuePair(key, value));
    }

    private static void setupParamIfNotNull(
        final ArrayList<BasicNameValuePair> params,
        final String key,
        final String value) {

        if (value != null) {
            params.add(new BasicNameValuePair(key, value));
        }
    }

    private static File getTemporaryFile(final File destination) throws IOException {
        File root = destination.getParentFile();
        File tempPath = File.createTempFile(destination.getName(), "part", root);
        return tempPath;
    }
}