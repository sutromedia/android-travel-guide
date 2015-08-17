package com.sutromedia.android.lib.http;

import java.util.HashMap;

public class Mime {

    private final static HashMap<String, String> MIME = new HashMap<String, String>();
    
    static  {
	MIME.put("pdf", "application/pdf");
	MIME.put("zip", "application/zip");
	MIME.put("gzip", "application/gzip");
	MIME.put("mp4", "audio/mp4");
	MIME.put("mp3", "audio/mpeg");
    }

    public static String guessFromExtension(final String extension) {
	if (extension != null) {
	    String mime = MIME.get(extension.toLowerCase());
	    return mime;
	}
	return null;
    }
}