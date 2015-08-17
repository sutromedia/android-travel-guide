package com.sutromedia.android.lib.model;


public class Comment extends ModelBase implements IComment {
    
    private String  mMessage;
    private String  mDate;
    private String  mAuthor;

    public Comment(
        final String message, 
	final String date, 
	final String author) throws DataException {

	mMessage = ensureNotEmptyTrimmed(message, "Comment body should not be empty");
	mDate = ensureNotNullIfEmpty(date);
	mAuthor = ensureNullIfTrimmedEmpty(author);
    }

    public String getMessage() {
	return mMessage;
    }

    public String getDate() {
	return mDate;
    }

    public String getAuthor() {
	return mAuthor;
    }
}
