package com.sutromedia.android.lib.view;

import android.net.Uri;
import java.util.HashSet;
import com.sutromedia.android.lib.diagnostic.*;

public class WebLink {

    private final static String          TAG_HTTPS = "https://";
    private final static String          TAG_ENTRY = "smentrylink://";
    private final static String          TAG_CATEGORY = "smtag:";
    private final static String          TAG_PHONE = "tel:";
    private final static String          TAG_MAP = "map:";
    private final static String          TAG_COMMENT = "smcomment:";
    private final static String          TAG_COMMENT_SUBMIT = "smcommentsubmit:";
    private final static String          TAG_UNNECESSARY = "sutromedia://unecessary/";

    private final static HashSet<String> DOC_EXTENSION = new HashSet<String>();
    private final static HashSet<String> YOUTUBE = new HashSet<String>();

    static {
	YOUTUBE.add("www.youtube.com");
	YOUTUBE.add("youtube.com");
	YOUTUBE.add("m.youtube.com");
	YOUTUBE.add("youtu.be");

	DOC_EXTENSION.add("pdf");
	DOC_EXTENSION.add("mp3");
    }

    public enum LinkType {
        Web,
        ExternalWeb,
        EntryDetail,
        Category,
        Map,
        Phone,
	Comment,
	CommentForm,
	YouTube,
	WebDocument
    }
    
    private     LinkType        mType;
    private     String          mData;

    private WebLink(LinkType type, String data) {
        mType = type;
        mData = data;
    } 
    
    public LinkType getType() {
        return mType;
    }
    
    public String getData() {
        return mData;
    }

    public String guessFilename() {
	Uri uri = Uri.parse(mData);
	if (uri != null) {
	    String path = uri.getLastPathSegment();
	    return path;
	} else {
	    //Come up with a name
	    //TODO JMV
	}
	return null;
    }

    public String guessExtension() {
	Uri uri = Uri.parse(mData);
	if (uri != null) {
	    String path = uri.getLastPathSegment().toLowerCase();
	    String extension = getExtension(path);
	    return extension;
	}
	return null;
    }

    public static WebLink parse(String url) {
        if (url != null && !url.trim().equals("")) {
            String trimmed = url.trim().toLowerCase();
            if (trimmed.startsWith(TAG_ENTRY)) {
                return new WebLink(LinkType.EntryDetail, trimmed.replace(TAG_ENTRY, "").trim());
            } else if (trimmed.startsWith(TAG_CATEGORY)) {
                return new WebLink(LinkType.Category, trimmed.replace(TAG_CATEGORY, "").trim());
            } else if (trimmed.startsWith(TAG_PHONE)) {
                return new WebLink(LinkType.Phone, trimmed);
            } else if (trimmed.startsWith(TAG_MAP)) {
                return new WebLink(LinkType.Map, trimmed.replace(TAG_MAP, "").trim());
            } else if (trimmed.startsWith(TAG_COMMENT)) {
                return new WebLink(LinkType.Comment, trimmed.replace(TAG_COMMENT, "").trim());
            } else if (trimmed.startsWith(TAG_COMMENT_SUBMIT)) {
                return new WebLink(LinkType.CommentForm, trimmed.replace(TAG_COMMENT_SUBMIT, "").trim());
            } else if (trimmed.startsWith(TAG_HTTPS)) {
                return new WebLink(LinkType.ExternalWeb, url);
            } else if (trimmed.startsWith(TAG_UNNECESSARY)) {
                return new WebLink(LinkType.Web, "http://" + trimmed.replace(TAG_UNNECESSARY, "").trim());
            } else {
		return parseAsUrl(url);
            }
        }
        return null;
    }

    private static WebLink parseAsUrl(final String url) {
	try {
	    Uri uri = Uri.parse(url);
	    String host  = uri.getHost();
	    String video = uri.getQueryParameter("v");
	    String path = uri.getLastPathSegment().toLowerCase();
	    if (YOUTUBE.contains(host)) {
		if ((video != null) && (video.length()>0)) {
		    return new WebLink(LinkType.YouTube, video);
		} else {
		    return new WebLink(LinkType.ExternalWeb, url);
		}
	    } else {
		//See if the link might point to a document.
		String extension = getExtension(path);
		Debug.v("Path for URL [" + url + "] is [" + path + "]");
		Debug.v("Extension for URL [" + url + "] is [" + extension + "]");
		if (extension != null && DOC_EXTENSION.contains(extension)) {
		    return new WebLink(LinkType.WebDocument, url);
		}
	    }
	} catch (Exception error) {
	    //This is not really  url => do nothing for now, although
	    //we might consider putting a warning for example.
	}
	return new WebLink(LinkType.Web, url);
    }


    private static String getExtension(final String path) {
	if (path != null) {
	    int lastDot = path.lastIndexOf('.');
	    if (lastDot != -1 && lastDot < path.length() - 1) {
		String extension = path.substring(lastDot+1);
		return extension;
	    }
	}
	return null;
    }
}
