package com.sutromedia.android.lib.model;

import java.util.Random;

public class PhotoSize {

    public final static int CUTOFF_SMALL = 150;
    public final static int CUTOFF_MEDIUM = 600;

    public final static int SMALL = 0;
    public final static int MEDIUM = 1;
    public final static int LARGE = 2;
    
    public static String getColumnNameForSize(int size) {
        switch (size) {
            case SMALL:
                return "downloaded_x100px_photo";
            case MEDIUM:
                return "downloaded_320px_photo";
            default:
                return "downloaded_768px_photo";
        }
    }

    public static String getDownloadUrlForPhoto(String photoId, int bestSize) {
        String template = getUrlTemplate(bestSize);
	Random server = new Random();
	int serverIndex = server.nextInt(3) + 1;
        return String.format(template, serverIndex, photoId);
    }
    
    public static String getLocalFilename(String id, int size) {
        switch (size) {
            case SMALL:
                return id + "_x100.jpg";
            case MEDIUM:
                return id + "-medium.jpg";
            default:
                return id + "-large.jpg";
        }
    }    

    private static String getUrlTemplate(int size) {
        switch (size) {
            case SMALL:
	        return "http://www.sutromedia.com/published/iphone-sized-photos/icons/%2$s-icon.jpg";
                //return "http://pub%s.sutromedia.com/dynamic-photos/100/%s.jpg";
            case MEDIUM:
                return "http://pub%1$s.sutromedia.com/published/480-sized-photos/%2$s.jpg";
            default:
                return "http://pub%1$s.sutromedia.com/published/ipad-sized-photos/%2$s.jpg";
        }
    }    
}